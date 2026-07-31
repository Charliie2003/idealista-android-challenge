package com.carloshinojosa.idealistachallenge.core.data.repository

import com.carloshinojosa.idealistachallenge.core.data.cache.InMemoryPropertiesCache
import com.carloshinojosa.idealistachallenge.core.data.dispatcher.DispatcherProvider
import com.carloshinojosa.idealistachallenge.core.domain.datasource.RemotePropertiesDataSource
import com.carloshinojosa.idealistachallenge.core.domain.error.DomainError
import com.carloshinojosa.idealistachallenge.core.domain.model.EnergyCertification
import com.carloshinojosa.idealistachallenge.core.domain.model.MoreCharacteristics
import com.carloshinojosa.idealistachallenge.core.domain.model.PriceInfo
import com.carloshinojosa.idealistachallenge.core.domain.model.Property
import com.carloshinojosa.idealistachallenge.core.domain.model.PropertyDetailEnrichment
import com.carloshinojosa.idealistachallenge.core.domain.util.Result
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PropertiesRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun buildRepository(
        remote: FakeRemoteDataSource,
        cache: InMemoryPropertiesCache = InMemoryPropertiesCache(),
    ) = PropertiesRepositoryImpl(
        remoteDataSource = remote,
        cache = cache,
        dispatchers = TestDispatcherProvider(testDispatcher),
    )

    @Test
    fun `observeProperties caches a successful listing`() = runTest(testDispatcher) {
        val cache = InMemoryPropertiesCache()
        val remote = FakeRemoteDataSource(propertiesResult = Result.Success(listOf(property("1"), property("2"))))
        val repo = buildRepository(remote, cache)

        repo.observeProperties().first()

        assertEquals(property("1"), cache.get("1"))
        assertEquals(property("2"), cache.get("2"))
    }

    @Test
    fun `observeProperties does not clear cached data when refresh fails`() = runTest(testDispatcher) {
        val cache = InMemoryPropertiesCache()
        cache.replace(listOf(property("1")))

        val remote = FakeRemoteDataSource(propertiesResult = Result.Error(DomainError.Network))
        val repo = buildRepository(remote, cache)

        repo.observeProperties().first()

        assertEquals(property("1"), cache.get("1"))
    }

    @Test
    fun `getDetail uses cached base property without fetching listing again`() = runTest(testDispatcher) {
        val cache = InMemoryPropertiesCache()
        cache.replace(listOf(property("1")))

        val remote = FakeRemoteDataSource(
            propertiesResult = Result.Success(listOf(property("1"))),
            enrichmentResult = Result.Error(DomainError.Network),
        )
        val repo = buildRepository(remote, cache)

        repo.getDetail("1")

        assertEquals(0, remote.fetchPropertiesCount)
    }

    @Test
    fun `getDetail fetches listing when cache is empty`() = runTest(testDispatcher) {
        val remote = FakeRemoteDataSource(
            propertiesResult = Result.Success(listOf(property("1"))),
            enrichmentResult = Result.Error(DomainError.Network),
        )
        val repo = buildRepository(remote)

        repo.getDetail("1")

        assertEquals(1, remote.fetchPropertiesCount)
    }

    @Test
    fun `getDetail caches listing loaded as fallback`() = runTest(testDispatcher) {
        val remote = FakeRemoteDataSource(
            propertiesResult = Result.Success(listOf(property("1"))),
            enrichmentResult = Result.Error(DomainError.Network),
        )
        val repo = buildRepository(remote)

        repo.getDetail("1")
        repo.getDetail("1")

        assertEquals(1, remote.fetchPropertiesCount)
    }

    @Test
    fun `getDetail returns listing error when cache is empty and listing fails`() = runTest(testDispatcher) {
        val remote = FakeRemoteDataSource(
            propertiesResult = Result.Error(DomainError.Network),
            enrichmentResult = Result.Error(DomainError.Network),
        )
        val repo = buildRepository(remote)

        val result = repo.getDetail("1")

        assertTrue(result is Result.Error)
        assertEquals(DomainError.Network, (result as Result.Error).error)
    }

    @Test
    fun `getDetail returns not found when requested id is absent from listing`() = runTest(testDispatcher) {
        val remote = FakeRemoteDataSource(
            propertiesResult = Result.Success(listOf(property("1"))),
            enrichmentResult = Result.Error(DomainError.Network),
        )
        val repo = buildRepository(remote)

        val result = repo.getDetail("99")

        assertTrue(result is Result.Error)
        assertEquals(DomainError.NotFound, (result as Result.Error).error)
    }

    @Test
    fun `getDetail enriches property only when enrichment id matches requested id`() = runTest(testDispatcher) {
        val remote = FakeRemoteDataSource(
            propertiesResult = Result.Success(listOf(property("1"))),
            enrichmentResult = Result.Success(enrichment("1")),
        )
        val repo = buildRepository(remote)

        val result = repo.getDetail("1") as Result.Success

        assertTrue(result.data.isEnriched)
        assertEquals("Enriched comment 1", result.data.description)
    }

    @Test
    fun `getDetail never mixes enrichment from a different property`() = runTest(testDispatcher) {
        val remote = FakeRemoteDataSource(
            propertiesResult = Result.Success(listOf(property("2"))),
            enrichmentResult = Result.Success(enrichment("1")),
        )
        val repo = buildRepository(remote)

        val result = repo.getDetail("2") as Result.Success

        assertFalse(result.data.isEnriched)
        assertEquals("Description 2", result.data.description)
    }

    @Test
    fun `getDetail returns base detail when enrichment request fails`() = runTest(testDispatcher) {
        val remote = FakeRemoteDataSource(
            propertiesResult = Result.Success(listOf(property("1"))),
            enrichmentResult = Result.Error(DomainError.Network),
        )
        val repo = buildRepository(remote)

        val result = repo.getDetail("1") as Result.Success

        assertFalse(result.data.isEnriched)
    }

    @Test
    fun `two concurrent cache misses perform only one listing request`() = runTest(testDispatcher) {
        val gate = CompletableDeferred<Unit>()
        val remote = FakeRemoteDataSource(
            propertiesResult = Result.Success(listOf(property("1"))),
            enrichmentResult = Result.Error(DomainError.Network),
            fetchPropertiesGate = gate,
        )
        val cache = InMemoryPropertiesCache()
        val repo = buildRepository(remote, cache)

        val j1 = launch { repo.getDetail("1") }
        val j2 = launch { repo.getDetail("1") }

        gate.complete(Unit)
        j1.join()
        j2.join()

        assertEquals(1, remote.fetchPropertiesCount)
    }

    @Test
    fun `explicit listing refresh still performs a network request`() = runTest(testDispatcher) {
        val cache = InMemoryPropertiesCache()
        val remote = FakeRemoteDataSource(propertiesResult = Result.Success(listOf(property("1"))))
        val repo = buildRepository(remote, cache)

        repo.observeProperties().first()
        repo.observeProperties().first()

        assertEquals(2, remote.fetchPropertiesCount)
    }

    @Test
    fun `failed refresh does not make an already cached detail unavailable`() = runTest(testDispatcher) {
        val cache = InMemoryPropertiesCache()
        cache.replace(listOf(property("1")))

        val remote = FakeRemoteDataSource(
            propertiesResult = Result.Error(DomainError.Network),
            enrichmentResult = Result.Error(DomainError.Network),
        )
        val repo = buildRepository(remote, cache)

        repo.observeProperties().first()

        val result = repo.getDetail("1")
        assertTrue(result is Result.Success)
    }

    // --- Test fixtures ---

    private fun property(id: String) = Property(
        id = id,
        thumbnail = null,
        floor = null,
        priceInfo = PriceInfo(amount = 100_000.0, currencySuffix = "€"),
        propertyType = "flat",
        operation = Property.OPERATION_SALE,
        size = 80.0,
        exterior = true,
        rooms = 3,
        bathrooms = 1,
        address = "Test street $id",
        province = "Madrid",
        municipality = "Madrid",
        district = "Centro",
        country = "es",
        neighborhood = "Sol",
        latitude = 40.0,
        longitude = -3.0,
        description = "Description $id",
        images = emptyList(),
        hasParkingSpace = false,
        isParkingSpaceIncludedInPrice = false,
        hasAirConditioning = false,
        hasBoxRoom = false,
        hasSwimmingPool = false,
        hasTerrace = false,
        hasGarden = false,
    )

    private fun enrichment(id: String) = PropertyDetailEnrichment(
        id = id,
        propertyComment = "Enriched comment $id",
        detailLatitude = 40.1,
        detailLongitude = -3.1,
        moreCharacteristics = MoreCharacteristics(
            communityCosts = 100.0,
            roomNumber = 3,
            bathNumber = 1,
            exterior = true,
            housingFurnitures = "furnished",
            agencyIsABank = false,
            energyCertificationType = "c",
            flatLocation = "high",
            modificationDate = 1_000_000L,
            constructedArea = 90,
            lift = true,
            boxroom = false,
            isDuplex = false,
            floor = "3",
            status = "good",
        ),
        energyCertification = EnergyCertification(
            title = "energyCertification",
            energyConsumptionType = "c",
            emissionsType = "c",
        ),
        detailImages = emptyList(),
    )
}

private class FakeRemoteDataSource(
    private val propertiesResult: Result<List<Property>>,
    private val enrichmentResult: Result<PropertyDetailEnrichment> = Result.Error(DomainError.Network),
    private val fetchPropertiesGate: CompletableDeferred<Unit>? = null,
) : RemotePropertiesDataSource {

    var fetchPropertiesCount = 0

    override suspend fun fetchProperties(): Result<List<Property>> {
        fetchPropertiesGate?.await()
        fetchPropertiesCount++
        return propertiesResult
    }

    override suspend fun fetchPropertyDetailEnrichment(): Result<PropertyDetailEnrichment> = enrichmentResult
}

private class TestDispatcherProvider(private val dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}
