package com.desuzed.everyweather.domain.interactor

import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.model.result.ActionType
import com.desuzed.everyweather.domain.model.result.FetchResult
import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.weather.Error
import com.desuzed.everyweather.domain.model.weather.ResultForecast
import com.desuzed.everyweather.domain.model.weather.WeatherContent
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider
import com.desuzed.everyweather.domain.repository.remote.RemoteDataRepository
import com.desuzed.everyweather.domain.repository.settings.SystemSettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.IOException

class WeatherInteractor(
    private val remoteDataRepository: RemoteDataRepository,
    private val systemSettingsRepository: SystemSettingsRepository,
    private val weatherDataRepository: WeatherDataRepository,
) {

    suspend fun fetchForecastOrErrorMessage(
        query: String,
        userLatLng: UserLatLng? = null,
    ): ResultForecast {
        if (query.isBlank()) {
            return ResultForecast(
                weatherContent = null,
                queryResult = QueryResult(code = ActionResultProvider.NO_DATA, query = query),
            )
        }
        val lang = systemSettingsRepository
            .lang
            .firstOrNull()?.lang?.lowercase()
            ?: Lang.EN.lang.lowercase()

        weatherDataRepository.updateQueryShouldNotBeTriggered()

        return when (val resultData = getForecast(query, lang)) {
            is FetchResult.Success -> {
                val resultContent = handleLatLonAndSaveForecast(resultData.body, userLatLng, query)
                ResultForecast(resultContent, null)
            }

            is FetchResult.Failure -> {
                ResultForecast(
                    weatherContent = weatherDataRepository.getWeatherContentFlow().firstOrNull(),
                    queryResult = QueryResult(
                        code = resultData.error.code,
                        query = query,
                        actionType = ActionType.RETRY
                    ),
                )
            }
        }
    }

    /**
     * Пофиксил баг из отзывов гугл плей маркета: маркер на карте отображался на неправильном месте
     * при загрузке погоды, а был сдвинут. Это было связано с тем, что брались координаты местоположения
     * от апи, и они очень неточные, 2 числа после запятой. Пришлось прокидывать полные координаты с карты
     * */
    private suspend fun handleLatLonAndSaveForecast(
        weatherContent: WeatherContent,
        userLatLng: UserLatLng?,
        query: String,
    ): WeatherContent {
        val latStr = query.substringBefore(",").trim()
        val lonStr = query.substringAfter(",").trim()

        val resultContent = if (userLatLng != null) {
            weatherContent.copy(
                location = weatherContent.location.copy(
                    lat = userLatLng.lat,
                    lon = userLatLng.lon,
                )
            )
        } else try {
            val lat = latStr.toDouble()
            val lon = lonStr.toDouble()
            weatherContent.copy(
                location = weatherContent.location.copy(
                    lat = lat,
                    lon = lon,
                )
            )
        } catch (e: Exception) {
            weatherContent
        }
        currentCoroutineContext().ensureActive()
        try {
            weatherDataRepository.saveForecastToCache(resultContent)
        } catch (_: IOException) {
            // Fresh weather remains usable even when persistent storage is unavailable.
        }
        return resultContent
    }

    private suspend fun getForecast(
        query: String,
        lang: String,
    ): FetchResult<WeatherContent, Error> =
        remoteDataRepository.getForecast(query, lang.lowercase())

}