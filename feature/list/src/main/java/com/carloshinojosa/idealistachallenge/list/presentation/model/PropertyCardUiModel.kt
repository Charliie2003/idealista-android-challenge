package com.carloshinojosa.idealistachallenge.list.presentation.model

/**
 * UI model for a single property card in the listing screen.
 * Produced by [com.carloshinojosa.idealistachallenge.list.presentation.mapper.PropertyMapper]; never mutated by the UI.
 */
data class PropertyCardUiModel(
    val id: String,
    val thumbnailUrl: String,
    val operationType: String,
    val operationLabel: String,
    val priceAmountText: String,
    val priceSuffixText: String,
    val neighborhood: String,
    val district: String,
    val rooms: Int,
    val bathrooms: Int,
    val sizeLabel: String,
    val isFavorite: Boolean,
    val favoritedDateLabel: String?,
    val isNew: Boolean,
)
