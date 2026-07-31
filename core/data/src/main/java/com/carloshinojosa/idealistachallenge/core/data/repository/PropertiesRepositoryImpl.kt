package com.carloshinojosa.idealistachallenge.core.data.repository

import com.carloshinojosa.idealistachallenge.core.data.cache.PropertiesMemoryCache
import com.carloshinojosa.idealistachallenge.core.data.dispatcher.DispatcherProvider
import com.carloshinojosa.idealistachallenge.core.domain.datasource.RemotePropertiesDataSource
import com.carloshinojosa.idealistachallenge.core.domain.error.DomainError
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetail
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetailEnrichment
import com.carloshinojosa.idealistachallenge.core.domain.repository.PropertiesRepository
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import com.carloshinojosa.idealistachallenge.core.domain.util.getOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class PropertiesRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemotePropertiesDataSource,
    private val cache: PropertiesMemoryCache,
    private val dispatchers: DispatcherProvider,
) : PropertiesRepository {

    private val mutex = Mutex()

    override fun observeProperties(): Flow<Result<List<Property>>> = flow {
        val result = remoteDataSource.fetchProperties()
        if (result is Result.Success) cache.replace(result.data)
        emit(result)
    }.flowOn(dispatchers.io)

    override suspend fun getDetail(id: String): Result<PropertyDetail> {
        val base = when (val r = resolveBaseProperty(id)) {
            is Result.Success -> r.data
            is Result.Error   -> return r
        }
        val enrichment = remoteDataSource.fetchPropertyDetailEnrichment().getOrNull()
        return Result.Success(
            // See ADR-0002: the detail endpoint always returns adid=1 — only enrich the matching property.
            if (enrichment != null && enrichment.id == id) base.enrichWith(enrichment)
            else base.toBaseDetail()
        )
    }

    // Looks up cache first; falls back to a network fetch under a Mutex to avoid stampeding.
    private suspend fun resolveBaseProperty(id: String): Result<Property> {
        cache.get(id)?.let { return Result.Success(it) }

        mutex.withLock {
            // Double-check after acquiring the lock — a concurrent caller may have already populated it.
            cache.get(id)?.let { return Result.Success(it) }

            when (val result = remoteDataSource.fetchProperties()) {
                is Result.Success -> cache.replace(result.data)
                is Result.Error   -> return result
            }
        }

        return cache.get(id)
            ?.let { Result.Success(it) }
            ?: Result.Error(DomainError.NotFound)
    }

    private fun Property.toBaseDetail() = PropertyDetail(
        property = this,
        isEnriched = false,
        description = description,
        moreCharacteristics = null,
        energyCertification = null,
        images = images,
    )

    private fun Property.enrichWith(e: PropertyDetailEnrichment) = PropertyDetail(
        property = this,
        isEnriched = true,
        description = e.propertyComment,
        moreCharacteristics = e.moreCharacteristics,
        energyCertification = e.energyCertification,
        images = e.detailImages.ifEmpty { images },
    )
}
