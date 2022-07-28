package com.desuzed.everyweather.presentation.features.weather_main

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.desuzed.everyweather.MockWeatherObject
import com.desuzed.everyweather.R
import com.desuzed.everyweather.ui.elements.*
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.toIntDp
import com.desuzed.everyweather.presentation.ui.main.MainWeatherMapper
import com.desuzed.everyweather.presentation.ui.main.WeatherMainInfoUi
import com.desuzed.everyweather.presentation.ui.main.WeatherMainUi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Preview(
    showBackground = true,
    widthDp = 400,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "PreviewWeatherMainContent"
)
@Composable
private fun PreviewWeatherMainContent() {
    WeatherMainContent(
        state = WeatherState(
            weatherUi = MainWeatherMapper(resources = LocalContext.current.resources).mapToMainWeatherUi(
                MockWeatherObject.weather
            )
        ),
        onUserInteraction = {},
    )
}


@Composable
fun WeatherMainContent(
    state: WeatherState,
    onUserInteraction: (WeatherUserInteraction) -> Unit,
) {
    EveryweatherTheme {
        val refreshingState = rememberSwipeRefreshState(isRefreshing = state.isLoading)
        val fabSize = dimensionResource(id = R.dimen.dimen_50)
        Box(modifier = Modifier.fillMaxSize()) {
            var fabPadding by remember { mutableStateOf(0.dp) }
            SwipeRefresh(
                state = refreshingState,
                onRefresh = { onUserInteraction(WeatherUserInteraction.Refresh) },
            ) {
                if (state.weatherUi != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        WeatherHeaderInfo(
                            state.weatherUi.mainInfo,
                            onUserInteraction
                        ) { headerHeight ->
                            fabPadding = headerHeight.toIntDp.dp - fabSize / 2
                        }
                        BottomDetailWeather(state.weatherUi, onUserInteraction)
                    }
                    if (state.isAddButtonEnabled) {
                        FloatingButton(
                            modifier = Modifier
                                .padding(
                                    top = fabPadding,
                                    start = dimensionResource(id = R.dimen.dimen_20)
                                )
                                .size(size = fabSize),
                            id = R.drawable.ic_round_add_location_24,
                            onClick = { onUserInteraction(WeatherUserInteraction.SaveLocation) }
                        )
                    }
                } else {
                    if (!refreshingState.isRefreshing) {
                        EmptyWeatherCard(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .verticalScroll(rememberScrollState()),
                            onUserInteraction = onUserInteraction,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherHeaderInfo(
    mainInfoUi: WeatherMainInfoUi,
    onUserInteraction: (WeatherUserInteraction) -> Unit,
    onNewHeight: (Int) -> Unit,
) {
    GradientBox(
        modifier = Modifier.onGloballyPositioned {
            onNewHeight(it.size.height)
        },
        colors = listOf(
            EveryweatherTheme.colors.primaryGradientStart,
            EveryweatherTheme.colors.primaryGradientEnd,
        )
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.dimen_20))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_10))
        ) {
            LocationText(text = mainInfoUi.geoText, onUserInteraction)
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_16)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .height(dimensionResource(id = R.dimen.dimen_60))      //todo разобраться с размерами
                        .width(dimensionResource(id = R.dimen.dimen_60)),
                    alignment = Alignment.CenterEnd,
                    painter = rememberAsyncImagePainter(mainInfoUi.iconUrl),
                    contentDescription = "",
                )
                UltraLargeBoldText(
                    text = mainInfoUi.currentTemp,
                    color = EveryweatherTheme.colors.textColorSecondary
                )
            }
            MediumText(
                text = mainInfoUi.feelsLike,
                color = EveryweatherTheme.colors.textColorSecondary,
            )
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_8)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MediumText(
                    text = mainInfoUi.date,
                    color = EveryweatherTheme.colors.textColorSecondary,
                )
                DelimiterText()
                MediumBoldText(
                    text = mainInfoUi.time,
                    color = EveryweatherTheme.colors.textColorSecondary,
                )
            }
            MediumText(
                text = mainInfoUi.description,
                color = EveryweatherTheme.colors.textColorSecondary,
            )
        }
    }
}

@Composable
fun BottomDetailWeather(
    weatherUi: WeatherMainUi,
    onUserInteraction: (WeatherUserInteraction) -> Unit
) {
    GradientBox(
        colors = listOf(
            EveryweatherTheme.colors.secondaryGradientStart,
            EveryweatherTheme.colors.secondaryGradientEnd,
        )
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = dimensionResource(id = R.dimen.dimen_20),
                    start = dimensionResource(id = R.dimen.dimen_20),
                    end = dimensionResource(id = R.dimen.dimen_20),
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_10)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //todo придумать анимацию
            LazyRow {
                items(items = weatherUi.hourList) { hourItem ->
                    HourItemContent(hourItem = hourItem)
                }
            }
            CardDetailDayItem(detailCard = weatherUi.detailCard)
            RoundedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onUserInteraction(WeatherUserInteraction.NextDays) },
                text = stringResource(id = R.string.next_days_forecast)
            )
            LinkText(Modifier.padding(bottom = dimensionResource(id = R.dimen.dimen_20)))
        }
    }

}

@Composable
fun EmptyWeatherCard(onUserInteraction: (WeatherUserInteraction) -> Unit, modifier: Modifier) {
    GradientBox(
        colors = listOf(
            EveryweatherTheme.colors.secondaryGradientStart,
            EveryweatherTheme.colors.secondaryGradientEnd,
        )
    ) {
        Card(
            modifier = modifier
                .padding(dimensionResource(id = R.dimen.dimen_20))
                .align(Alignment.Center),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)),
            backgroundColor = EveryweatherTheme.colors.onSurface,
            elevation = dimensionResource(id = R.dimen.dimen_4),
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.dimen_20)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_20)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BoldText(
                    text = stringResource(id = R.string.no_weather_data_is_loaded_header),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
                RegularText(text = stringResource(id = R.string.no_weather_data_is_loaded))
                RoundedButton(
                    onClick = { onUserInteraction(WeatherUserInteraction.SaveLocation) },
                    text = stringResource(id = R.string.choose_location)
                )
            }
        }
    }

}