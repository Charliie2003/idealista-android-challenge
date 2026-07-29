package com.carloshinojosa.idealistachallenge.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.carloshinojosa.idealistachallenge.core.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM favorites")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Upsert
    suspend fun upsert(entity: FavoriteEntity)

    @Query("SELECT * FROM favorites WHERE propertyId = :id")
    suspend fun findById(id: String): FavoriteEntity?

    @Query("DELETE FROM favorites WHERE propertyId = :id")
    suspend fun deleteById(id: String)
}
