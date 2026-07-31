package com.carloshinojosa.idealistachallenge.core.domain.usecase

import com.carloshinojosa.idealistachallenge.core.domain.model.Favorite
import com.carloshinojosa.idealistachallenge.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Returns a [Flow] that emits the [Favorite] entry for the given property when it is favorited,
 * or `null` when it is not. Callers that only need a boolean can map with `{ it != null }`.
 */
class IsFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(propertyId: String): Flow<Favorite?> =
        favoritesRepository.observeFavorites()
            .map { favorites -> favorites.find { it.propertyId == propertyId } }
}
