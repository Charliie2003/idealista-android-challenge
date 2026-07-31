package com.carloshinojosa.idealistachallenge.core.data.cache

import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import javax.inject.Inject

/** Thread-safe in-memory cache backed by an immutable map swapped atomically on each replace. */
class InMemoryPropertiesCache @Inject constructor() : PropertiesMemoryCache {

    @Volatile private var snapshot: Map<String, Property> = emptyMap()

    override fun get(id: String): Property? = snapshot[id]

    // associateBy produces a new immutable map; the reference assignment is atomic on JVM.
    override fun replace(properties: List<Property>) {
        snapshot = properties.associateBy { it.id }
    }

    override fun clear() {
        snapshot = emptyMap()
    }
}
