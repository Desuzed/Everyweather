package com.desuzed.everyweather.data.network.retrofit.api

import com.desuzed.everyweather.BuildConfig
import com.desuzed.everyweather.data.network.dto.weatherApi.ErrorDtoWeatherApi
import com.desuzed.everyweather.data.network.dto.weatherApi.WeatherResponseDto
import com.desuzed.everyweather.domain.model.network.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast.json?key=${BuildConfig.WEATHER_API_KEY}&days=7")
    suspend fun getForecast(
        @Query("q") query: String,
        @Query("lang") lang: String
    ): NetworkResponse<WeatherResponseDto, ErrorDtoWeatherApi>
}