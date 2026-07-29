package com.carloshinojosa.idealistachallenge.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PropertyDto(
    val propertyCode: String,
    val thumbnail: String? = null,
    val floor: String? = null,
    val price: Double = 0.0,
    val priceInfo: PriceInfoListingDto,
    val propertyType: String = "",
    val operation: String = "",
    val size: Double = 0.0,
    val exterior: Boolean = false,
    val rooms: Int = 0,
    val bathrooms: Int = 0,
    val address: String = "",
    val province: String = "",
    val municipality: String = "",
    val district: String = "",
    val country: String = "",
    val neighborhood: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val multimedia: MultimediaDto = MultimediaDto(),
    val parkingSpace: ParkingSpaceDto? = null,
    val features: FeaturesDto = FeaturesDto(),
)

@Serializable
data class PriceInfoListingDto(val price: PriceDto)

@Serializable
data class PriceDto(
    val amount: Double = 0.0,
    val currencySuffix: String = "€",
)

@Serializable
data class MultimediaDto(
    val images: List<ImageDto> = emptyList(),
)

@Serializable
data class ImageDto(
    val url: String,
    val tag: String = "",
    val localizedName: String? = null,
    val multimediaId: Long? = null,
)

@Serializable
data class ParkingSpaceDto(
    val hasParkingSpace: Boolean = false,
    val isParkingSpaceIncludedInPrice: Boolean = false,
)

@Serializable
data class FeaturesDto(
    val hasAirConditioning: Boolean = false,
    val hasBoxRoom: Boolean = false,
    val hasSwimmingPool: Boolean = false,
    val hasTerrace: Boolean = false,
    val hasGarden: Boolean = false,
)
