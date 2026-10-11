package com.desuzed.everyweather.data.room

import com.desuzed.everyweather.domain.model.weather.Location
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteLocationDtoTest {

    @Test
    fun `test toString returns correct value with all country fields being filled`() {
        val favoriteLocationDto =
            FavoriteLocationDto(
                "43.2,132.1",
                "cityName",
                "region",
                "country",
                "43.2",
                "132.1"
            )
        val expectedStr = "region, country"

        val actualStr = favoriteLocationDto.toString()

        assertEquals(expectedStr, actualStr)
    }

    @Test
    fun `test toString returns correct value when region is empty`() {
        val favoriteLocationDto =
            FavoriteLocationDto(
                "43.2,132.1",
                "cityName",
                "",
                "country",
                "43.2",
                "132.1"
            )
        val expectedStr = "country"

        val actualStr = favoriteLocationDto.toString()

        assertEquals(expectedStr, actualStr)
    }

    @Test
    fun `build favorite location preserves the geographic longitude`() {
        val location =
            Location(
                name = "Vladivostok",
                region = "Primorsky Krai",
                country = "Russia",
                lat = 43.1155,
                lon = 131.8855,
                timezone = "Asia/Vladivostok",
                localtimeEpoch = 10,
            )

        val actual = FavoriteLocationDto.buildFavoriteLocationObj(
            location = location,
            latLon = "43.2,131.9",
        )

        assertEquals("43.1155", actual.lat)
        assertEquals("131.8855", actual.lon)
    }
}
