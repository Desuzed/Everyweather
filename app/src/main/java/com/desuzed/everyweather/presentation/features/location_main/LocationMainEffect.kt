package com.desuzed.everyweather.presentation.features.location_main

import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.presentation.base.SideEffect
import com.desuzed.everyweather.presentation.base.SnackBarEffect

sealed interface LocationMainEffect : SideEffect {
    class ShowSnackbar(override val data: QueryResult) : LocationMainEffect, SnackBarEffect
    data object FindUserLocation : LocationMainEffect
    data object NavigateToMapSelection : LocationMainEffect
    data object NavigateToSettings : LocationMainEffect
    data object NavigateBack : LocationMainEffect
    data object RequestLocationPermissions : LocationMainEffect
}