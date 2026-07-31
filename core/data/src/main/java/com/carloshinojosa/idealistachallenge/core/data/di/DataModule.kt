package com.carloshinojosa.idealistachallenge.core.data.di

import com.carloshinojosa.idealistachallenge.core.data.cache.InMemoryPropertiesCache
import com.carloshinojosa.idealistachallenge.core.data.cache.PropertiesMemoryCache
import com.carloshinojosa.idealistachallenge.core.data.dispatcher.DefaultDispatcherProvider
import com.carloshinojosa.idealistachallenge.core.data.dispatcher.DispatcherProvider
import com.carloshinojosa.idealistachallenge.core.data.repository.FavoritesRepositoryImpl
import com.carloshinojosa.idealistachallenge.core.data.repository.PropertiesRepositoryImpl
import com.carloshinojosa.idealistachallenge.core.domain.repository.FavoritesRepository
import com.carloshinojosa.idealistachallenge.core.domain.repository.PropertiesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton
    abstract fun bindPropertiesRepository(impl: PropertiesRepositoryImpl): PropertiesRepository

    @Binds @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    @Binds @Singleton
    abstract fun bindPropertiesCache(impl: InMemoryPropertiesCache): PropertiesMemoryCache
}
