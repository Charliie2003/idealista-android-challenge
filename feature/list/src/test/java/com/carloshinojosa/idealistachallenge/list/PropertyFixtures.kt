package com.carloshinojosa.idealistachallenge.list

import com.carloshinojosa.idealistachallenge.core.domain.model.ImageItem
import com.carloshinojosa.idealistachallenge.core.domain.model.PriceInfo
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import java.time.Instant

/**
 * Module-local fixtures for [Property]. Named parameters with defaults keep each test minimal
 * while remaining readable. Do not share these across modules.
 */
internal object PropertyFixtures {

    fun sale(
        id: String = "p-sale-001",
        price: Double = 250_000.0,
        thumbnail: String? = "https://cdn.example.com/thumb.jpg",
        isFavorited: Boolean = false,
        favoritedAt: Instant? = null,
    ): Property = build(
        id = id,
        price = price,
        operation = Property.OPERATION_SALE,
        thumbnail = thumbnail,
        isFavorited = isFavorited,
        favoritedAt = favoritedAt,
    )

    fun rent(
        id: String = "p-rent-001",
        price: Double = 1_200.0,
    ): Property = build(
        id = id,
        price = price,
        operation = Property.OPERATION_RENT,
    )

    fun favoritedSale(
        id: String = "p-fav-001",
        favoritedAt: Instant = Instant.parse("2025-11-04T12:00:00Z"),
    ): Property = sale(id = id, isFavorited = true, favoritedAt = favoritedAt)

    private fun build(
        id: String,
        price: Double,
        operation: String,
        thumbnail: String? = "https://cdn.example.com/thumb.jpg",
        isFavorited: Boolean = false,
        favoritedAt: Instant? = null,
    ): Property = Property(
        id = id,
        thumbnail = thumbnail,
        floor = "2",
        priceInfo = PriceInfo(amount = price, currencySuffix = "€"),
        propertyType = "flat",
        operation = operation,
        size = 80.0,
        exterior = true,
        rooms = 3,
        bathrooms = 1,
        address = "Calle Mayor 1",
        province = "Madrid",
        municipality = "Madrid",
        district = "Centro",
        country = "ES",
        neighborhood = "Sol",
        latitude = 40.416775,
        longitude = -3.703790,
        description = "Bonito piso",
        images = listOf(
            ImageItem(url = "https://cdn.example.com/img.jpg", tag = "general", localizedName = null),
        ),
        hasParkingSpace = false,
        isParkingSpaceIncludedInPrice = false,
        hasAirConditioning = false,
        hasBoxRoom = false,
        hasSwimmingPool = false,
        hasTerrace = false,
        hasGarden = false,
        isFavorited = isFavorited,
        favoritedAt = favoritedAt,
    )
}
