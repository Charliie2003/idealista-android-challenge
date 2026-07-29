package com.carloshinojosa.idealistachallenge.core.domain.usecase

import com.carloshinojosa.idealistachallenge.core.domain.repository.FavoritesRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/**
 * Toggles a property's favorite state. Re-favoriting updates the timestamp — see ADR-0006.
 * The add-vs-delete decision is delegated to [FavoritesRepository.toggle].
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(propertyId: String) {
        favoritesRepository.toggle(propertyId, Instant.now(clock))
    }
}
