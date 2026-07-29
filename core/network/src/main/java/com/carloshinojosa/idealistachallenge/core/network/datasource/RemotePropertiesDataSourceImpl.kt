package com.carloshinojosa.idealistachallenge.core.network.datasource

import com.carloshinojosa.idealistachallenge.core.domain.datasource.RemotePropertiesDataSource
import com.carloshinojosa.idealistachallenge.core.domain.error.DomainError
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetailEnrichment
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import com.carloshinojosa.idealistachallenge.core.network.api.IdealistaApi
import com.carloshinojosa.idealistachallenge.core.network.mapper.toDomain
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RemotePropertiesDataSourceImpl @Inject constructor(
    private val api: IdealistaApi,
) : RemotePropertiesDataSource {

    override suspend fun fetchProperties(): Result<List<Property>> = safeApiCall {
        api.getProperties().map { it.toDomain() }
    }

    override suspend fun fetchPropertyDetailEnrichment(): Result<PropertyDetailEnrichment> = safeApiCall {
        api.getDetail().toDomain()
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (e: HttpException) {
        Result.Error(DomainError.Http(e.code()))
    } catch (e: IOException) {
        Result.Error(DomainError.Network)
    } catch (e: Exception) {
        Result.Error(DomainError.Unknown(e))
    }
}
