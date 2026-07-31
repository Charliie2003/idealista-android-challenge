package com.carloshinojosa.idealistachallenge.detail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carloshinojosa.idealistachallenge.core.domain.usecase.IsFavoriteUseCase
import com.carloshinojosa.idealistachallenge.core.domain.usecase.GetPropertyDetailUseCase
import com.carloshinojosa.idealistachallenge.core.domain.usecase.ToggleFavoriteUseCase
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import com.carloshinojosa.idealistachallenge.design.ui.theme.UiText
import com.carloshinojosa.idealistachallenge.detail.R
import com.carloshinojosa.idealistachallenge.detail.presentation.mapper.DetailMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the detail screen.
 *
 * Exposes [state] as [StateFlow] (not LiveData) because the detail screen renders with
 * Jetpack Compose. Contrast with [com.carloshinojosa.idealistachallenge.list.ListingViewModel],
 * which uses LiveData for its XML listing screen.
 *
 * A one-shot detail fetch is combined with a live [IsFavoriteUseCase] flow so that
 * toggling favorites in this screen (or in the listing screen) is reflected immediately
 * without any shared ViewModel or broadcast mechanism — Room is the single source of truth.
 *
 * [_retryTrigger] allows the user to re-fetch after a network error without recreating the
 * ViewModel.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDetail: GetPropertyDetailUseCase,
    private val isFavorite: IsFavoriteUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val mapper: DetailMapper,
) : ViewModel() {

    private val propertyId: String = checkNotNull(savedStateHandle[KEY_PROPERTY_ID])

    private val _retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DetailUiState> = _retryTrigger
        .flatMapLatest {
            flow { emit(getDetail(propertyId)) }
                .combine(isFavorite(propertyId)) { detailResult, favorite ->
                    when (detailResult) {
                        is Result.Error -> DetailUiState.Error(
                            UiText.StringResource(R.string.detail_error_generic),
                        )
                        is Result.Success -> DetailUiState.Content(
                            property = mapper.map(detailResult.data),
                            isFavorite = favorite != null,
                            favoritedDateLabel = favorite?.let { mapper.formatFavoriteDate(it.favoritedAt) },
                        )
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailUiState.Loading,
        )

    fun onFavoriteToggle() {
        viewModelScope.launch { toggleFavorite(propertyId) }
    }

    fun onRetry() {
        _retryTrigger.value++
    }

    companion object {
        const val KEY_PROPERTY_ID = "propertyId"
    }
}
