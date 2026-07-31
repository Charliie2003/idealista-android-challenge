package com.carloshinojosa.idealistachallenge

import com.carloshinojosa.idealistachallenge.core.network.api.IdealistaApi
import com.carloshinojosa.idealistachallenge.core.network.di.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkModule::class])
object TestNetworkModule {

    @Provides
    @Singleton
    fun provideFakeApi(): IdealistaApi = FakeIdealistaApi()
}
