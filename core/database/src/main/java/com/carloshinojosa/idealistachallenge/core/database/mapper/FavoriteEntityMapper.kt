package com.carloshinojosa.idealistachallenge.core.database.mapper

import com.carloshinojosa.idealistachallenge.core.database.entity.FavoriteEntity
import com.carloshinojosa.idealistachallenge.core.domain.model.Favorite
import java.time.Instant

internal fun FavoriteEntity.toDomain(): Favorite = Favorite(
    propertyId = propertyId,
    favoritedAt = Instant.ofEpochMilli(favoritedAt),
)

internal fun toEntity(propertyId: String, at: Instant): FavoriteEntity = FavoriteEntity(
    propertyId = propertyId,
    favoritedAt = at.toEpochMilli(),
)
