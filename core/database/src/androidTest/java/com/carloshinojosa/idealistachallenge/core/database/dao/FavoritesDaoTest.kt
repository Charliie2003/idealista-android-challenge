package com.carloshinojosa.idealistachallenge.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.carloshinojosa.idealistachallenge.core.database.db.AppDatabase
import com.carloshinojosa.idealistachallenge.core.database.entity.FavoriteEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FavoritesDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.favoritesDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertPersistsFavoriteRetrievableViaFindById() = runTest {
        val entity = FavoriteEntity(propertyId = "p1", favoritedAt = 1_000_000L)

        dao.upsert(entity)

        val result = dao.findById("p1")
        assertNotNull(result)
        assertEquals("p1", result?.propertyId)
        assertEquals(1_000_000L, result?.favoritedAt)
    }

    @Test
    fun upsertReplacesExistingRowForSamePropertyId() = runTest {
        val first = FavoriteEntity(propertyId = "p1", favoritedAt = 1_000_000L)
        val second = FavoriteEntity(propertyId = "p1", favoritedAt = 2_000_000L)

        dao.upsert(first)
        dao.upsert(second)

        val result = dao.findById("p1")
        assertNotNull(result)
        assertEquals(2_000_000L, result?.favoritedAt)
    }

    @Test
    fun deleteByIdRemovesOnlyMatchingRow() = runTest {
        dao.upsert(FavoriteEntity(propertyId = "p1", favoritedAt = 1_000_000L))
        dao.upsert(FavoriteEntity(propertyId = "p2", favoritedAt = 2_000_000L))

        dao.deleteById("p1")

        assertNull(dao.findById("p1"))
        assertNotNull(dao.findById("p2"))
    }

    @Test
    fun observeAllEmitsAgainAfterNewFavoriteIsInserted() = runTest {
        dao.observeAll().test {
            assertEquals(0, awaitItem().size)

            dao.upsert(FavoriteEntity(propertyId = "p1", favoritedAt = 1_000_000L))

            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("p1", updated.first().propertyId)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
