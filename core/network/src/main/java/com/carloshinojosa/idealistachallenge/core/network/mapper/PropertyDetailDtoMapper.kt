package com.carloshinojosa.idealistachallenge.core.network.mapper

import com.carloshinojosa.idealistachallenge.core.domain.model.EnergyCertification
import com.carloshinojosa.idealistachallenge.core.domain.model.ImageItem
import com.carloshinojosa.idealistachallenge.core.domain.model.MoreCharacteristics
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetailEnrichment
import com.carloshinojosa.idealistachallenge.core.network.dto.PropertyDetailDto

internal fun PropertyDetailDto.toDomain(): PropertyDetailEnrichment = PropertyDetailEnrichment(
    id = adid.toString(),
    propertyComment = propertyComment,
    detailLatitude = ubication.latitude,
    detailLongitude = ubication.longitude,
    moreCharacteristics = MoreCharacteristics(
        communityCosts = moreCharacteristics.communityCosts,
        roomNumber = moreCharacteristics.roomNumber,
        bathNumber = moreCharacteristics.bathNumber,
        exterior = moreCharacteristics.exterior,
        housingFurnitures = moreCharacteristics.housingFurnitures,
        agencyIsABank = moreCharacteristics.agencyIsABank,
        energyCertificationType = moreCharacteristics.energyCertificationType,
        flatLocation = moreCharacteristics.flatLocation,
        modificationDate = moreCharacteristics.modificationDate,
        constructedArea = moreCharacteristics.constructedArea,
        lift = moreCharacteristics.lift,
        boxroom = moreCharacteristics.boxroom,
        isDuplex = moreCharacteristics.isDuplex,
        floor = moreCharacteristics.floor,
        status = moreCharacteristics.status,
    ),
    energyCertification = EnergyCertification(
        title = energyCertification.title,
        energyConsumptionType = energyCertification.energyConsumption.type,
        emissionsType = energyCertification.emissions.type,
    ),
    detailImages = multimedia.images.map { img ->
        ImageItem(url = img.url, tag = img.tag, localizedName = img.localizedName)
    },
)
