package com.desuzed.everyweather.presentation.features.map_point_selection

import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.model.weather.Location
import com.desuzed.everyweather.presentation.base.State

data class MapPointSelectionState(
    val defaultMapPinLocation: Location? = null,
    val newSelectedLocation: UserLatLng? = null,
    val shouldShowSelectedPin: Boolean = false,
    val isConfirmDialogShown: Boolean = false,
    val isMapInitialized: Boolean = false,
) : State
