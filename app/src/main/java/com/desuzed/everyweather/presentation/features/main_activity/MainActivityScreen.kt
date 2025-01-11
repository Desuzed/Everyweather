package com.desuzed.everyweather.presentation.features.main_activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Surface
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.desuzed.everyweather.R
import com.desuzed.everyweather.presentation.features.main_activity.ui.BottomContentWidget
import com.desuzed.everyweather.presentation.features.main_activity.ui.DownloadingInAppUpdateWidget
import com.desuzed.everyweather.presentation.features.shared.SharedState
import com.desuzed.everyweather.presentation.features.weather_main.WeatherMainScreen
import com.desuzed.everyweather.ui.elements.RegularText
import com.desuzed.everyweather.ui.navigation.appNavGraph
import com.desuzed.everyweather.ui.theming.EveryweatherTheme

val LocalEdgeToEdgeInset = compositionLocalOf<Set<EdgeToEdgeInset>> { setOf() }

enum class EdgeToEdgeInset {
    Top, Bottom
}

// какие нужны диалоги
// - общий, вместо снекбаров. С Кнопками ок и ретрай. Как ловить ретрай колбэки? Общий кэш с подпиской?
// - для инап апдейта
// - кастом для ввода инфы, пусть управляется на экране?
// - диалог с картой? хотя карту вообще лучше вынести в экране, но как  и куда. Отдельный экран с поиском, скопированным с гео экрана?
// - кастом в настройках с радиобаттанами, тоже пусть управляется экраном
@Composable
fun MainActivityScreen(
    mainActivityState: MainActivityState,
    sharedState: SharedState,
) {
    val navController = rememberNavController()
    var localEdgeToEdgePaddingsProvided by remember {
        mutableStateOf(setOf<EdgeToEdgeInset>())
    }
    CompositionLocalProvider(LocalEdgeToEdgeInset provides localEdgeToEdgePaddingsProvided) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (mainActivityState.isInternetUnavailable) {
                localEdgeToEdgePaddingsProvided = addInsetToSet(
                    inset = EdgeToEdgeInset.Top,
                    set = localEdgeToEdgePaddingsProvided,
                )
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
            } else {
                localEdgeToEdgePaddingsProvided = deleteInsetFromSet(
                    inset = EdgeToEdgeInset.Top,
                    set = localEdgeToEdgePaddingsProvided,
                )
            }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = WeatherMainScreen.destination.route,
                ) {
                    appNavGraph(navController)
                }
            }
            if (mainActivityState.isLookingForLocation || sharedState.isUpdateLoading) {
                localEdgeToEdgePaddingsProvided = addInsetToSet(
                    inset = EdgeToEdgeInset.Bottom,
                    set = localEdgeToEdgePaddingsProvided,
                )
                BottomContentWidget(isLookingForLocation = mainActivityState.isLookingForLocation) {
                    DownloadingInAppUpdateWidget(
                        isDownloadingInProgress = sharedState.isUpdateLoading,
                        totalBytes = sharedState.totalBytes,
                        bytesDownloaded = sharedState.bytesDownloaded,
                    )
                }
            } else {
                localEdgeToEdgePaddingsProvided = deleteInsetFromSet(
                    inset = EdgeToEdgeInset.Bottom,
                    set = localEdgeToEdgePaddingsProvided,
                )
            }
        }
    }
}

private fun addInsetToSet(
    inset: EdgeToEdgeInset,
    set: Set<EdgeToEdgeInset>
): Set<EdgeToEdgeInset> {
    val mutableSet = set.toMutableSet()
    mutableSet.add(inset)

    return mutableSet
}

private fun deleteInsetFromSet(
    inset: EdgeToEdgeInset,
    set: Set<EdgeToEdgeInset>
): Set<EdgeToEdgeInset> {
    val mutableSet = set.toMutableSet()
    mutableSet.remove(inset)

    return mutableSet
}
