package com.carloshinojosa.idealistachallenge.core.testing

import com.carloshinojosa.idealistachallenge.core.domain.model.Favorite
import com.carloshinojosa.idealistachallenge.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

/**
 * In-memory [FavoritesRepository] for unit tests. Implements the same true-toggle semantics
 * as the production [FavoritesRepositoryImpl]: delete-if-exists, add-if-not.
 */
class FakeFavoritesRepository : FavoritesRepository {
    private val storage = mutableMapOf<String, Favorite>()
    private val _flow = MutableStateFlow<List<Favorite>>(emptyList())

    override fun observeFavorites(): Flow<List<Favorite>> = _flow

    override suspend fun get(id: String): Favorite? = storage[id]

    override suspend fun toggle(id: String, at: Instant) {
        if (storage.containsKey(id)) {
            storage.remove(id)
        } else {
            storage[id] = Favorite(id, at)
        }
        _flow.value = storage.values.toList()
    }
}
