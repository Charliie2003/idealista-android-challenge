---
name: testing-patterns
description: Read this before writing any test. Contains ready-to-adapt recipes for unit tests, ViewModel tests with Turbine, Room DAO tests, and Compose UI tests. Follow these patterns exactly unless you have a specific reason not to.
---

# Testing Patterns

Every test is an investment. These patterns keep the investment worthwhile.

## Common utilities (create these once, use everywhere)

### `MainDispatcherRule` (JVM unit tests)

```kotlin
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

Place in `:core:testing` (JVM library) or in each module's `src/test/kotlin/testing/` if you don't want a testing module.

### Fixtures

Create `Fixtures.kt` per module with hand-written domain instances. Never share fixtures across modules — each module owns its own.

```kotlin
object PropertyFixtures {
    fun sale(id: String = "1"): Property = Property(
        id = id,
        priceLabel = "1.195.000 €",
        operation = Operation.SALE,
        // ...
    )

    fun rent(id: String = "2"): Property = Property(
        id = id,
        priceLabel = "1.200 €/mes",
        operation = Operation.RENT,
        // ...
    )
}
```

Fixtures with named defaults are the most maintainable way to write tests.

## Unit test — pure function (mapper)

```kotlin
class PropertyDtoMapperTest {

    @Test
    fun `maps sale property with correct price suffix`() {
        // given
        val dto = PropertyDto(
            propertyCode = "1",
            priceInfo = PriceInfoDto(
                price = PriceDto(amount = 1_195_000.0, currencySuffix = "€"),
            ),
            operation = "sale",
            // ...
        )

        // when
        val domain = dto.toDomain()

        // then
        assertThat(domain.id).isEqualTo("1")
        assertThat(domain.priceLabel).isEqualTo("1.195.000 €")
        assertThat(domain.operation).isEqualTo(Operation.SALE)
    }

    @Test
    fun `maps rent property with monthly suffix`() {
        val dto = PropertyDtoFixtures.rent()
        val domain = dto.toDomain()

        assertThat(domain.priceLabel).endsWith("€/mes")
        assertThat(domain.operation).isEqualTo(Operation.RENT)
    }
}
```

## Unit test — use case with fake repository

```kotlin
class ToggleFavoriteUseCaseTest {

    private val fixedInstant = Instant.parse("2025-11-04T10:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    private val repo = FakeFavoritesRepository()
    private val useCase = ToggleFavoriteUseCase(repo, clock)

    @Test
    fun `marking a non-favorite persists the current instant`() = runTest {
        useCase.invoke("1")

        assertThat(repo.get("1")).isEqualTo(Favorite("1", fixedInstant))
    }

    @Test
    fun `re-marking an existing favorite updates the timestamp`() = runTest {
        val laterInstant = fixedInstant.plusSeconds(60)
        val laterClock = Clock.fixed(laterInstant, ZoneOffset.UTC)
        repo.add("1", fixedInstant) // seed
        val useCase = ToggleFavoriteUseCase(repo, laterClock)

        useCase.invoke("1")

        assertThat(repo.get("1")?.favoritedAt).isEqualTo(laterInstant)
    }
}

private class FakeFavoritesRepository : FavoritesRepository {
    private val map = mutableMapOf<String, Favorite>()

    override suspend fun add(id: String, at: Instant) {
        map[id] = Favorite(id, at)
    }

    override suspend fun get(id: String): Favorite? = map[id]

    override fun observeAll(): Flow<List<Favorite>> = flowOf(map.values.toList())
}
```

## ViewModel test with Turbine

```kotlin
class ListingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeProperties = FakeObservePropertiesUseCase()
    private val toggleFavorite: ToggleFavoriteUseCase = mockk(relaxed = true)

    private fun viewModel() = ListingViewModel(observeProperties, toggleFavorite)

