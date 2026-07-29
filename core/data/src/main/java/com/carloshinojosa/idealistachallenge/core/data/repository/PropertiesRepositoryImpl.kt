package com.carloshinojosa.idealistachallenge.core.data.repository

import com.carloshinojosa.idealistachallenge.core.data.dispatcher.DispatcherProvider
import com.carloshinojosa.idealistachallenge.core.domain.error.DomainError
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetail
import com.carloshinojosa.idealistachallenge.core.domain.repository.PropertiesRepository
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import com.carloshinojosa.idealistachallenge.core.domain.util.getOrNull
import com.carloshinojosa.idealistachallenge.core.domain.datasource.RemotePropertiesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PropertiesRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemotePropertiesDataSource,
    private val dispatchers: DispatcherProvider,
) : PropertiesRepository {

    override fun observeProperties(): Flow<Result<List<Property>>> = flow {
        emit(remoteDataSource.fetchProperties())
    }.flowOn(dispatchers.io)

    override suspend fun getDetail(id: String): Result<PropertyDetail> {
        val propertiesResult = remoteDataSource.fetchProperties()
        val properties = propertiesResult.getOrNull()
            ?: return Result.Error((propertiesResult as Result.Error).error)

        val base = properties.find { it.id == id }
            ?: return Result.Error(DomainError.Http(404))

        val enrichment = remoteDataSource.fetchPropertyDetailEnrichment().getOrNull()

        // See ADR-0002: the detail endpoint always returns adid=1 — only enrich the matching property.
        return if (enrichment != null && enrichment.id == id) {
            Result.Success(
                PropertyDetail(
                    property = base,
                    isEnriched = true,
                    description = enrichment.propertyComment,
                    moreCharacteristics = enrichment.moreCharacteristics,
                    energyCertification = enrichment.energyCertification,
                    images = enrichment.detailImages.ifEmpty { base.images },
                )
            )
        } else {
            Result.Success(
                PropertyDetail(
                    property = base,
                    isEnriched = false,
                    description = base.description,
                    moreCharacteristics = null,
                    energyCertification = null,
                    images = base.images,
                )
            )
        }
    }
}
