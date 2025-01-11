package com.desuzed.everyweather.presentation.features.map_point_selection

import com.desuzed.everyweather.analytics.MapPointSelectionAnalytics
import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.presentation.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

class MapPointSelectionViewModel(
    private val weatherDataRepository: WeatherDataRepository,
    private val analytics: MapPointSelectionAnalytics,
) : BaseViewModel<MapPointSelectionState, MapPointSelectionEffect, MapPointSelectionAction>(
    MapPointSelectionState()
) {

    init {
        initSelectedPin()
    }

    override fun onAction(action: MapPointSelectionAction) {
        analytics.onAction(action)
        when (action) {
            MapPointSelectionAction.DismissConfirmPinDialog -> onDismissConfirmPinDialog()
            MapPointSelectionAction.NewLocationConfirm -> onNewLocationConfirm()
            MapPointSelectionAction.OnBackClick -> navigateBack()
            MapPointSelectionAction.OnMapInitialized -> setState { copy(isMapInitialized = true) }
            is MapPointSelectionAction.NewLocationPicked -> onNewLocationPicked(action.location)
        }
    }

    private fun initSelectedPin() {
        launch {
            val location = weatherDataRepository.getWeatherContentFlow().firstOrNull()?.location
            setState { copy(defaultMapPinLocation = location) }
        }
    }

    private fun onNewLocationPicked(newLocation: UserLatLng) {
        setState {
            copy(
                newSelectedLocation = newLocation,
                isConfirmDialogShown = true,
            )
        }
    }

    private fun onDismissConfirmPinDialog() {
        setState { copy(newSelectedLocation = null) }
        onDismissDialog()
    }

    private fun onNewLocationConfirm() {
        launch {
            onDismissDialog()
            val latLng = state.value.newSelectedLocation
            setState { copy(defaultMapPinLocation = null, shouldShowSelectedPin = true) }
            delay(ONE_SEC)
            setState { copy() }
            if (latLng != null) {
                val userLatLng = latLng.copy(time = System.currentTimeMillis())
                saveQueryAndNavigateBackToWeather(
                    query = userLatLng.toString(),
                    userLatLng = userLatLng,
                )
            }
        }
    }

    private fun saveQueryAndNavigateBackToWeather(query: String, userLatLng: UserLatLng? = null) {
        launch {
            weatherDataRepository.saveQuery(
                query = query,
                shouldTriggerWeatherRequest = true,
                userLatLng = userLatLng,
            )
            navigateBackToWeather()
        }
    }

    private fun navigateBackToWeather() {
        setSideEffect(MapPointSelectionEffect.NavigateBackToWeather)
    }

    private fun navigateBack() {
        setSideEffect(effect = MapPointSelectionEffect.NavigateBack)
    }

    private fun onDismissDialog() {
        setState { copy(isConfirmDialogShown = false) }
    }

    companion object {
        private const val ONE_SEC = 1000L
    }

}