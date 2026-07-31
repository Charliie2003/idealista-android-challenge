package com.carloshinojosa.idealistachallenge.detail

import com.carloshinojosa.idealistachallenge.core.domain.model.EnergyCertification
import com.carloshinojosa.idealistachallenge.core.domain.model.Favorite
import com.carloshinojosa.idealistachallenge.core.domain.model.ImageItem
import com.carloshinojosa.idealistachallenge.core.domain.model.MoreCharacteristics
import com.carloshinojosa.idealistachallenge.core.domain.model.PriceInfo
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetail
import java.time.Instant

/**
 * Module-local fixtures for [PropertyDetail] and related domain objects.
 * Named parameters with defaults keep each test minimal. Do not share across modules.
 */
internal object DetailFixtures {

    fun propertyDetail(
        propertyId: String = "p-001",
        operation: String = Property.OPERATION_SALE,
        price: Double = 250_000.0,
        isEnriched: Boolean = true,
        energyCertificationType: String = "E",
    ): PropertyDetail = PropertyDetail(
        property = Property(
            id = propertyId,
            thumbnail = "https://cdn.example.com/thumb.jpg",
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
            isFavorited = false,
            favoritedAt = null,
        ),
        isEnriched = isEnriched,
        description = "Descripción larga del inmueble.",
        moreCharacteristics = if (isEnriched) moreCharacteristics() else null,
        energyCertification = if (isEnriched) {
            EnergyCertification(
                title = "Consumo",
                energyConsumptionType = energyCertificationType,
                emissionsType = "D",
            )
        } else null,
        images = listOf(
            ImageItem(url = "https://cdn.example.com/salon.jpg", tag = "salon", localizedName = "Salon"),
        ),
    )

    fun moreCharacteristics(
        communityCosts: Double = 330.0,
    ): MoreCharacteristics = MoreCharacteristics(
        communityCosts = communityCosts,
        roomNumber = 3,
        bathNumber = 2,
        exterior = true,
        housingFurnitures = "furnished",
        agencyIsABank = false,
        energyCertificationType = "E",
        flatLocation = "exterior",
        modificationDate = 1_700_000_000L,
        constructedArea = 133,
        lift = true,
        boxroom = true,
        isDuplex = false,
        floor = "2",
        status = "good",
    )

    fun favorite(
        propertyId: String = "p-001",
        favoritedAt: Instant = Instant.parse("2026-07-28T10:00:00Z"),
    ): Favorite = Favorite(
        propertyId = propertyId,
        favoritedAt = favoritedAt,
    )
}
