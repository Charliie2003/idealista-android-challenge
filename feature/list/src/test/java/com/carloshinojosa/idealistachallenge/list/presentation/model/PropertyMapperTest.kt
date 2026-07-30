package com.carloshinojosa.idealistachallenge.list.presentation.model

import android.content.Context
import com.carloshinojosa.idealistachallenge.list.PropertyFixtures
import com.carloshinojosa.idealistachallenge.list.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class PropertyMapperTest {

    private val context: Context = mockk()
    private val mapper = PropertyMapper(context)

    private lateinit var savedTimezone: TimeZone

    @Before
    fun setUp() {
        // Fix the JVM timezone so DateTimeFormatter.ofLocalizedDate produces a deterministic date.
        savedTimezone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        every { context.getString(R.string.listing_filter_sale) } returns "Venta"
        every { context.getString(R.string.listing_filter_rent) } returns "Alquiler"
        // vararg getString: first arg is resId, remaining args are format params.
        every { context.getString(any(), any()) } returns "stubbed_format"
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(savedTimezone)
    }

    // ── price formatting ──────────────────────────────────────────────────────

    @Test
    fun `formats price amount using Spanish locale thousands separator`() {
        // given – 250 000 with the Spanish locale should separate with '.' not ','
        val property = PropertyFixtures.sale(price = 250_000.0)

        // when
        val ui = mapper.map(property)

        // then
        assertEquals("250.000", ui.priceAmountText)
    }

    // ── thumbnail resolution ──────────────────────────────────────────────────

    @Test
    fun `uses thumbnail field when present`() {
        val property = PropertyFixtures.sale(thumbnail = "https://cdn.example.com/thumb.jpg")

        val ui = mapper.map(property)

        assertEquals("https://cdn.example.com/thumb.jpg", ui.thumbnailUrl)
    }

    @Test
    fun `falls back to first image url when thumbnail is null`() {
        // given – PropertyFixtures provides one image in the images list
        val property = PropertyFixtures.sale(thumbnail = null)

        val ui = mapper.map(property)

        assertEquals("https://cdn.example.com/img.jpg", ui.thumbnailUrl)
    }

    @Test
    fun `falls back to empty string when thumbnail is null and images list is empty`() {
        val property = PropertyFixtures.sale(thumbnail = null).copy(images = emptyList())

        val ui = mapper.map(property)

        assertEquals("", ui.thumbnailUrl)
    }

    // ── operation label ───────────────────────────────────────────────────────

    @Test
    fun `maps sale operation to listing_filter_sale string resource`() {
        val property = PropertyFixtures.sale()

        val ui = mapper.map(property)

        assertEquals("Venta", ui.operationLabel)
        verify { context.getString(R.string.listing_filter_sale) }
    }

    // ── favorite date label ───────────────────────────────────────────────────

    @Test
    fun `formats favoritedAt date in es-ES locale and wraps in listing_saved_on`() {
        // given – November 4 2025 at noon UTC; timezone is fixed to UTC in setUp
        val favoritedAt = Instant.parse("2025-11-04T12:00:00Z")
        val capturedDate = slot<String>()
        every {
            context.getString(eq(R.string.listing_saved_on), capture(capturedDate))
        } answers { "Guardado el ${capturedDate.captured}" }

        val property = PropertyFixtures.sale(isFavorited = true, favoritedAt = favoritedAt)

        // when
        val ui = mapper.map(property)

        // then – the formatter uses FormatStyle.LONG with es-ES which produces "4 de noviembre de 2025"
        assertNotNull(ui.favoritedDateLabel)
        assertTrue(
            "Expected 'noviembre' in formatted date. Got: ${capturedDate.captured}",
            capturedDate.captured.contains("noviembre"),
        )
        assertTrue(
            "Expected '2025' in formatted date. Got: ${capturedDate.captured}",
            capturedDate.captured.contains("2025"),
        )
        verify { context.getString(eq(R.string.listing_saved_on), any()) }
    }

    @Test
    fun `favoritedDateLabel is null when favoritedAt is null`() {
        val property = PropertyFixtures.sale(isFavorited = false, favoritedAt = null)

        val ui = mapper.map(property)

        assertNull(ui.favoritedDateLabel)
    }

    // ── size label ────────────────────────────────────────────────────────────

    @Test
    fun `produces sizeLabel by formatting size as integer via listing_size_label`() {
        // given – size is 80.0, so size.toInt() == 80
        val sizeSlot = slot<Int>()
        every {
            context.getString(eq(R.string.listing_size_label), capture(sizeSlot))
        } answers { "${sizeSlot.captured} m²" }

        val property = PropertyFixtures.sale() // size = 80.0

        val ui = mapper.map(property)

        assertEquals(80, sizeSlot.captured)
        assertEquals("80 m²", ui.sizeLabel)
        verify { context.getString(eq(R.string.listing_size_label), 80) }
    }
}
