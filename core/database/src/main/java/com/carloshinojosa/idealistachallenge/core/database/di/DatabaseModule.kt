package com.carloshinojosa.idealistachallenge.core.database.di

import android.content.Context
import androidx.room.Room
import com.carloshinojosa.idealistachallenge.core.database.dao.FavoritesDao
import com.carloshinojosa.idealistachallenge.core.database.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        // v1 schema — no migrations for this challenge
        Room.databaseBuilder(context, AppDatabase::class.java, "idealista.db")
            .build()

    @Provides
    @Singleton
    fun provideFavoritesDao(database: AppDatabase): FavoritesDao =
        database.favoritesDao()
}
