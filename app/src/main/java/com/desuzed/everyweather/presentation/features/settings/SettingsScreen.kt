package com.desuzed.everyweather.presentation.features.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.desuzed.everyweather.presentation.base.BaseComposeScreen
import com.desuzed.everyweather.presentation.features.settings.ui.SettingsScreen
import com.desuzed.everyweather.ui.navigation.Destination
import org.koin.androidx.compose.koinViewModel

object SettingsScreen : BaseComposeScreen<
        SettingsState,
        SettingsEffect,
        SettingsAction,
        SettingsViewModel>(
    initialState = SettingsState()
) {

    override val destination: Destination
        get() = Destination.SettingsScreen

    @Composable
    override fun ProvideScreenForNavGraph(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        SettingsComposeScreen(
            navController = navController,
            navBackStackEntry = navBackStackEntry
        )
    }

    @Composable
    private fun SettingsComposeScreen(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        viewModel: SettingsViewModel = koinViewModel()
    ) {
        ComposeScreen(
            viewModel = viewModel,
            backAction = SettingsAction.OnBackClick,
            onEffect = {
                when (it) {
                    SettingsEffect.NavigateBack -> navController.popBackStack()
                    is SettingsEffect.ShowReadyToInstallDialog ->
                        navController.navigate(Destination.InAppUpdateScreen.createRoute(it.status))
                    is SettingsEffect.ShowUpdateDialog ->
                        navController.navigate(Destination.InAppUpdateScreen.createRoute(it.status))
                }
            },
            content = { state ->
                SettingsScreen(
                    state = state,
                    onAction = viewModel::onAction,
                )
            },
        )
    }
}