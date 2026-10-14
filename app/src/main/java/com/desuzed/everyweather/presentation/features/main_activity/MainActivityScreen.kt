package com.desuzed.everyweather.presentation.features.main_activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.presentation.features.main_activity.ui.BottomContentWidget
import com.desuzed.everyweather.presentation.features.main_activity.ui.DownloadingInAppUpdateWidget
import com.desuzed.everyweather.presentation.features.weather_main.WeatherMainScreen
import com.desuzed.everyweather.ui.elements.RegularText
import com.desuzed.everyweather.ui.elements.AppSnackbar
import com.desuzed.everyweather.ui.navigation.Destination
import com.desuzed.everyweather.ui.navigation.appNavGraph
import com.desuzed.everyweather.ui.theming.EveryweatherTheme

val LocalEdgeToEdgeInset = compositionLocalOf<Set<EdgeToEdgeInset>> { setOf() }

enum class EdgeToEdgeInset {
    Top, Bottom
}

@Composable
fun MainActivityScreen(
    state: MainActivityState,
    viewModel: MainActivityViewModel,
    snackbarHostState: SnackbarHostState,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    val edgeToEdgeInsets = buildSet {
        if (state.isInternetUnavailable) add(EdgeToEdgeInset.Top)
        if (state.isLookingForLocation || state.isUpdateLoading) add(EdgeToEdgeInset.Bottom)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                MainActivitySideEffect.UpdateAvailableDialog ->
                    navController.navigate(
                        Destination.InAppUpdateScreen.createRoute(InAppUpdateStatus.READY_TO_LAUNCH_UPDATE)
                    )
                MainActivitySideEffect.UpdateReadyToInstallDialog ->
                    navController.navigate(
                        Destination.InAppUpdateScreen.createRoute(InAppUpdateStatus.READY_TO_INSTALL)
                    )
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalEdgeToEdgeInset provides edgeToEdgeInsets) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.isInternetUnavailable) {
                    RegularText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EveryweatherTheme.colors.errorBackground)
                            .padding(vertical = dimensionResource(id = R.dimen.dimen_4))
                            .statusBarsPadding(),
                        text = stringResource(id = R.string.no_internet_connection),
                        color = EveryweatherTheme.colors.onErrorBackground,
                        textAlign = TextAlign.Center,
                    )
                }
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                ) {
                    ModalBottomSheetLayout(
                        bottomSheetNavigator = bottomSheetNavigator,
                        sheetShape = RoundedCornerShape(
                            topStart = dimensionResource(id = R.dimen.corner_radius_30),
                            topEnd = dimensionResource(id = R.dimen.corner_radius_30)
                        ),
                        sheetBackgroundColor = EveryweatherTheme.colors.tertiaryBackground,
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = WeatherMainScreen.destination.route,
                        ) {
                            appNavGraph(navController)
                        }
                    }
                }
                if (state.isLookingForLocation || state.isUpdateLoading) {
                    BottomContentWidget(isLookingForLocation = state.isLookingForLocation) {
                        DownloadingInAppUpdateWidget(
                            isDownloadingInProgress = state.isUpdateLoading,
                            totalBytes = state.totalBytes,
                            bytesDownloaded = state.bytesDownloaded,
                        )
                    }
                }
            }
        }
        AppSnackbar(snackbarState = snackbarHostState)
    }
}
