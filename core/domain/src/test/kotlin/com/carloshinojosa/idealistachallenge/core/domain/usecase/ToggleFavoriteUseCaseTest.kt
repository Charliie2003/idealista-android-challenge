package com.carloshinojosa.idealistachallenge.core.domain.usecase

import com.carloshinojosa.idealistachallenge.core.testing.FakeFavoritesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ToggleFavoriteUseCaseTest {

    private val fixedInstant: Instant = Instant.parse("2026-07-31T10:00:00Z")
    private val clock: Clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    private val fakeFavoritesRepository = FakeFavoritesRepository()
    private val useCase = ToggleFavoriteUseCase(fakeFavoritesRepository, clock)

    @Test
    fun `marking a non-favorite persists a Favorite with the current Clock instant`() = runTest {
        useCase("prop-1")

        val stored = fakeFavoritesRepository.get("prop-1")
        assertNotNull(stored)
        assertEquals(fixedInstant, stored?.favoritedAt)
    }

    @Test
    fun `unmarking a favorite deletes the row`() = runTest {
        useCase("prop-1") // mark
        useCase("prop-1") // unmark

        assertNull(fakeFavoritesRepository.get("prop-1"))
    }
}
