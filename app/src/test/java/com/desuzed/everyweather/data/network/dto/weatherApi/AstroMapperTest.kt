package com.desuzed.everyweather.data.network.dto.weatherApi

import com.desuzed.everyweather.data.mapper.weather_api.AstroMapper
import com.desuzed.everyweather.domain.model.weather.Astro
import org.junit.Assert.assertEquals
import org.junit.Test

class AstroMapperTest {
    @Test
    fun `maps 12 hour api times including midnight and noon`() {
        val astroMapper = AstroMapper()
        val astroDto = AstroDto(
            sunrise = "12:00 AM",
            sunset = "12:00 PM",
            moonrise = "01:40 PM",
            moonset = "04:59 AM",
        )

        val astroApp = astroMapper.mapFromEntity(astroDto)

        assertEquals(Astro("00:00", "12:00", "13:40", "04:59"), astroApp)
    }

    @Test
    fun `keeps original values when api returns a non time astro value`() {
        val astroDto = AstroDto(
            sunrise = "No sunrise",
            sunset = "07:16 PM",
            moonrise = "No moonrise",
            moonset = "No moonset",
        )

        assertEquals(
            Astro("No sunrise", "07:16 PM", "No moonrise", "No moonset"),
            AstroMapper().mapFromEntity(astroDto),
        )
    }
}
