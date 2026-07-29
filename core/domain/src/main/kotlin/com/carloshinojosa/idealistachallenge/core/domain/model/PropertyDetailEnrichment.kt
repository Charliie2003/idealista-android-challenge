package com.carloshinojosa.idealistachallenge.core.domain.model

/**
 * Extra data from the detail endpoint used to enrich a base [Property].
 * The [id] is `adid.toString()` and is compared against the requested property ID — see ADR-0002.
 */
data class PropertyDetailEnrichment(
    val id: String,
    val propertyComment: String,
    val detailLatitude: Double,
    val detailLongitude: Double,
    val moreCharacteristics: MoreCharacteristics,
    val energyCertification: EnergyCertification,
    val detailImages: List<ImageItem>,
)
