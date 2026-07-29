package com.carloshinojosa.idealistachallenge.core.domain.usecase

import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetail
import com.carloshinojosa.idealistachallenge.core.domain.repository.PropertiesRepository
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import javax.inject.Inject

/**
 * One-shot fetch for a property's enriched detail.
 * Returns [Result] rather than [Flow] — detail is loaded once on screen entry; the ViewModel
 * observes [IsFavoriteUseCase] separately for live favorite state.
 * See ADR-0002 for static-endpoint handling.
 */
class ObservePropertyDetailUseCase @Inject constructor(
    private val propertiesRepository: PropertiesRepository,
) {
    suspend operator fun invoke(propertyId: String): Result<PropertyDetail> =
        propertiesRepository.getDetail(propertyId)
}
