package com.desuzed.everyweather.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.desuzed.everyweather.presentation.base.composeScreenDestination
import com.desuzed.everyweather.presentation.features.location_main.LocationMainScreen
import com.desuzed.everyweather.presentation.features.map_point_selection.MapPointSelectionScreen
import com.desuzed.everyweather.presentation.features.settings.SettingsScreen
import com.desuzed.everyweather.presentation.features.weather_main.WeatherMainScreen

fun NavGraphBuilder.appNavGraph(
    navController: NavHostController,
) = apply {

//    composable(Destination.SplashScreen.route) {
//        SplashScreen(
//            navController = navController,
//        )
//    }

    composeScreenDestination(WeatherMainScreen, navController)

    composeScreenDestination(LocationMainScreen, navController)

    composeScreenDestination(SettingsScreen, navController)

    composeScreenDestination(MapPointSelectionScreen, navController)

}

