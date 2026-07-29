package com.carloshinojosa.idealistachallenge.core.domain.datasource

import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetailEnrichment
import com.carloshinojosa.idealistachallenge.core.domain.util.Result

/**
 * Domain port: defines what the data layer must provide for remote property access.
 * Implementation lives in :core:network to maintain the Dependency Inversion Principle.
 */
interface RemotePropertiesDataSource {
    suspend fun fetchProperties(): Result<List<Property>>
    suspend fun fetchPropertyDetailEnrichment(): Result<PropertyDetailEnrichment>
}
