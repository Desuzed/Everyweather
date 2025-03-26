package com.desuzed.everyweather.ui.navigation

sealed class Destination(
    val route: String,
) {
    data object SplashScreen : Destination("splash_screen")

    data object WeatherMainScreen : Destination("weather_main_screen")

    data object LocationScreen : Destination("location_screen")

    data object SettingsScreen : Destination("settings_screen")

    data object MapSelectionScreen : Destination("map_selection_screen")

    data object InAppUpdateScreen : Destination("in_app_update_screen")
    //data object InAppUpdateScreen : Destination("in_app_update_screen?InAppUpdateStatus={InAppUpdateStatus}")
    //data object InAppUpdateScreen : Destination("in_app_update_screen/{InAppUpdateStatus}")
}
