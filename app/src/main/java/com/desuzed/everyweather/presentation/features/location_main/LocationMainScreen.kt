package com.desuzed.everyweather.presentation.features.location_main

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.desuzed.everyweather.data.repository.providers.action_result.GeoActionResultProvider
import com.desuzed.everyweather.presentation.base.BaseComposeScreen
import com.desuzed.everyweather.presentation.features.location_main.ui.LocationMain
import com.desuzed.everyweather.ui.navigation.Destination
import com.desuzed.everyweather.ui.navigation.getMainActivity
import org.koin.androidx.compose.koinViewModel

object LocationMainScreen : BaseComposeScreen<
        LocationMainState,
        LocationMainEffect,
        LocationAction,
        LocationViewModel>(
    initialState = LocationMainState()
) {

    override val destination: Destination
        get() = Destination.LocationScreen

    @Composable
    override fun ProvideScreenForNavGraph(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        LocationMainComposeScreen(
            navController = navController,
        )
    }

    @Composable
    private fun LocationMainComposeScreen(
        navController: NavHostController,
        viewModel: LocationViewModel = koinViewModel()
    ) {
        val activity = getMainActivity()
        ComposeScreen(
            viewModel = viewModel,
            backAction = LocationAction.OnBackClick,
            snackBarParams = SnackBarParams(
                snackBarProviderClass = GeoActionResultProvider::class,
                snackBarRetryAction = LocationAction.FindByQuery,
                snackBarEffectClass = LocationMainEffect.ShowSnackbar::class,
            ),
            onEffect = {
                when (it) {
                    LocationMainEffect.FindUserLocation -> {
                        //TODO refactor
                        activity.findUserLocation()
                    }

                    LocationMainEffect.NavigateBack -> navController.popBackStack()
                    LocationMainEffect.NavigateToSettings -> navController.navigate(
                        route = Destination.SettingsScreen.route,
                    )

                    LocationMainEffect.RequestLocationPermissions -> {
                        //todo refactor
                        activity.requestLocationPermissions()
                    }

                    LocationMainEffect.NavigateToMapSelection -> {
                        navController.navigate(
                            route = Destination.MapSelectionScreen.route,
                        )
                    }

                    is LocationMainEffect.ShowSnackbar -> {
                        /** Эффект обрабатывается в обёртке ComposeScreen **/
                    }
                }
            },
            content = { state ->
                LocationMain(
                    state = state,
                    onAction = viewModel::onAction,
                )
            },
        )
    }
}