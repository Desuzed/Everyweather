package com.desuzed.everyweather.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.desuzed.everyweather.domain.model.location.FavoriteLocation
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.weather.Astro
import com.desuzed.everyweather.domain.model.weather.Current
import com.desuzed.everyweather.domain.model.weather.Day
import com.desuzed.everyweather.domain.model.weather.ForecastDay
import com.desuzed.everyweather.domain.model.weather.Hour
import com.desuzed.everyweather.domain.model.weather.Location
import com.desuzed.everyweather.domain.model.weather.WeatherContent
import com.desuzed.everyweather.presentation.ui.main.DetailCardMain
import com.desuzed.everyweather.util.MockWeatherObject.weather
import java.util.UUID

object MockWeatherObject {
    val weatherLocation = Location(
        name = "Location name",
        region = "region location",
        country = "country",
        lat = 100.5,
        lon = 200.1,
        timezone = "Moscow/russia",
        localtimeEpoch = 10000000,
    )
    val current = Current(
        tempC = 25.1f,
        tempF = 25.2f,
        text = "text",
        icon = "icon",
        windSpeedKph = 100.1f,
        windSpeedMph = 100f,
        pressureMb = 273f,
        pressureInch = 100f,
        precipMm = 100f,
        precipInch = 100f,
        humidity = 200,
        feelsLikeC = 28.5f,
        feelsLikeF = 28f
    )
    val day = Day(
        maxTempC = 27.3f,
        maxTempF = 21.3f,
        minTempC = 100f,
        minTempF = 200f,
        maxWindKph = 125f,
        maxWindMph = 125f,
        totalPrecipMm = 10f,
        totalPrecipInch = 12f,
        avgHumidity = 13f,
        popRain = 50,
        text = "text",
        icon = "icon"
    )
    val astro = Astro(sunrise = "11:22", sunset = "19:05", moonrise = "10:00", moonset = "11:22")
    val hour = Hour(
        timeEpoch = 123122545,
        tempC = 20.1f,
        tempF = 20.1f,
        text = "text",
        icon = "ic",
        windSpeedKmh = 120f,
        windSpeedMph = 110f,
        windDegree = 22,
        pressureMb = 543f,
        pressureInch = 550f
    )
    val forecastDay = ForecastDay(
        date = "11.11.2021",
        dateEpoch = 13231312,
        day = day,
        astro = astro,
        hourForecast = listOf(hour, hour, hour, hour),
        isExpanded = false
    )
    val weather = WeatherContent(
        location = weatherLocation,
        current = current,
        forecastDay = listOf(forecastDay, forecastDay, forecastDay, forecastDay)
    )
    val locationDto = FavoriteLocation(
        latLon = "",
        cityName = "London",
        region = "London region",
        country = "United Kingdoms",
        lat = "",
        lon = "",
        customName = "",
    )
    val locations = listOf(
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
        locationDto.copy(latLon = UUID.randomUUID().toString()),
    )
}

val detailCardMain: DetailCardMain
    @Composable
    get() {
        return DetailCardMain(
            response = weather,
            res = LocalContext.current.resources,
            windSpeed = DistanceDimen.METRIC_KMH,
            pressureDimen = PressureDimen.MILLIMETERS,
        )
    }