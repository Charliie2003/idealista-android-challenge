package com.carloshinojosa.idealistachallenge.core.network.di

import com.carloshinojosa.idealistachallenge.core.domain.datasource.RemotePropertiesDataSource
import com.carloshinojosa.idealistachallenge.core.network.datasource.RemotePropertiesDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindingsModule {

    @Binds @Singleton
    abstract fun bindRemoteDataSource(impl: RemotePropertiesDataSourceImpl): RemotePropertiesDataSource
}
