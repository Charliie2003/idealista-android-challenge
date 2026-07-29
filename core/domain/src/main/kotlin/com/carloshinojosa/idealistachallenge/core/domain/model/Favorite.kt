package com.carloshinojosa.idealistachallenge.core.domain.model

import java.time.Instant

/** Represents a favorited property and when it was last favorited. See ADR-0006. */
data class Favorite(
    val propertyId: String,
    val favoritedAt: Instant,
)
