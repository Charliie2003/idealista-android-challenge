package com.carloshinojosa.idealistachallenge.list.presentation.model

import com.carloshinojosa.idealistachallenge.core.domain.util.UiText

sealed interface ListingUiState {
    data object Loading : ListingUiState
    data class Content(val items: List<PropertyCardUiModel>) : ListingUiState
    data object Empty : ListingUiState
    data class Error(val message: UiText) : ListingUiState
}
