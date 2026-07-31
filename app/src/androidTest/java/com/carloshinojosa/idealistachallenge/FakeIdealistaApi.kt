package com.carloshinojosa.idealistachallenge

import com.carloshinojosa.idealistachallenge.core.network.api.IdealistaApi
import com.carloshinojosa.idealistachallenge.core.network.dto.MultimediaDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PriceDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PriceInfoListingDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PropertyDetailDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PropertyDto

class FakeIdealistaApi(
    private val properties: List<PropertyDto> = listOf(
        PropertyDto(
            propertyCode = "1",
            priceInfo = PriceInfoListingDto(PriceDto(amount = 500_000.0, currencySuffix = "€")),
            operation = "sale",
            multimedia = MultimediaDto(),
        ),
    ),
    private val detail: PropertyDetailDto = PropertyDetailDto(adid = 1),
) : IdealistaApi {
    override suspend fun getProperties(): List<PropertyDto> = properties
    override suspend fun getDetail(): PropertyDetailDto = detail
}
