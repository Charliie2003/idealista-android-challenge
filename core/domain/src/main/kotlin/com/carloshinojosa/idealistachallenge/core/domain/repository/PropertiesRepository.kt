package com.carloshinojosa.idealistachallenge.core.domain.repository

import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetail
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

/** Contract for the properties repository. Implementation lives in :core:data. */
interface PropertiesRepository {
    fun observeProperties(): Flow<Result<List<Property>>>
    suspend fun getDetail(id: String): Result<PropertyDetail>
}
