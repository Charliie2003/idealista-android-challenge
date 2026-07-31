package com.carloshinojosa.idealistachallenge.core.domain.usecase

import app.cash.turbine.test
import com.carloshinojosa.idealistachallenge.core.domain.error.DomainError
import com.carloshinojosa.idealistachallenge.core.domain.model.PriceInfo
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import com.carloshinojosa.idealistachallenge.core.testing.FakeFavoritesRepository
import com.carloshinojosa.idealistachallenge.core.testing.FakePropertiesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ObservePropertiesUseCaseTest {

    private val fakeFavoritesRepo = FakeFavoritesRepository()

    private fun property(id: String) = Property(
        id = id, thumbnail = null, floor = null,
        priceInfo = PriceInfo(amount = 100_000.0, currencySuffix = "€"),
        propertyType = "flat", operation = Property.OPERATION_SALE,
        size = 80.0, exterior = true, rooms = 3, bathrooms = 1,
        address = "Test street", province = "Madrid", municipality = "Madrid",
        district = "Centro", country = "es", neighborhood = "Sol",
        latitude = 40.0, longitude = -3.0, description = "Description",
        images = emptyList(),
        hasParkingSpace = false, isParkingSpaceIncludedInPrice = false,
        hasAirConditioning = false, hasBoxRoom = false,
        hasSwimmingPool = false, hasTerrace = false, hasGarden = false,
    )

    @Test
    fun `combines properties list and marks matching entries as favorited`() =
        runTest(UnconfinedTestDispatcher()) {
            val prop = property("p1")
            val propertiesFlow = MutableStateFlow<Result<List<Property>>>(Result.Success(listOf(prop)))
            val fakePropertiesRepo = FakePropertiesRepository(propertiesFlow)
            val useCase = ObservePropertiesUseCase(fakePropertiesRepo, fakeFavoritesRepo)

            useCase().test {
                // initial: not favorited
                val initial = awaitItem() as Result.Success
                assertFalse(initial.data.first().isFavorited)

                // toggle favorite
                fakeFavoritesRepo.toggle("p1", Instant.EPOCH)

                // second emission: now favorited
                val afterFav = awaitItem() as Result.Success
                assertTrue(afterFav.data.first().isFavorited)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits properties with isFavorited=false when favorites list is empty`() =
        runTest(UnconfinedTestDispatcher()) {
            val propertiesFlow = MutableStateFlow<Result<List<Property>>>(
                Result.Success(listOf(property("p1"), property("p2"))),
            )
            val fakePropertiesRepo = FakePropertiesRepository(propertiesFlow)
            val useCase = ObservePropertiesUseCase(fakePropertiesRepo, fakeFavoritesRepo)

            useCase().test {
                val result = awaitItem() as Result.Success
                assertTrue(result.data.all { !it.isFavorited })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits error but favorites flow continues independently`() =
        runTest(UnconfinedTestDispatcher()) {
            val propertiesFlow = MutableStateFlow<Result<List<Property>>>(
                Result.Error(DomainError.Network),
            )
            val fakePropertiesRepo = FakePropertiesRepository(propertiesFlow)
            val useCase = ObservePropertiesUseCase(fakePropertiesRepo, fakeFavoritesRepo)

            useCase().test {
                // first: error from properties
                assertTrue(awaitItem() is Result.Error)

                // recover: push a successful result
                propertiesFlow.value = Result.Success(listOf(property("p1")))

                // second: success with unfavorited property
                val afterRecovery = awaitItem() as Result.Success
                assertFalse(afterRecovery.data.first().isFavorited)

                // toggle favorite — favorites flow still active
                fakeFavoritesRepo.toggle("p1", Instant.EPOCH)

                // third: success with favorited property (favorites stream was not cancelled)
                val afterFav = awaitItem() as Result.Success
                assertTrue(afterFav.data.first().isFavorited)

                cancelAndIgnoreRemainingEvents()
            }
        }
}
