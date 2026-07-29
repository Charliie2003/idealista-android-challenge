package com.carloshinojosa.idealistachallenge.core.domain.datasource

import com.carloshinojosa.idealistachallenge.core.domain.model.Favorite
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Domain port: defines what the data layer must provide for local favorites persistence.
 * Implementation lives in :core:database to maintain the Dependency Inversion Principle.
 */
interface LocalFavoritesDataSource {
    fun observeAll(): Flow<List<Favorite>>
    suspend fun upsert(propertyId: String, at: Instant)
    suspend fun findById(id: String): Favorite?
    suspend fun deleteById(id: String)
}
