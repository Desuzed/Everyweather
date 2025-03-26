package com.desuzed.everyweather.presentation.features.main_activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
    state: MainActivityState,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    var localEdgeToEdgePaddingsProvided by remember {
        mutableStateOf(setOf<EdgeToEdgeInset>())
    }
    CompositionLocalProvider(LocalEdgeToEdgeInset provides localEdgeToEdgePaddingsProvided) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.isInternetUnavailable) {
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
                localEdgeToEdgePaddingsProvided = addInsetToSet(
                    inset = EdgeToEdgeInset.Bottom,
                    set = localEdgeToEdgePaddingsProvided,
                )
                BottomContentWidget(isLookingForLocation = state.isLookingForLocation) {
                    DownloadingInAppUpdateWidget(
                        isDownloadingInProgress = state.isUpdateLoading,
                        totalBytes = state.totalBytes,
                        bytesDownloaded = state.bytesDownloaded,
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
