package com.desuzed.everyweather.data.network.dto.weatherApi

import com.desuzed.everyweather.model.entity.Astro
import junit.framework.TestCase
import org.junit.Test


class AstroMapperTest : TestCase() {
    @Test
    fun `test astro from api maps to app model class`() {
        //"sunrise":"07:29 AM","sunset":"07:16 PM","moonrise":"01:40 PM","moonset":"04:59 AM"
        val astroMapper = AstroMapper()
        val astroDto = AstroDto()
        astroDto.sunrise = "07:29 AM"
        astroDto.sunset = "07:16 PM"
        astroDto.moonrise = "01:40 PM"
        astroDto.moonset = "04:59 AM"

        val astroApp = astroMapper.mapFromEntity(astroDto)

        assertEquals(astroApp, Astro("07:29", "19:16", "13:40", "04:59"))
    }
}