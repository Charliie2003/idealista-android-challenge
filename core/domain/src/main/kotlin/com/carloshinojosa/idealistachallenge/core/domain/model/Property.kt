package com.carloshinojosa.idealistachallenge.core.domain.model

import java.time.Instant

/** Domain model for a listing property, optionally overlaid with favorite state. */
data class Property(
    val id: String,
    val thumbnail: String?,
    val floor: String?,
    val priceInfo: PriceInfo,
    val propertyType: String,
    val operation: String,
    val size: Double,
    val exterior: Boolean,
    val rooms: Int,
    val bathrooms: Int,
    val address: String,
    val province: String,
    val municipality: String,
    val district: String,
    val country: String,
    val neighborhood: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val images: List<ImageItem>,
    val hasParkingSpace: Boolean,
    val isParkingSpaceIncludedInPrice: Boolean,
    val hasAirConditioning: Boolean,
    val hasBoxRoom: Boolean,
    val hasSwimmingPool: Boolean,
    val hasTerrace: Boolean,
    val hasGarden: Boolean,
    val isFavorited: Boolean = false,
    val favoritedAt: Instant? = null,
)
