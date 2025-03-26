package com.desuzed.everyweather.presentation.features.weather_main

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.desuzed.everyweather.data.repository.providers.action_result.WeatherActionResultProvider
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.presentation.base.BaseComposeScreen
import com.desuzed.everyweather.presentation.base.navigate
import com.desuzed.everyweather.presentation.features.in_app_update.InAppUpdateScreen
import com.desuzed.everyweather.presentation.features.weather_main.ui.WeatherMain
import com.desuzed.everyweather.ui.navigation.Destination
import com.desuzed.everyweather.ui.navigation.getMainActivity
import org.koin.androidx.compose.koinViewModel

object WeatherMainScreen : BaseComposeScreen<
        WeatherState,
        WeatherMainEffect,
        WeatherAction,
        WeatherMainViewModel>(
    initialState = WeatherState()
) {

    override val destination: Destination
        get() = Destination.WeatherMainScreen

    @Composable
    override fun ProvideScreenForNavGraph(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        WeatherMainComposeScreen(
            navController = navController,
            navBackStackEntry = navBackStackEntry
        )
    }

    @Composable
    private fun WeatherMainComposeScreen(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        viewModel: WeatherMainViewModel = koinViewModel()
    ) {
        val activity = getMainActivity()

        ComposeScreen(
            viewModel = viewModel,
            backAction = WeatherAction.BackClick,
            snackBarParams = SnackBarParams(
                snackBarProviderClass = WeatherActionResultProvider::class,
                snackBarRetryAction = WeatherAction.Refresh,
                snackBarEffectClass = WeatherMainEffect.ShowSnackBar::class
            ),
            onEffect = {
                when (it) {
                    WeatherMainEffect.NavigateToLocation -> navController.navigate(
                        screen = InAppUpdateScreen,
                        argName = "InAppUpdateStatus",
                        argValue = InAppUpdateStatus.READY_TO_INSTALL.name,
                    )

                    WeatherMainEffect.NavigateToNextDaysWeather -> {

                    }

                    is WeatherMainEffect.ShowSnackBar -> {
                        /** Эффект обрабатывается в обёртке ComposeScreen **/
                    }

                    WeatherMainEffect.ExitApp -> activity.finish()
                }
            },
            content = { state ->
                WeatherMain(
                    state = state,
                    onAction = viewModel::onAction,
                )
            },
        )
    }
}