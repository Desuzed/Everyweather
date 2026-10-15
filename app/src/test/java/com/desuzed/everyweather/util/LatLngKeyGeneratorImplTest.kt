package com.desuzed.everyweather.util

import com.desuzed.everyweather.domain.model.weather.Location
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class LatLngKeyGeneratorImplTest {

    private val generator = LatLngKeyGeneratorImpl()

    @Test
    fun `key uses a dot decimal separator when device locale uses commas`() {
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale("ru", "RU"))
        try {
            val key = generator.generateKey(location(lat = 55.7558, lon = 37.6173))

            assertEquals("55.8,37.7", key)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun `key retains the established ceiling precision for negative coordinates`() {
        val key = generator.generateKey(location(lat = -33.8688, lon = -151.2093))

        assertEquals("-33.8,-151.2", key)
    }

    private fun location(lat: Double, lon: Double) = Location(
        name = "Test",
        region = "",
        country = "Test country",
        lat = lat,
        lon = lon,
        timezone = "UTC",
        localtimeEpoch = 0,
    )
}
