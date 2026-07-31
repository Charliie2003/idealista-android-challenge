package com.carloshinojosa.idealistachallenge.core.testing

import com.carloshinojosa.idealistachallenge.core.domain.error.DomainError
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetail
import com.carloshinojosa.idealistachallenge.core.domain.repository.PropertiesRepository
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [PropertiesRepository] for unit tests. Tests push values directly into
 * [propertiesFlow] to simulate network/cache emissions.
 */
class FakePropertiesRepository(
    val propertiesFlow: MutableStateFlow<Result<List<Property>>> = MutableStateFlow(Result.Success(emptyList())),
    private var detailResult: Result<PropertyDetail> = Result.Error(DomainError.NotFound),
) : PropertiesRepository {
    override fun observeProperties(): Flow<Result<List<Property>>> = propertiesFlow

    override suspend fun getDetail(id: String): Result<PropertyDetail> = detailResult

    fun setDetailResult(result: Result<PropertyDetail>) {
        detailResult = result
    }
}
