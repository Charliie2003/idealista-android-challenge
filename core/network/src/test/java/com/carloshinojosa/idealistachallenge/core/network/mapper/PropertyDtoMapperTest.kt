package com.carloshinojosa.idealistachallenge.core.network.mapper

import com.carloshinojosa.idealistachallenge.core.network.dto.FeaturesDto
import com.carloshinojosa.idealistachallenge.core.network.dto.MultimediaDto
import com.carloshinojosa.idealistachallenge.core.network.dto.ParkingSpaceDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PriceDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PriceInfoListingDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PropertyDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PropertyDtoMapperTest {

    @Test
    fun `maps propertyCode to domain id`() {
        val dto = minimalDto(propertyCode = "42")
        assertEquals("42", dto.toDomain().id)
    }

    @Test
    fun `ignores top-level price when priceInfo disagrees`() {
        // Quirk 1: top-level price is 2_750_000 but the property is a rental at 1_200 per month
        val dto = minimalDto(
            topLevelPrice = 2_750_000.0,
            priceAmount = 1_200.0,
            currencySuffix = "€/mes",
        )
        val domain = dto.toDomain()
        assertEquals(1_200.0, domain.priceInfo.amount, 0.0)
        assertEquals("€/mes", domain.priceInfo.currencySuffix)
    }

    @Test
    fun `absent parkingSpace defaults to false`() {
        val dto = minimalDto(parkingSpace = null)
        val domain = dto.toDomain()
        assertFalse(domain.hasParkingSpace)
        assertFalse(domain.isParkingSpaceIncludedInPrice)
    }

    @Test
    fun `present parkingSpace maps correctly`() {
        val dto = minimalDto(
            parkingSpace = ParkingSpaceDto(
                hasParkingSpace = true,
                isParkingSpaceIncludedInPrice = true,
            )
        )
        val domain = dto.toDomain()
        assertTrue(domain.hasParkingSpace)
        assertTrue(domain.isParkingSpaceIncludedInPrice)
    }

    @Test
    fun `optional feature flags default to false`() {
        val dto = minimalDto(features = FeaturesDto())
        val domain = dto.toDomain()
        assertFalse(domain.hasSwimmingPool)
        assertFalse(domain.hasTerrace)
        assertFalse(domain.hasGarden)
    }

    @Test
    fun `thumbnail null is preserved`() {
        val dto = minimalDto(thumbnail = null)
        assertNull(dto.toDomain().thumbnail)
    }

    private fun minimalDto(
        propertyCode: String = "1",
        topLevelPrice: Double = 500_000.0,
        priceAmount: Double = 500_000.0,
        currencySuffix: String = "€",
        thumbnail: String? = null,
        parkingSpace: ParkingSpaceDto? = null,
        features: FeaturesDto = FeaturesDto(),
    ) = PropertyDto(
        propertyCode = propertyCode,
        thumbnail = thumbnail,
        price = topLevelPrice,
        priceInfo = PriceInfoListingDto(PriceDto(amount = priceAmount, currencySuffix = currencySuffix)),
        multimedia = MultimediaDto(),
        parkingSpace = parkingSpace,
        features = features,
    )
}
