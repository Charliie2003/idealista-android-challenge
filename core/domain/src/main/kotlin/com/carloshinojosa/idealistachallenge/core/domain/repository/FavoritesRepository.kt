package com.carloshinojosa.idealistachallenge.core.domain.repository

import com.carloshinojosa.idealistachallenge.core.domain.model.Favorite
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/** Contract for the favorites repository. Implementation lives in :app. */
interface FavoritesRepository {
    fun observeFavorites(): Flow<List<Favorite>>
    suspend fun toggle(id: String, at: Instant)
    suspend fun get(id: String): Favorite?
}
