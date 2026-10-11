package com.desuzed.everyweather.data.mapper.weather_api

import com.desuzed.everyweather.data.network.dto.weatherApi.ConditionDto
import com.desuzed.everyweather.data.network.dto.weatherApi.CurrentDto
import com.desuzed.everyweather.data.network.dto.weatherApi.LocationDto
import com.desuzed.everyweather.data.network.dto.weatherApi.WeatherResponseDto
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherResponseMapperTest {

    private val mapper = WeatherResponseMapper(
        locationMapper = LocationMapper(),
        currentMapper = CurrentMapper(),
        forecastDayMapper = ForecastDayMapper(DayMapper(), HourMapper(), AstroMapper()),
    )

    @Test
    fun `maps metric and imperial current weather values without unit swapping`() {
        val weather = mapper.mapFromEntity(
            WeatherResponseDto(
                locationDto = LocationDto(
                    name = "New York",
                    region = "New York",
                    country = "United States",
                    lat = 40.7128,
                    lon = -74.0060,
                    timeZone = "America/New_York",
                    localtimeEpoch = 1_700_000_000,
                ),
                currentDto = CurrentDto(
                    tempC = 20.5f,
                    tempF = 68.9f,
                    conditionDto = ConditionDto(text = "Sunny", icon = "//icon"),
                    windSpeedKph = 15.4f,
                    windSpeedMph = 9.6f,
                    pressureMb = 1013.2f,
                    pressureInch = 29.92f,
                    precipMm = 2.5f,
                    precipInch = 0.1f,
                    humidity = 61,
                    feelsLikeC = 20.1f,
                    feelsLikeF = 68.2f,
                ),
            ),
        )

        assertEquals(20.5f, weather.current.tempC)
        assertEquals(68.9f, weather.current.tempF)
        assertEquals(15.4f, weather.current.windSpeedKph)
        assertEquals(9.6f, weather.current.windSpeedMph)
        assertEquals(1013.2f, weather.current.pressureMb)
        assertEquals(29.92f, weather.current.pressureInch)
        assertEquals(2.5f, weather.current.precipMm)
        assertEquals(0.1f, weather.current.precipInch)
        assertEquals("America/New_York", weather.location.timezone)
        assertEquals(-74.0060, weather.location.lon, 0.0)
    }

    @Test
    fun `maps a missing forecast to an empty list`() {
        val weather = mapper.mapFromEntity(
            WeatherResponseDto(
                locationDto = LocationDto(name = "Tokyo"),
                currentDto = CurrentDto(),
            ),
        )

        assertEquals(emptyList<Any>(), weather.forecastDay)
    }
}