    @Test
    fun `emits loading then content when properties are available`() = runTest {
        observeProperties.emit(listOf(PropertyFixtures.sale()))

        viewModel().state.test {
            assertThat(awaitItem()).isInstanceOf(ListingUiState.Loading::class.java)
            val content = awaitItem() as ListingUiState.Content
            assertThat(content.properties).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits error when the source flow throws`() = runTest {
        observeProperties.emitError(DomainError.Network)

        viewModel().state.test {
            skipItems(1) // Loading
            assertThat(awaitItem()).isInstanceOf(ListingUiState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

## Turbine + `stateIn(WhileSubscribed)` gotcha

With `UnconfinedTestDispatcher`, coroutines run eagerly on the same thread. If the upstream flow produces values synchronously (e.g. `flowOf(value)` or a `coEvery { } returns flowOf(...)`), then by the time Turbine's `test { }` block subscribes:

1. `stateIn` starts collecting.
2. `onStart { emit(Loading) }` fires → Loading enters the `StateFlow`.
3. Upstream emits Content immediately (synchronously) → replaces Loading in the `StateFlow`.

Because `StateFlow` only replays the **current** value (buffer=1), the subscriber only sees Content — the Loading state was already overwritten.

**Rule: never use `skipItems(1)` to skip a Loading item in a ViewModel test where the use case is a `flowOf(...)` mock.** Receive the first item directly:

```kotlin
@Test
fun `emits Content when detail is available`() = runTest {
    coEvery { observeDetail("1") } returns flowOf(DetailFixtures.propertyDetail())
    coEvery { isFavorite("1") } returns flowOf(null)

    viewModel().state.test {
        // No skipItems — Loading was already overwritten before Turbine subscribes
        val content = awaitItem() as DetailUiState.Content
        assertThat(content.isFavorite).isFalse()
        cancelAndIgnoreRemainingEvents()
    }
}
```

**When Loading IS visible:** This happens when the use case is backed by a `MutableSharedFlow` that hasn't emitted yet (see `ListingViewModelTest`). The `onStart { emit(Loading) }` fires and stays in the `StateFlow` until the test explicitly calls `emit(...)` on the fake. In that case, `awaitItem()` → Loading, then `awaitItem()` → Content is correct.

```
Upstream is MutableSharedFlow (nothing yet):  Loading → Content  → skipItems(1) IS needed
Upstream is flowOf(value) (immediate):        Content only       → skipItems(1) MUST NOT be used
```

## MockK: `coEvery` vs `every`

- **`coEvery { suspendFun(...) } returns value`** — for suspend functions (repository methods, use cases returning a single value).
- **`every { flowFun(...) } returns flowOf(value)`** — for functions returning `Flow<T>` (they are NOT suspend functions, even if their body collects another flow).

```kotlin
// Correct:
coEvery { observeDetail("1") } returns flowOf(detail)   // returns Flow, NOT suspend — use every
every { isFavorite("1") } returns flowOf(favorite)       // same

coEvery { toggleFavorite("1") } just Runs               // suspend function — use coEvery
```

Wait — `observeDetail` and `isFavorite` return `Flow<T>`, so:
```kotlin
every { observeDetail("1") } returns flowOf(detail)     // correct (not suspend)
every { isFavorite("1") } returns flowOf(null)           // correct (not suspend)
coEvery { toggleFavorite("1") } just Runs               // correct (suspend)
```

If you use `coEvery` on a non-suspend function, MockK will compile but it inserts an unnecessary coroutine wrapper. Use `every` for Flow-returning functions.

## Room DAO test (in-memory)

```kotlin
@RunWith(AndroidJUnit4::class)
class FavoritesDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FavoritesDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.favoritesDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun observeAll_emitsInsertedFavorite() = runTest {
        val favorite = FavoriteEntity(propertyId = "1", favoritedAt = 1_700_000_000_000L)

        dao.upsert(favorite)

        dao.observeAll().test {
            assertThat(awaitItem()).containsExactly(favorite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upsert_replacesExistingRowForSamePropertyId() = runTest {
        dao.upsert(FavoriteEntity("1", 1_700_000_000_000L))
        dao.upsert(FavoriteEntity("1", 1_700_000_060_000L))

        val rows = dao.observeAll().first()
        assertThat(rows).hasSize(1)
        assertThat(rows.first().favoritedAt).isEqualTo(1_700_000_060_000L)
    }
}
```

## Compose UI test

```kotlin
@HiltAndroidTest
class DetailScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Test
    fun tappingFavorite_showsFavoritedDate() {
        composeRule.setContent {
            AppTheme {
                DetailScreen(
                    state = DetailUiState.Content(PropertyDetailFixtures.notFavorited()),
                    onFavoriteToggle = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithTag("favorite_toggle").performClick()

        // In a real test with a hoisted stateful root, assert the state changed:
        // composeRule.onNodeWithText("Guardado el 4 nov 2025").assertIsDisplayed()
    }
}
```

## End-to-end (Espresso + Compose)

```kotlin
@HiltAndroidTest
class FavoriteFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun markingFavoriteFromList_persistsToDetail() {
        // Espresso for XML list
        onView(withId(R.id.recyclerView))
            .perform(actionOnItemAtPosition<PropertyViewHolder>(0, clickFavorite()))

        onView(withId(R.id.recyclerView))
            .perform(actionOnItemAtPosition<PropertyViewHolder>(0, click()))

        // Compose for detail
        composeRule.onNodeWithTag("favorite_toggle").assertIsDisplayed()
        composeRule.onNodeWithText("Guardado", substring = true).assertIsDisplayed()
    }
}

fun clickFavorite() = object : ViewAction {
    override fun getConstraints() = null
    override fun getDescription() = "Click favorite icon in card"
    override fun perform(uiController: UiController, view: View) {
        view.findViewById<View>(R.id.favoriteButton).performClick()
    }
}
```

## What NOT to do in tests

- **No `Thread.sleep`.** Use `advanceUntilIdle()` or `IdlingResource`.
- **No `@Ignore`.** Delete or fix.
- **No shared mutable state at class level** unless it's `@Before`-initialized.
- **No mocks of the class under test.**
- **No assertions on interaction counts** (`verify(x, times(1))`) unless it's genuinely the only observable behavior.
- **No test named after a method** (`testGetProperties`). Name after behavior.
