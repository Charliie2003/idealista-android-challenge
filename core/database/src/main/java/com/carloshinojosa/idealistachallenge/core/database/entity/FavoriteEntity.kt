package com.carloshinojosa.idealistachallenge.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val propertyId: String,
    val favoritedAt: Long,
)
