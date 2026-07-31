package com.carloshinojosa.idealistachallenge.detail.presentation

import com.carloshinojosa.idealistachallenge.design.ui.theme.UiText
import com.carloshinojosa.idealistachallenge.detail.presentation.model.PropertyDetailUiModel

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Content(
        val property: PropertyDetailUiModel,
        val isFavorite: Boolean,
        val favoritedDateLabel: String?,
    ) : DetailUiState
    data class Error(val message: UiText) : DetailUiState
}
