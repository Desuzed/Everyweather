package com.desuzed.everyweather.ui.navigation

import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus

sealed class Destination(
    val route: String,
) {
    data object SplashScreen : Destination("splash_screen")

    data object WeatherMainScreen : Destination("weather_main_screen")

    data object LocationScreen : Destination("location_screen")

    data object SettingsScreen : Destination("settings_screen")

    data object MapSelectionScreen : Destination("map_selection_screen")

    data object InAppUpdateScreen : Destination("in_app_update_screen/{inAppUpdateStatus}") {
        fun createRoute(status: InAppUpdateStatus): String =
            "in_app_update_screen/${status.name}"
    }
}
