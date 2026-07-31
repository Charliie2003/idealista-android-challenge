package com.carloshinojosa.idealistachallenge.detail.presentation.model

data class PropertyDetailUiModel(
    val id: String,
    val operationLabel: String,
    val statusLabel: String?,
    val priceLabel: String,
    val priceSuffix: String,
    val title: String,
    val neighborhood: String,
    val district: String,
    val municipality: String,
    val images: List<ImageUiModel>,
    val highlights: Highlights,
    val description: String,
    val characteristics: List<CharacteristicUiModel>,
    val energyCertification: EnergyUiModel?,
    val communityCostsLabel: String?,
    val latitude: Double,
    val longitude: Double,
) {
    data class Highlights(
        val sizeLabel: String,
        val rooms: Int,
        val bathrooms: Int,
        val floorLabel: String,
    )
}

data class EnergyUiModel(
    val letter: String,
    val activeIndex: Int,
)
