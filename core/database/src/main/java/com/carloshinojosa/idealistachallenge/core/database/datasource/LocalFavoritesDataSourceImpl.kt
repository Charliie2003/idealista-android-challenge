package com.carloshinojosa.idealistachallenge.core.database.datasource

import com.carloshinojosa.idealistachallenge.core.database.dao.FavoritesDao
import com.carloshinojosa.idealistachallenge.core.database.mapper.toDomain
import com.carloshinojosa.idealistachallenge.core.database.mapper.toEntity
import com.carloshinojosa.idealistachallenge.core.domain.datasource.LocalFavoritesDataSource
import com.carloshinojosa.idealistachallenge.core.domain.model.Favorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class LocalFavoritesDataSourceImpl @Inject constructor(
    private val dao: FavoritesDao,
) : LocalFavoritesDataSource {

    override fun observeAll(): Flow<List<Favorite>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsert(propertyId: String, at: Instant) {
        dao.upsert(toEntity(propertyId, at))
    }

    override suspend fun findById(id: String): Favorite? =
        dao.findById(id)?.toDomain()

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }
}
