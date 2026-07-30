package com.carloshinojosa.idealistachallenge.list

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.carloshinojosa.idealistachallenge.core.domain.error.DomainError
import com.carloshinojosa.idealistachallenge.core.domain.usecase.ObservePropertiesUseCase
import com.carloshinojosa.idealistachallenge.core.domain.usecase.ToggleFavoriteUseCase
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import com.carloshinojosa.idealistachallenge.list.presentation.model.FilterType
import com.carloshinojosa.idealistachallenge.list.presentation.model.ListingUiState
import com.carloshinojosa.idealistachallenge.list.presentation.model.PropertyMapper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ListingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    // MockK mocks classes (final Kotlin classes) using inline mocking.
    private val observeProperties: ObservePropertiesUseCase = mockk()
    private val toggleFavorite: ToggleFavoriteUseCase = mockk(relaxed = true)

    // Context with relaxed mock: getString returns "" for all calls; the mapper still produces
    // correct operationType and isFavorite values from domain data, which is what filter tests need.
    private val context: Context = mockk(relaxed = true)
    private val mapper = PropertyMapper(context)

    @Before
    fun setUp() {
        // Default stub: success with an empty list. Individual tests override via every { }.
        every { observeProperties() } returns flowOf(Result.Success(emptyList()))
    }

    @After
    fun tearDown() {
        io.mockk.clearAllMocks()
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private fun createViewModel() = ListingViewModel(
        observeProperties = observeProperties,
        toggleFavorite = toggleFavorite,
        mapper = mapper,
    )

    /**
     * Observes [this] LiveData for the lifetime of [block] and returns all emitted values.
     * Relies on [InstantTaskExecutorRule] for synchronous delivery.
     */
    private fun <T> LiveData<T>.collectValues(block: () -> Unit = {}): List<T> {
        val values = mutableListOf<T>()
        val observer = Observer<T> { values.add(it) }
        observeForever(observer)
        try {
            block()
        } finally {
            removeObserver(observer)
        }
        return values
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun `state has no value before being observed`() {
        // LiveData backed by switchMap is inactive until observed.
        // Without an observer the shareIn upstream never starts collecting, so value stays null.
        val vm = createViewModel()

        assertNull(vm.state.value)
    }

    // ── success path ──────────────────────────────────────────────────────────

    @Test
    fun `state reflects Content with sale items when use case emits success`() {
        // given
        val saleProperty = PropertyFixtures.sale()
        every { observeProperties() } returns flowOf(Result.Success(listOf(saleProperty)))

        // when
        val states = createViewModel().state.collectValues()

        // then – default filter is SALE; one sale property should appear
        val content = states.last() as ListingUiState.Content
        assertEquals(1, content.items.size)
        assertEquals(saleProperty.id, content.items.first().id)
    }

    // ── filter: FAVORITES with no favorites ───────────────────────────────────

    @Test
    fun `state reflects Empty when filter is FAVORITES and no items are favorited`() {
        // given – a sale property that is NOT favorited
        every { observeProperties() } returns flowOf(Result.Success(listOf(PropertyFixtures.sale())))
        val vm = createViewModel()

        // when
        val states = vm.state.collectValues {
            vm.onFilterChanged(FilterType.FAVORITES)
        }

        // then
        assertTrue(
            "Expected at least one Empty state, got: $states",
            states.any { it is ListingUiState.Empty },
        )
        assertTrue(states.last() is ListingUiState.Empty)
    }

    // ── filter: FAVORITES with favorites present ───────────────────────────────

    @Test
    fun `state reflects Content when filter is FAVORITES and favorited items exist`() {
        // given – one favorited sale, one unfavorited sale
        val fav = PropertyFixtures.favoritedSale(id = "fav-1")
        val notFav = PropertyFixtures.sale(id = "sale-1")
        every { observeProperties() } returns flowOf(Result.Success(listOf(fav, notFav)))
        val vm = createViewModel()

        // when
        val states = vm.state.collectValues {
            vm.onFilterChanged(FilterType.FAVORITES)
        }

        // then – only the favorited item should be in Content
        val content = states.last() as ListingUiState.Content
        assertEquals(1, content.items.size)
        assertEquals(fav.id, content.items.first().id)
    }

    // ── error path ────────────────────────────────────────────────────────────

    @Test
    fun `state reflects Error when use case emits a failure`() {
        // given
        every { observeProperties() } returns flowOf(Result.Error(DomainError.Network))

        // when
        val states = createViewModel().state.collectValues()

        // then
        assertTrue(
            "Expected Error state, got: $states",
            states.last() is ListingUiState.Error,
        )
    }

    @Test
    fun `Error state carries the listing_error_message string resource`() {
        every { observeProperties() } returns flowOf(Result.Error(DomainError.Network))

        val states = createViewModel().state.collectValues()

        val error = states.last() as ListingUiState.Error
        val message = error.message as com.carloshinojosa.idealistachallenge.core.domain.util.UiText.StringResource
        assertEquals(R.string.listing_error_message, message.resId)
    }

    // ── retry ─────────────────────────────────────────────────────────────────

    @Test
    fun `onRetryClicked triggers a fresh observeProperties call and recovers from error`() {
        // given – first call returns error, second call (after retry) returns success
        every { observeProperties() } returnsMany listOf(
            flowOf(Result.Error(DomainError.Network)),
            flowOf(Result.Success(listOf(PropertyFixtures.sale()))),
        )
        val vm = createViewModel()

        val states = mutableListOf<ListingUiState>()
        val observer = Observer<ListingUiState> { states.add(it) }
        vm.state.observeForever(observer)

        assertTrue("Expected initial Error before retry", states.last() is ListingUiState.Error)

        // when
        vm.onRetryClicked()

        // then – flatMapLatest re-invokes observeProperties(); second call returns success
        assertTrue("Expected Content after retry", states.last() is ListingUiState.Content)
        vm.state.removeObserver(observer)
    }

    // ── favoritesCount ────────────────────────────────────────────────────────

    @Test
    fun `favoritesCount emits the number of favorited items`() {
        // given – two items, one favorited
        val fav = PropertyFixtures.favoritedSale(id = "fav-1")
        val notFav = PropertyFixtures.sale(id = "sale-1")
        every { observeProperties() } returns flowOf(Result.Success(listOf(fav, notFav)))

        // when
        val counts = createViewModel().favoritesCount.collectValues()

        // then
        assertEquals(1, counts.last())
    }

    @Test
    fun `favoritesCount emits zero when no items are favorited`() {
        every { observeProperties() } returns flowOf(
            Result.Success(listOf(PropertyFixtures.sale(), PropertyFixtures.rent())),
        )

        val counts = createViewModel().favoritesCount.collectValues()

        assertEquals(0, counts.last())
    }

    // ── filter: ALL ───────────────────────────────────────────────────────────

    @Test
    fun `state reflects Content with all items when filter is ALL`() {
        // given – one sale and one rent property
        val sale = PropertyFixtures.sale(id = "s1")
        val rent = PropertyFixtures.rent(id = "r1")
        every { observeProperties() } returns flowOf(Result.Success(listOf(sale, rent)))
        val vm = createViewModel()

        // when
        val states = vm.state.collectValues {
            vm.onFilterChanged(FilterType.ALL)
        }

        // then – both items appear unfiltered
        val content = states.last() as ListingUiState.Content
        assertEquals(2, content.items.size)
    }

    @Test
    fun `state reflects Content (not Empty) when filter is ALL and list is empty`() {
        every { observeProperties() } returns flowOf(Result.Success(emptyList()))
        val vm = createViewModel()

        // when
        val states = vm.state.collectValues {
            vm.onFilterChanged(FilterType.ALL)
        }

        // then – Empty is reserved for FAVORITES; ALL with no items is Content(emptyList())
        assertTrue(states.last() is ListingUiState.Content)
    }
}
