package com.carloshinojosa.idealistachallenge.list.presentation

import com.carloshinojosa.idealistachallenge.design.ui.theme.UiText
import com.carloshinojosa.idealistachallenge.list.presentation.model.PropertyCardUiModel

sealed interface ListingUiState {
    data object Loading : ListingUiState
    data class Content(val items: List<PropertyCardUiModel>) : ListingUiState
    data object Empty : ListingUiState
    data class Error(val message: UiText) : ListingUiState
}
