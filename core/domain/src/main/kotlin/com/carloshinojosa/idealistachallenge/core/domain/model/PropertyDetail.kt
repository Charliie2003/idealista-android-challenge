package com.carloshinojosa.idealistachallenge.core.domain.model

/**
 * A [Property] enriched with data from the detail endpoint when available.
 * [isEnriched] is false when the static detail endpoint returned a different property — see ADR-0002.
 */
data class PropertyDetail(
    val property: Property,
    val isEnriched: Boolean,
    val description: String,
    val moreCharacteristics: MoreCharacteristics?,
    val energyCertification: EnergyCertification?,
    val images: List<ImageItem>,
)
