package com.carloshinojosa.idealistachallenge.core.network.mapper

import com.carloshinojosa.idealistachallenge.core.network.dto.PropertyDetailDto
import org.junit.Assert.assertEquals
import org.junit.Test

class PropertyDetailDtoMapperTest {

    @Test
    fun `maps adid integer to string domain id`() {
        val dto1 = PropertyDetailDto(adid = 1)
        assertEquals("1", dto1.toDomain().id)

        val dto42 = PropertyDetailDto(adid = 42)
        assertEquals("42", dto42.toDomain().id)
    }
}
