package com.desuzed.everyweather.presentation.features.map_point_selection

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.desuzed.everyweather.presentation.base.BaseComposeScreen
import com.desuzed.everyweather.presentation.features.map_point_selection.ui.MapPointSelectionScreenContent
import com.desuzed.everyweather.ui.navigation.Destination
import org.koin.androidx.compose.koinViewModel

object MapPointSelectionScreen : BaseComposeScreen<
        MapPointSelectionState,
        MapPointSelectionEffect,
        MapPointSelectionAction,
        MapPointSelectionViewModel>(
    initialState = MapPointSelectionState()
) {
    override val destination: Destination
        get() = Destination.MapSelectionScreen

    @Composable
    override fun ProvideScreenForNavGraph(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        MapPointSelectionComposeScreen(navController)
    }

    @Composable
    private fun MapPointSelectionComposeScreen(
        navController: NavHostController,
        viewModel: MapPointSelectionViewModel = koinViewModel()
    ) {
        ComposeScreen(viewModel = viewModel, onEffect = {
            when (it) {
                MapPointSelectionEffect.NavigateBackToWeather -> navController.popBackStack(
                    route = Destination.WeatherMainScreen.route,
                    inclusive = false,
                )

                MapPointSelectionEffect.NavigateBack -> navController.popBackStack()
            }
        }) {
            MapPointSelectionScreenContent(
                state = it,
                onAction = viewModel::onAction,
            )
        }
    }
}