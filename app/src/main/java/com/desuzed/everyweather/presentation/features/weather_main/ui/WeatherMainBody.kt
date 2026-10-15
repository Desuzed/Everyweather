package com.desuzed.everyweather.presentation.features.weather_main.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.desuzed.everyweather.R
import com.desuzed.everyweather.presentation.features.weather_main.WeatherAction
import com.desuzed.everyweather.presentation.ui.main.WeatherMainUi
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysUi
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.elements.FloatingButton
import com.desuzed.everyweather.ui.extensions.topEdgeToEdgePadding
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.toIntDp

@AppPreview
@Composable
private fun Preview() {
    WeatherMainBody(
        weatherData = remember { mutableStateOf(null) },
        nextDaysUiList = remember { mutableStateOf(null) },
        isLoading = false,
        isAddButtonEnabled = false,
        onAction = {},
        onNextDayClick = {},
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WeatherMainBody(
    weatherData: State<WeatherMainUi?>,
    nextDaysUiList: State<List<NextDaysUi>?>,
    isLoading: Boolean,
    isAddButtonEnabled: Boolean,
    onAction: (WeatherAction) -> Unit,
    onNextDayClick: (NextDaysUi) -> Unit,
) {
    val refreshingState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { onAction(WeatherAction.Refresh) },
    )
    val fabSize = dimensionResource(id = R.dimen.dimen_50)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("weather_main_content"),
    ) {
        var fabPadding by remember { mutableStateOf(0.dp) }
        val weatherUi = weatherData.value
        if (weatherUi != null) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                WeatherHeaderInfo(
                    mainInfoUi = weatherUi.mainInfo,
                    onAction = onAction,
                    refreshingState = refreshingState,
                ) { headerHeight ->
                    fabPadding = headerHeight.toIntDp.dp - fabSize / 2
                }
                BottomDetailWeather(
                    weatherUi = weatherUi,
                    refreshingState = refreshingState,
                    nextDaysUiList = nextDaysUiList,
                    onNextDayClick = onNextDayClick,
                    onAction = onAction,
                )

            }
            if (isAddButtonEnabled) {
                FloatingButton(
                    modifier = Modifier
                        .padding(
                            top = fabPadding,
                            start = dimensionResource(id = R.dimen.dimen_20)
                        )
                        .size(size = fabSize)
                        .testTag("save_location"),
                    id = R.drawable.ic_round_add_location_24,
                    onClick = { onAction(WeatherAction.SaveLocation) }
                )
            }
        } else {
            if (!isLoading) {
                EmptyWeatherCard(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .pullRefresh(refreshingState)
                        .verticalScroll(rememberScrollState()),
                    onAction = onAction,
                )
            }
        }
        PullRefreshIndicator(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .topEdgeToEdgePadding()
                .padding(top = dimensionResource(id = R.dimen.dimen_20)),
            refreshing = isLoading,
            state = refreshingState,
            backgroundColor = EveryweatherTheme.colors.primaryBackground.first(),
            contentColor = EveryweatherTheme.colors.primary,
        )
    }

}
