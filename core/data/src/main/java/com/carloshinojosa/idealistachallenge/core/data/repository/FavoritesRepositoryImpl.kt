package com.carloshinojosa.idealistachallenge.core.data.repository

import com.carloshinojosa.idealistachallenge.core.domain.datasource.LocalFavoritesDataSource
import com.carloshinojosa.idealistachallenge.core.domain.model.Favorite
import com.carloshinojosa.idealistachallenge.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    private val localDataSource: LocalFavoritesDataSource,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<Favorite>> = localDataSource.observeAll()

    override suspend fun toggle(id: String, at: Instant) {
        if (localDataSource.findById(id) == null) {
            localDataSource.upsert(id, at)
        } else {
            localDataSource.deleteById(id)
        }
    }

    override suspend fun get(id: String): Favorite? = localDataSource.findById(id)
}
