package com.desuzed.everyweather.data.mapper.location

import com.desuzed.everyweather.data.room.FavoriteLocationDto
import com.desuzed.everyweather.domain.model.location.FavoriteLocation
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteLocationMapperTest {

    private val mapper = FavoriteLocationMapper()

    @Test
    fun `favorite location round trip preserves custom name and query coordinates`() {
        val original = FavoriteLocation(
            latLon = "55.8,37.7",
            cityName = "Moscow",
            region = "Moscow",
            country = "Russia",
            lat = "55.7558",
            lon = "37.6173",
            customName = "Home",
        )

        val restored = mapper.mapFromEntity(mapper.mapFromDomain(original))

        assertEquals(original, restored)
        assertEquals("55.7558, 37.6173", restored.toQuery())
    }

    @Test
    fun `entity to domain mapping preserves persisted custom name`() {
        val entity = FavoriteLocationDto(
            latLon = "51.5,-0.1",
            cityName = "London",
            region = "England",
            country = "United Kingdom",
            lat = "51.5072",
            lon = "-0.1276",
            customName = "Work trip",
        )

        assertEquals("Work trip", mapper.mapFromEntity(entity).customName)
    }
}
