package com.carloshinojosa.idealistachallenge.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carloshinojosa.idealistachallenge.core.domain.error.DomainError
import com.carloshinojosa.idealistachallenge.core.domain.usecase.IsFavoriteUseCase
import com.carloshinojosa.idealistachallenge.core.domain.usecase.ObservePropertyDetailUseCase
import com.carloshinojosa.idealistachallenge.core.domain.usecase.ToggleFavoriteUseCase
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import com.carloshinojosa.idealistachallenge.detail.presentation.mapper.DetailMapper
import com.carloshinojosa.idealistachallenge.detail.presentation.DetailUiState
import com.carloshinojosa.idealistachallenge.detail.presentation.DetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [com.carloshinojosa.idealistachallenge.detail.presentation.DetailViewModel].
 *
 * The ViewModel uses `stateIn(WhileSubscribed)` with [DetailUiState.Loading] as the initial
 * value. With [UnconfinedTestDispatcher] installed as Dispatchers.Main, the `flatMapLatest`
 * upstream runs synchronously during the StateFlow subscription, replacing the initial Loading
 * value before any subscriber receives the first emission. Consequently Turbine's first
 * `awaitItem()` yields Content/Error — never Loading — and no `skipItems(1)` is needed.
 *
 * [DetailMapper] is mocked (relaxed) because its resource-dependent behavior is tested
 * separately in [DetailMapperTest]. Here we only assert on ViewModel state transitions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getDetail: ObservePropertyDetailUseCase = mockk()
    private val isFavorite: IsFavoriteUseCase = mockk()
    private val toggleFavorite: ToggleFavoriteUseCase = mockk(relaxed = true)
    private val mapper: DetailMapper = mockk(relaxed = true)

    @After
    fun tearDown() {
        io.mockk.clearAllMocks()
    }

    private fun createViewModel(propertyId: String = "p-001"): DetailViewModel =
        DetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf(DetailViewModel.KEY_PROPERTY_ID to propertyId)),
            getDetail = getDetail,
            isFavorite = isFavorite,
            toggleFavorite = toggleFavorite,
            mapper = mapper,
        )

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun `state value is Loading before any subscriber is attached`() {
        // SharingStarted.WhileSubscribed keeps the upstream inactive until a subscriber appears.
        // Without any subscriber, the StateFlow holds its initialValue indefinitely.
        val vm = createViewModel()

        assertEquals(DetailUiState.Loading, vm.state.value)
    }

    // ── success path, not favorited ───────────────────────────────────────────

    @Test
    fun `state emits Content with isFavorite=false when detail succeeds and property is not favorited`() = runTest {
        // given
        coEvery { getDetail("p-001") } returns Result.Success(DetailFixtures.propertyDetail())
        every { isFavorite("p-001") } returns flowOf(null)

        val vm = createViewModel()

        // With UnconfinedTestDispatcher the upstream runs synchronously during subscription.
        // The StateFlow value is already Content before Turbine delivers its first item.
        vm.state.test {
            val content = awaitItem() as DetailUiState.Content
            assertFalse(content.isFavorite)
            assertNull(content.favoritedDateLabel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── success path, favorited ───────────────────────────────────────────────

    @Test
    fun `state emits Content with isFavorite=true when a matching favorite exists`() = runTest {
        // given
        val favorite = DetailFixtures.favorite()
        coEvery { getDetail("p-001") } returns Result.Success(DetailFixtures.propertyDetail())
        every { isFavorite("p-001") } returns flowOf(favorite)
        every { mapper.formatFavoriteDate(favorite.favoritedAt) } returns "Guardado el 28 de julio de 2026"

        val vm = createViewModel()

        vm.state.test {
            val content = awaitItem() as DetailUiState.Content
            assertTrue(content.isFavorite)
            assertNotNull(content.favoritedDateLabel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── error path ────────────────────────────────────────────────────────────

    @Test
    fun `state emits Error when the detail use case returns a failure`() = runTest {
        // given
        coEvery { getDetail("p-001") } returns Result.Error(DomainError.Network)
        every { isFavorite("p-001") } returns flowOf(null)

        val vm = createViewModel()

        vm.state.test {
            assertTrue(awaitItem() is DetailUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── retry ─────────────────────────────────────────────────────────────────

    @Test
    fun `onRetry recovers from Error to Content on the second attempt`() = runTest {
        // given – first getDetail call fails, second (after retry) succeeds
        coEvery { getDetail("p-001") } returnsMany listOf(
            Result.Error(DomainError.Network),
            Result.Success(DetailFixtures.propertyDetail()),
        )
        every { isFavorite("p-001") } returns flowOf(null)

        val vm = createViewModel()

        vm.state.test {
            // First settled state is Error (upstream ran synchronously during subscription)
            val firstState = awaitItem()
            assertTrue("Expected Error before retry, got: $firstState", firstState is DetailUiState.Error)

            // when
            vm.onRetry()

            // then – flatMapLatest re-triggers; second getDetail call returns Success
            assertTrue("Expected Content after retry", awaitItem() is DetailUiState.Content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── favorite toggle ───────────────────────────────────────────────────────

    @Test
    fun `onFavoriteToggle calls toggleFavorite with the correct property id`() = runTest {
        // given
        coEvery { getDetail("p-001") } returns Result.Success(DetailFixtures.propertyDetail())
        every { isFavorite("p-001") } returns flowOf(null)

        val vm = createViewModel()

        // when – with UnconfinedTestDispatcher the viewModelScope.launch runs synchronously
        vm.onFavoriteToggle()

        // then
        coVerify { toggleFavorite("p-001") }
    }
}
