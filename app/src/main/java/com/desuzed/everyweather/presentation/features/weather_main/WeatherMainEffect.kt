package com.desuzed.everyweather.presentation.features.weather_main

import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.presentation.base.SideEffect
import com.desuzed.everyweather.presentation.base.SnackBarEffect

sealed interface WeatherMainEffect : SideEffect {
    class ShowSnackBar(override val data: QueryResult) : WeatherMainEffect, SnackBarEffect
    data object NavigateToLocation : WeatherMainEffect
    data object ExitApp : WeatherMainEffect
}