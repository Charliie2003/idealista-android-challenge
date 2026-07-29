package com.carloshinojosa.idealistachallenge.core.network.api

import com.carloshinojosa.idealistachallenge.core.network.dto.PriceDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PriceInfoListingDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PropertyDetailDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PropertyDto

internal class FakeIdealistaApi(
    private val properties: List<PropertyDto> = listOf(
        PropertyDto(
            propertyCode = "1",
            priceInfo = PriceInfoListingDto(PriceDto(amount = 500000.0, currencySuffix = "€")),
        )
    ),
    private val detail: PropertyDetailDto = PropertyDetailDto(adid = 1),
) : IdealistaApi {
    override suspend fun getProperties(): List<PropertyDto> = properties
    override suspend fun getDetail(): PropertyDetailDto = detail
}
