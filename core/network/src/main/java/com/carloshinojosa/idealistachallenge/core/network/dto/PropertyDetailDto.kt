package com.carloshinojosa.idealistachallenge.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PropertyDetailDto(
    val adid: Int,
    val price: Double = 0.0,
    val priceInfo: PriceInfoDetailDto = PriceInfoDetailDto(),
    val operation: String = "",
    val propertyType: String = "",
    val extendedPropertyType: String = "",
    val homeType: String = "",
    val state: String = "",
    val multimedia: MultimediaDto = MultimediaDto(),
    val propertyComment: String = "",
    val ubication: UbicationDto = UbicationDto(),
    val country: String = "",
    val moreCharacteristics: MoreCharacteristicsDto = MoreCharacteristicsDto(),
    val energyCertification: EnergyCertificationDto = EnergyCertificationDto(),
)

@Serializable
data class PriceInfoDetailDto(
    val amount: Double = 0.0,
    val currencySuffix: String = "€",
)

@Serializable
data class UbicationDto(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)

@Serializable
data class MoreCharacteristicsDto(
    val communityCosts: Double = 0.0,
    val roomNumber: Int = 0,
    val bathNumber: Int = 0,
    val exterior: Boolean = false,
    val housingFurnitures: String = "",
    val agencyIsABank: Boolean = false,
    val energyCertificationType: String = "",
    val flatLocation: String = "",
    val modificationDate: Long = 0L,
    val constructedArea: Int = 0,
    val lift: Boolean = false,
    val boxroom: Boolean = false,
    val isDuplex: Boolean = false,
    val floor: String = "",
    val status: String = "",
)

@Serializable
data class EnergyCertificationDto(
    val title: String = "",
    val energyConsumption: EnergyTypeDto = EnergyTypeDto(),
    val emissions: EnergyTypeDto = EnergyTypeDto(),
)

@Serializable
data class EnergyTypeDto(val type: String = "")
