package com.carloshinojosa.idealistachallenge.core.domain.usecase

import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.repository.FavoritesRepository
import com.carloshinojosa.idealistachallenge.core.domain.repository.PropertiesRepository
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import com.carloshinojosa.idealistachallenge.core.domain.util.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Combines the properties list with live favorite state into a single observable stream. */
class GetPropertyDetailUseCase @Inject constructor(
    private val propertiesRepository: PropertiesRepository,
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(): Flow<Result<List<Property>>> =
        combine(
            propertiesRepository.observeProperties(),
            favoritesRepository.observeFavorites(),
        ) { propertiesResult, favorites ->
            propertiesResult.map { properties ->
                val favMap = favorites.associateBy { it.propertyId }
                properties.map { p ->
                    p.copy(
                        isFavorited = favMap.containsKey(p.id),
                        favoritedAt = favMap[p.id]?.favoritedAt,
                    )
                }
            }
        }
}
