package com.carloshinojosa.idealistachallenge.core.network.mapper

import com.carloshinojosa.idealistachallenge.core.domain.model.ImageItem
import com.carloshinojosa.idealistachallenge.core.domain.model.PriceInfo
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.network.dto.PropertyDto

internal fun PropertyDto.toDomain(): Property = Property(
    id = propertyCode,
    thumbnail = thumbnail,
    floor = floor,
    // Source of truth is priceInfo — top-level price field disagrees on rental properties (Quirk 1)
    priceInfo = PriceInfo(
        amount = priceInfo.price.amount,
        currencySuffix = priceInfo.price.currencySuffix,
    ),
    propertyType = propertyType,
    operation = operation,
    size = size,
    exterior = exterior,
    rooms = rooms,
    bathrooms = bathrooms,
    address = address,
    province = province,
    municipality = municipality,
    district = district,
    country = country,
    neighborhood = neighborhood,
    latitude = latitude,
    longitude = longitude,
    description = description,
    images = multimedia.images.map { img ->
        ImageItem(url = img.url, tag = img.tag, localizedName = img.localizedName)
    },
    hasParkingSpace = parkingSpace?.hasParkingSpace ?: false,
    isParkingSpaceIncludedInPrice = parkingSpace?.isParkingSpaceIncludedInPrice ?: false,
    hasAirConditioning = features.hasAirConditioning,
    hasBoxRoom = features.hasBoxRoom,
    hasSwimmingPool = features.hasSwimmingPool,
    hasTerrace = features.hasTerrace,
    hasGarden = features.hasGarden,
)
