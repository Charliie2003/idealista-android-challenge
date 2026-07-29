package com.carloshinojosa.idealistachallenge.core.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.carloshinojosa.idealistachallenge.core.database.dao.FavoritesDao
import com.carloshinojosa.idealistachallenge.core.database.entity.FavoriteEntity

/** v1 schema — no migrations required for this challenge. */
@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}
