package com.desuzed.everyweather.presentation.features.weather_main

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import com.desuzed.everyweather.analytics.WeatherMainAnalytics
import com.desuzed.everyweather.domain.interactor.LocationInteractor
import com.desuzed.everyweather.domain.interactor.WeatherInteractor
import com.desuzed.everyweather.domain.interactor.WeatherSettingsInteractor
import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.domain.model.weather.WeatherContent
import com.desuzed.everyweather.domain.model.weather.WeatherQuery
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider.Companion.UNKNOWN
import com.desuzed.everyweather.domain.repository.settings.SystemSettingsRepository
import com.desuzed.everyweather.presentation.base.BaseViewModel

class WeatherMainViewModel(
    private val weatherInteractor: WeatherInteractor,
    private val weatherDataRepository: WeatherDataRepository,
    private val locationInteractor: LocationInteractor,
    private val analytics: WeatherMainAnalytics,
    private val weatherSettingsInteractor: WeatherSettingsInteractor,
    private val systemSettingsRepository: SystemSettingsRepository,
) : BaseViewModel<WeatherState, WeatherMainEffect, WeatherAction>(WeatherState()) {

    private var requestJob: Job? = null

    init {
        collect(weatherSettingsInteractor.distanceDimen, ::collectWindSpeed)
        collect(weatherSettingsInteractor.tempDimen, ::collectTemperature)
        collect(systemSettingsRepository.lang, ::collectLanguage)
        collect(weatherSettingsInteractor.pressureDimen, ::collectPressure)
        collect(weatherDataRepository.getQueryFlow(), ::onNewQuery)
        collect(weatherDataRepository.getWeatherContentFlow(), ::onNewWeatherContent)
    }

    private fun getForecast(query: String, userLatLng: UserLatLng? = null) {
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            setState { copy(isLoading = true, query = query) }
            try {
                val fetchedForecast = weatherInteractor.fetchForecastOrErrorMessage(
                    query = query,
                    userLatLng = userLatLng,
                )

                ensureActive()
                val actionResult = fetchedForecast.queryResult
                val isLocationSaved = fetchedForecast.weatherContent?.let {
                    isLocationSaved(it)
                }
                setState {
                    copy(
                        weatherData = fetchedForecast.weatherContent ?: weatherData,
                        isLoading = false,
                        isAddButtonEnabled = isLocationSaved?.not() ?: false,
                    )
                }
                if (actionResult != null) {
                    setSideEffect(WeatherMainEffect.ShowSnackBar(actionResult))
                    setState {
                        copy(
                            isLoading = false,
                            isAddButtonEnabled = false,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                setState { copy(isLoading = false, isAddButtonEnabled = false) }
                setSideEffect(WeatherMainEffect.ShowSnackBar(QueryResult(UNKNOWN, query)))
            }
        }
    }

    override fun onAction(action: WeatherAction) {
        analytics.onAction(action)
        when (action) {
            WeatherAction.Location -> setSideEffect(WeatherMainEffect.NavigateToLocation)
            WeatherAction.Refresh -> getForecast(state.value.query)
            WeatherAction.SaveLocation -> saveLocation()
            WeatherAction.Redirection -> onRedirection()
            WeatherAction.BackClick -> onBackClick()
        }
    }

    private fun onBackClick() {
        //TODO: Таймер + снекбар "Повторите/нажмите ещё раз, чтобы выйти"
        setSideEffect(WeatherMainEffect.ExitApp)
    }

    private fun saveLocation() {
        launch {
            val isSaved = locationInteractor.saveLocationToDb(state.value.weatherData)
            if (isSaved) {
                onSuccess(ActionResultProvider.SAVED)
            } else {
                onError(ActionResultProvider.FAIL)
            }
        }
    }

    private fun onError(code: Int) {
        setSideEffect(WeatherMainEffect.ShowSnackBar(QueryResult(code = code)))
    }

    private fun onSuccess(code: Int) {
        setSideEffect(WeatherMainEffect.ShowSnackBar(QueryResult(code = code)))
        setState {
            copy(isAddButtonEnabled = false)
        }
    }

    private suspend fun isLocationSaved(weatherContent: WeatherContent): Boolean {
        return locationInteractor.isLocationSaved(weatherContent.location)
    }

    private fun onNewQuery(index: Int, weatherQuery: WeatherQuery) {
        setState { copy(query = weatherQuery.query) }
        if (weatherQuery.shouldTriggerWeatherRequest || index == INDEX_OF_FIRST_QUERY) {
            getForecast(
                query = weatherQuery.query,
                userLatLng = weatherQuery.userChosenMapPoint,
            )
        }
    }

    private fun onNewWeatherContent(weatherContent: WeatherContent?) {
        if (weatherContent != null) {
            setState { copy(weatherData = weatherContent) }
        }
    }

    private fun onRedirection() {
        setSideEffect(
            WeatherMainEffect.ShowSnackBar(
                data = QueryResult(ActionResultProvider.REDIRECTION),
            )
        )
    }

    private fun collectWindSpeed(windSpeed: DistanceDimen) =
        setState { copy(windSpeed = windSpeed) }

    private fun collectLanguage(language: Lang) = setState { copy(selectedLang = language) }

    private fun collectTemperature(temperature: TempDimen) = setState {
        copy(temperature = temperature)
    }

    private fun collectPressure(pressure: PressureDimen) = setState {
        copy(pressure = pressure)
    }

    companion object {
        private const val INDEX_OF_FIRST_QUERY = 0
    }
}