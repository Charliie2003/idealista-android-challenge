package com.carloshinojosa.idealistachallenge.list.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.carloshinojosa.idealistachallenge.core.domain.model.Property.Companion.OPERATION_RENT
import com.carloshinojosa.idealistachallenge.core.domain.model.Property.Companion.OPERATION_SALE
import com.carloshinojosa.idealistachallenge.core.domain.usecase.ObservePropertiesUseCase
import com.carloshinojosa.idealistachallenge.core.domain.usecase.ToggleFavoriteUseCase
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import com.carloshinojosa.idealistachallenge.design.ui.theme.UiText
import com.carloshinojosa.idealistachallenge.list.R
import com.carloshinojosa.idealistachallenge.list.presentation.model.FilterType
import com.carloshinojosa.idealistachallenge.list.presentation.model.PropertyCardUiModel
import com.carloshinojosa.idealistachallenge.list.presentation.mapper.PropertyMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the listing screen.
 *
 * Exposes [state] as [LiveData] (rather than StateFlow) because the listing screen uses XML views
 * with [androidx.lifecycle.Observer], which integrates naturally with LiveData lifecycle handling.
 * StateFlow is used in the detail screen (Compose).
 */
@HiltViewModel
class ListingViewModel @Inject constructor(
    private val observeProperties: ObservePropertiesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val mapper: PropertyMapper,
) : ViewModel() {

    private val _filter = MutableLiveData(FilterType.SALE)
    val filter: LiveData<FilterType> = _filter

    private val _retryTrigger = MutableStateFlow(0)

    /**
     * Shared mapped flow. Uses an explicit [when] expression instead of Result.map to avoid
     * a name collision between [map] and the Result extension.
     * Shared so that [state] and [favoritesCount] collect a single upstream subscription.
     * Re-fetches from scratch whenever [_retryTrigger] increments.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawFlow: SharedFlow<Result<List<PropertyCardUiModel>>> =
        _retryTrigger
            .flatMapLatest {
                observeProperties().map { result ->
                    when (result) {
                        is Result.Success -> Result.Success(result.data.map(mapper::map))
                        is Result.Error -> result
                    }
                }
            }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    /**
     * The current UI state, recomputed whenever the active filter changes.
     * Uses [switchMap] so that stale in-flight emissions from a previous filter are cancelled.
     */
    val state: LiveData<ListingUiState> =
        _filter.switchMap { filter ->
            rawFlow
                .map { result -> toUiState(result, filter) }
                .distinctUntilChanged()
                .asLiveData()
        }

    val favoritesCount: LiveData<Int> =
        rawFlow
            .map { result ->
                if (result is Result.Success) result.data.count { it.isFavorite } else 0
            }
            .asLiveData()

    fun onFilterChanged(type: FilterType) {
        _filter.value = type
    }

    fun onFavoriteClicked(id: String) {
        viewModelScope.launch { toggleFavorite(id) }
    }

    fun onRetryClicked() {
        _retryTrigger.value++
    }

    private fun toUiState(
        result: Result<List<PropertyCardUiModel>>,
        filter: FilterType,
    ): ListingUiState = when (result) {
        is Result.Error -> ListingUiState.Error(
            UiText.StringResource(R.string.listing_error_message),
        )
        is Result.Success -> {
            val filtered = applyFilter(result.data, filter)
            when {
                filtered.isEmpty() && filter == FilterType.FAVORITES -> ListingUiState.Empty
                else -> ListingUiState.Content(filtered)
            }
        }
    }

    private fun applyFilter(
        items: List<PropertyCardUiModel>,
        filter: FilterType,
    ): List<PropertyCardUiModel> = when (filter) {
        FilterType.SALE      -> items.filter { it.operationType == OPERATION_SALE }
        FilterType.RENT      -> items.filter { it.operationType == OPERATION_RENT }
        FilterType.FAVORITES -> items.filter { it.isFavorite }
        FilterType.ALL       -> items
    }
}
