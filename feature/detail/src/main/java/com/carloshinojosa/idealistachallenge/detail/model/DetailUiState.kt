package com.carloshinojosa.idealistachallenge.detail.model

import com.carloshinojosa.idealistachallenge.core.domain.util.UiText

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Content(
        val property: PropertyDetailUiModel,
        val isFavorite: Boolean,
        val favoritedDateLabel: String?,
    ) : DetailUiState
    data class Error(val message: UiText) : DetailUiState
}
