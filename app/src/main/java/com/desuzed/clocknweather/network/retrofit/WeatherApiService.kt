package com.desuzed.clocknweather.network.retrofit

import com.desuzed.clocknweather.BuildConfig
import com.desuzed.clocknweather.network.dto.ApiError
import com.desuzed.clocknweather.network.dto.LocationDto
import com.desuzed.clocknweather.network.dto.WeatherApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("search.json?key=${BuildConfig.WEATHER_API_KEY}")
    suspend fun getLocation(
        @Query("q") query: String
    ): Response<LocationDto>
    //Todo ru/en language

    @GET("forecast.json?key=${BuildConfig.WEATHER_API_KEY}&days=7&lang=en")
    suspend fun getForecast(
        @Query("q") query: String
    ): NetworkResponse<WeatherApi, ApiError>


    companion object{
        private const val baseUrl = "https://api.weatherapi.com/v1/"
        private var weatherApiService : WeatherApiService? = null
        fun getInstance() : WeatherApiService {
            if (weatherApiService == null){
                val interceptor = HttpLoggingInterceptor()
                interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
                val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build()
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(NetworkResponseAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                weatherApiService = retrofit.create(WeatherApiService::class.java)
            }
            return weatherApiService!!
        }
    }
}