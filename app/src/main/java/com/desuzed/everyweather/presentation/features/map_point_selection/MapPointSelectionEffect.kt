package com.desuzed.everyweather.presentation.features.map_point_selection

import com.desuzed.everyweather.presentation.base.SideEffect

sealed interface MapPointSelectionEffect : SideEffect {
    data object NavigateBackToWeather : MapPointSelectionEffect
    data object NavigateBack : MapPointSelectionEffect
}