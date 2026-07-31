package com.carloshinojosa.idealistachallenge.detail

import android.content.Context
import com.carloshinojosa.idealistachallenge.detail.presentation.mapper.DetailMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class DetailMapperTest {

    // Relaxed mock: getString returns "" for all unstubbed calls. The real logic under
    // test — price formatting, energy index, characteristic count, floor label — does not
    // depend on string resources, so "" is safe for those tests.
    private val context: Context = mockk(relaxed = true)
    private val mapper = DetailMapper(context)

    private lateinit var savedTimezone: TimeZone

    @Before
    fun setUp() {
        // Fix the JVM timezone so DateTimeFormatter.ofLocalizedDate produces a deterministic
        // result regardless of the CI machine's locale configuration.
        savedTimezone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(savedTimezone)
        io.mockk.clearAllMocks()
    }

    // ── id, price, and suffix ─────────────────────────────────────────────────

    @Test
    fun `map returns correct id, priceLabel in Spanish locale, and trimmed priceSuffix`() {
        // given – 250 000 € in es-ES locale formats as "250.000" (period as thousands separator)
        val detail = DetailFixtures.propertyDetail(propertyId = "p-001", price = 250_000.0)

        // when
        val ui = mapper.map(detail)

        // then
        assertEquals("p-001", ui.id)
        assertEquals("250.000", ui.priceLabel)
        assertEquals("€", ui.priceSuffix)
    }

    // ── operation label fallback ──────────────────────────────────────────────

    @Test
    fun `map uses the raw operation string when the operation is not sale or rent`() {
        // The mapper's else branch returns the operation value directly — no getString call.
        val detail = DetailFixtures.propertyDetail().copy(
            property = DetailFixtures.propertyDetail().property.copy(operation = "auction"),
        )

        val ui = mapper.map(detail)

        assertEquals("auction", ui.operationLabel)
    }

    // ── characteristics ───────────────────────────────────────────────────────

    @Test
    fun `map returns 6 characteristics when moreCharacteristics is present`() {
        val detail = DetailFixtures.propertyDetail(isEnriched = true)

        val ui = mapper.map(detail)

        assertEquals(6, ui.characteristics.size)
    }

    @Test
    fun `map returns empty characteristics when moreCharacteristics is absent`() {
        val detail = DetailFixtures.propertyDetail(isEnriched = false)

        val ui = mapper.map(detail)

        assertTrue(ui.characteristics.isEmpty())
    }

    // ── energy certification ──────────────────────────────────────────────────

    @Test
    fun `map returns energy certification with activeIndex 4 for consumption type E`() {
        val detail = DetailFixtures.propertyDetail(isEnriched = true, energyCertificationType = "E")

        val ui = mapper.map(detail)

        assertNotNull(ui.energyCertification)
        assertEquals("E", ui.energyCertification!!.letter)
        assertEquals(4, ui.energyCertification!!.activeIndex)
    }

    @Test
    fun `map returns energy certification with activeIndex 0 for consumption type A`() {
        val detail = DetailFixtures.propertyDetail(isEnriched = true, energyCertificationType = "A")

        val ui = mapper.map(detail)

        assertNotNull(ui.energyCertification)
        assertEquals("A", ui.energyCertification!!.letter)
        assertEquals(0, ui.energyCertification!!.activeIndex)
    }

    @Test
    fun `map returns null energy certification when energyConsumptionType is unrecognized`() {
        val detail = DetailFixtures.propertyDetail(isEnriched = true, energyCertificationType = "X")

        val ui = mapper.map(detail)

        assertNull(ui.energyCertification)
    }

    // ── floor label ───────────────────────────────────────────────────────────

    @Test
    fun `map highlights use the base property floor when moreCharacteristics is absent`() {
        // property.floor = "2"; when mc is null the mapper falls back to property.floor
        val detail = DetailFixtures.propertyDetail(isEnriched = false)

        val ui = mapper.map(detail)

        assertEquals("2ª", ui.highlights.floorLabel)
    }

    // ── community costs ───────────────────────────────────────────────────────

    @Test
    fun `map communityCostsLabel is null when community costs are zero`() {
        val detail = DetailFixtures.propertyDetail().copy(
            moreCharacteristics = DetailFixtures.moreCharacteristics(communityCosts = 0.0),
        )

        val ui = mapper.map(detail)

        assertNull(ui.communityCostsLabel)
    }

    // ── image fallback ────────────────────────────────────────────────────────

    @Test
    fun `map uses property thumbnail as the sole image when detail images list is empty`() {
        // property.thumbnail = "https://cdn.example.com/thumb.jpg" from the fixture
        val detail = DetailFixtures.propertyDetail().copy(images = emptyList())

        val ui = mapper.map(detail)

        assertEquals(1, ui.images.size)
        assertEquals("https://cdn.example.com/thumb.jpg", ui.images[0].url)
    }

    // ── formatFavoriteDate ────────────────────────────────────────────────────

    @Test
    fun `formatFavoriteDate returns the formatted result of the detail_saved_on template`() {
        // given – capture the date string that the formatter produces so we can assert on it
        val capturedDate = slot<String>()
        every {
            context.getString(eq(R.string.detail_saved_on), capture(capturedDate))
        } answers { "Guardado el ${capturedDate.captured}" }

        val instant = Instant.parse("2026-07-28T10:00:00Z")

        // when
        val result = mapper.formatFavoriteDate(instant)

        // then – the es-ES long-date formatter produces a string containing the year
        assertTrue("Expected non-blank result", result.isNotBlank())
        assertTrue(
            "Expected '2026' in formatted date, got: ${capturedDate.captured}",
            capturedDate.captured.contains("2026"),
        )
        assertTrue(
            "Expected 'julio' in formatted date, got: ${capturedDate.captured}",
            capturedDate.captured.contains("julio"),
        )
    }
}
