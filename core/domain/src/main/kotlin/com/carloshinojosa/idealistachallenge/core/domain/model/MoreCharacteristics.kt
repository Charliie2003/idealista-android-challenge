package com.carloshinojosa.idealistachallenge.core.domain.model

data class MoreCharacteristics(
    val communityCosts: Double,
    val roomNumber: Int,
    val bathNumber: Int,
    val exterior: Boolean,
    val housingFurnitures: String,
    val agencyIsABank: Boolean,
    val energyCertificationType: String,
    val flatLocation: String,
    val modificationDate: Long,
    val constructedArea: Int,
    val lift: Boolean,
    val boxroom: Boolean,
    val isDuplex: Boolean,
    val floor: String,
    val status: String,
)
