package com.desuzed.everyweather.data.repository

import android.content.Context
import com.desuzed.everyweather.data.network.ActionResultProviderImpl
import com.desuzed.everyweather.model.model.WeatherResponse
import com.google.gson.Gson

class SPrefProviderImpl(private val context: Context) : SPrefProvider {
    private val sp =
        context.getSharedPreferences(LocalDataSourceImpl.S_PREF_NAME, Context.MODE_PRIVATE)

    override fun saveForecast(weatherResponse: WeatherResponse) {
        val gson = Gson().toJson(weatherResponse)
        sp
            .edit()
            .putString(LocalDataSourceImpl.WEATHER_API, gson)
            .apply()
    }

    override fun loadForecast(): WeatherResponse? {
        val savedForecast = sp.getString(LocalDataSourceImpl.WEATHER_API, null)
        return Gson().fromJson(savedForecast, WeatherResponse::class.java)
    }

    override fun saveQuery(query: String) {
        sp
            .edit()
            .putString(LocalDataSourceImpl.QUERY, query)
            .apply()
    }

    override fun loadQuery(): String = sp.getString(LocalDataSourceImpl.QUERY, "").toString()

    override fun parseCode(errorCode: Int): String =
        ActionResultProviderImpl(context.resources).parseCode(errorCode)
}

interface SPrefProvider {
    fun saveForecast(weatherResponse: WeatherResponse)
    fun loadForecast(): WeatherResponse?
    fun saveQuery(query: String)
    fun loadQuery(): String
    fun parseCode(errorCode: Int): String
}