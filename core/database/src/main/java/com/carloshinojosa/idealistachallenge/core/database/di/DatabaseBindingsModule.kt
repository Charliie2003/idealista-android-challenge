package com.carloshinojosa.idealistachallenge.core.database.di

import com.carloshinojosa.idealistachallenge.core.domain.datasource.LocalFavoritesDataSource
import com.carloshinojosa.idealistachallenge.core.database.datasource.LocalFavoritesDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseBindingsModule {

    @Binds @Singleton
    abstract fun bindLocalDataSource(impl: LocalFavoritesDataSourceImpl): LocalFavoritesDataSource
}
