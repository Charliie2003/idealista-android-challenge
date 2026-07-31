package com.carloshinojosa.idealistachallenge.core.data.cache

import com.carloshinojosa.idealistachallenge.core.domain.model.Property

/**
 * Session-scoped in-memory cache for the property listing.
 * Avoids re-downloading the listing when navigating to detail from the list screen.
 * Intentionally lives only in :core:data — the domain layer has no awareness of it.
 */
interface PropertiesMemoryCache {
    fun get(id: String): Property?
    fun replace(properties: List<Property>)
    fun clear()
}
