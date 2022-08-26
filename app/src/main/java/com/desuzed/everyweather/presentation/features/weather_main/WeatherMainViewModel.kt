package com.desuzed.everyweather.presentation.features.weather_main

import androidx.lifecycle.viewModelScope
import com.desuzed.everyweather.data.repository.local.SettingsRepository
import com.desuzed.everyweather.data.repository.local.UiMapper
import com.desuzed.everyweather.data.room.FavoriteLocationDto
import com.desuzed.everyweather.domain.model.WeatherResponse
import com.desuzed.everyweather.domain.repository.local.RoomProvider
import com.desuzed.everyweather.domain.repository.local.SharedPrefsProvider
import com.desuzed.everyweather.presentation.base.BaseViewModel
import com.desuzed.everyweather.util.ActionResultProvider
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class WeatherMainViewModel(
    private val useCase: WeatherMainUseCase,
    private val uiMapper: UiMapper,
    private val sharedPrefsProvider: SharedPrefsProvider,
    private val actionResultProvider: ActionResultProvider,
    private val roomProvider: RoomProvider,
    settingsRepository: SettingsRepository,
) :
    BaseViewModel<WeatherState, WeatherMainAction>(WeatherState()) {

    private val messageFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        getCachedForecast()
        loadCachedQuery()

        collect(settingsRepository.lang) { language ->
            setState { copy(lang = language.id) }
        }

        viewModelScope.launch {
            messageFlow.collect {
                setAction(WeatherMainAction.ShowToast(it))
            }
        }
    }

    fun getForecast(query: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, query = query) }
            val fetchedForecast = useCase.fetchForecastOrErrorMessage(query, state.value.lang)
            val weatherResponse = fetchedForecast.weatherResponse
            val message = fetchedForecast.message
            if (weatherResponse != null) {
                val isLocationSaved = isLocationSaved(weatherResponse)
                val weatherUi = uiMapper.mapToMainWeatherUi(weatherResponse)
                setState {
                    copy(
                        weatherData = weatherResponse,
                        weatherUi = weatherUi,
                        isLoading = false,
                        isAddButtonEnabled = !isLocationSaved,
                    )
                }
                return@launch
            } else if (message != null) {
                messageFlow.emit(message)
                setState {
                    copy(
                        isLoading = false,
                        isAddButtonEnabled = false,
                    )
                }
            }
        }
    }

    fun onUserInteraction(userInteraction: WeatherUserInteraction) {
        when (userInteraction) {
            WeatherUserInteraction.Location -> setAction(WeatherMainAction.NavigateToLocation)
            WeatherUserInteraction.NextDays -> setAction(WeatherMainAction.NavigateToNextDaysWeather)
            WeatherUserInteraction.Refresh -> getForecast(state.value.query)
            WeatherUserInteraction.SaveLocation -> saveLocation()
        }
    }

    private fun saveLocation() {
        viewModelScope.launch {
            if (state.value.weatherData == null) {
                onError(ActionResultProvider.FAIL)
                return@launch
            }
            val favoriteLocationDto =
                FavoriteLocationDto.buildFavoriteLocationObj(state.value.weatherData!!.location)
            val inserted = roomProvider.insert(favoriteLocationDto)
            if (inserted) onSuccess(ActionResultProvider.SAVED)
            else onError(ActionResultProvider.FAIL)
        }
    }

    private fun onError(code: Int) {
        viewModelScope.launch {
            val message = actionResultProvider.parseCode(code)
            messageFlow.emit(message)
        }
    }

    private fun onSuccess(code: Int) {
        val message = actionResultProvider.parseCode(code)
        viewModelScope.launch {
            messageFlow.emit(message)
            setState {
                copy(isAddButtonEnabled = false)
            }
        }

    }

    private suspend fun isLocationSaved(response: WeatherResponse): Boolean {
        val latLonKey = FavoriteLocationDto.generateKey(response.location)
        return roomProvider.containsPrimaryKey(latLonKey)
    }

    private fun getCachedForecast() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = sharedPrefsProvider.loadForecastFromCache()
            if (result != null) {
                val weatherUi = uiMapper.mapToMainWeatherUi(result)
                val isLocationSaved = isLocationSaved(result)
                setState {
                    copy(
                        isAddButtonEnabled = !isLocationSaved,
                        weatherUi = weatherUi,
                        weatherData = result,
                        isLoading = false
                    )
                }
            } else {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun loadCachedQuery() {
        val query = sharedPrefsProvider.loadQuery()
        setState { copy(query = query) }
    }

}