package com.carloshinojosa.idealistachallenge.core.domain.usecase

import com.carloshinojosa.idealistachallenge.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Returns a [Flow] that emits `true` whenever the given property is currently favorited. */
class IsFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(propertyId: String): Flow<Boolean> =
        favoritesRepository.observeFavorites()
            .map { favorites -> favorites.any { it.propertyId == propertyId } }
}
