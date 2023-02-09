package com.desuzed.everyweather.presentation.features.weather_next_days

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter
import com.desuzed.everyweather.R
import com.desuzed.everyweather.data.mapper.UiMapper
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysMainInfo
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysUi
import com.desuzed.everyweather.ui.elements.BoldText
import com.desuzed.everyweather.ui.elements.CardDetailDayItem
import com.desuzed.everyweather.ui.elements.HourItemContent
import com.desuzed.everyweather.ui.elements.RegularText
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.MockWeatherObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun NextDaysBottomSheetContent(
    state: NextDaysState,
) {
    EveryweatherTheme {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val mappedWeatherUi = remember { mutableStateOf<List<NextDaysUi>?>(null) }
        coroutineScope.launch {
            mappedWeatherUi.value = withContext(Dispatchers.Default) {
                state.weather?.let {
                    UiMapper(
                        context = context,
                        windSpeed = state.windSpeed,
                        temperature = state.temperature,
                        language = state.language,
                        pressure = state.pressure,
                    ).mapToNextDaysUi(it)
                }
            }
        }

        Surface(
            modifier = Modifier.height(dimensionResource(id = R.dimen.dimen_350)),
            shape = RoundedCornerShape(
                topStart = dimensionResource(id = R.dimen.corner_radius_30),
                topEnd = dimensionResource(id = R.dimen.corner_radius_30)
            ),
            color = EveryweatherTheme.colors.bottomDialogBackground
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = dimensionResource(id = R.dimen.dimen_20),
                        start = dimensionResource(id = R.dimen.dimen_10),
                        end = dimensionResource(id = R.dimen.dimen_10),
                    )
            ) {
                val nextDaysWeather = mappedWeatherUi.value
                if (nextDaysWeather != null) {
                    val indexOfLastItem = nextDaysWeather.lastIndex
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        nextDaysWeather.forEach { forecastItem ->
                            val isLastElement =
                                nextDaysWeather.indexOf(forecastItem) == indexOfLastItem
                            ForecastListItem(dayItem = forecastItem, isLastItem = isLastElement)
                        }
                    }
                }
            }
        }

    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ForecastListItem(
    dayItem: NextDaysUi,
    isLastItem: Boolean,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        onClick = {
            isExpanded = !isExpanded
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .padding(
                top = dimensionResource(id = R.dimen.dimen_4),
                bottom = if (isLastItem) dimensionResource(id = R.dimen.dimen_8)
                else dimensionResource(id = R.dimen.dimen_0),
            )
            .fillMaxSize(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)),
        backgroundColor = EveryweatherTheme.colors.tertiary,
        elevation = dimensionResource(id = R.dimen.dimen_8),
    ) {
        Column(
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.dimen_10)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_10))
        ) {
            TopAreaDayItem(nextDaysMainInfo = dayItem.nextDaysMainInfo, isExpanded)
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    CardDetailDayItem(
                        detailCard = dayItem.detailCard,
                        Modifier.padding(horizontal = dimensionResource(id = R.dimen.dimen_20))
                    )
                    LazyRow {
                        items(items = dayItem.hourList) { hourItem ->
                            HourItemContent(hourItem = hourItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopAreaDayItem(nextDaysMainInfo: NextDaysMainInfo, isExpanded: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.dimen_20))
            .padding(top = dimensionResource(id = R.dimen.dimen_10)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_8))
        ) {
            BoldText(text = nextDaysMainInfo.date)
            RegularText(text = nextDaysMainInfo.description)
        }
        MaxMinTempWithImg(nextDaysMainInfo = nextDaysMainInfo, isExpanded)
    }
}

@Composable
fun MaxMinTempWithImg(nextDaysMainInfo: NextDaysMainInfo, isExpanded: Boolean) {
    var iconSize by remember { mutableStateOf(0) }
    Row(
        modifier = Modifier.onGloballyPositioned {
            iconSize = it.size.height
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_8))
    ) {
        Image(
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.dimen_40))
                .width(dimensionResource(id = R.dimen.dimen_40)),
            alignment = Alignment.CenterEnd,
            painter = rememberAsyncImagePainter(nextDaysMainInfo.iconUrl),
            contentDescription = "",
        )

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_8))
        ) {
            BoldText(text = nextDaysMainInfo.maxTemp)
            RegularText(text = nextDaysMainInfo.minTemp)
        }
        Image(
            modifier = Modifier.rotate(if (isExpanded) 0f else 180f),
            painter = painterResource(id = R.drawable.ic_arrow_24),
            contentDescription = "",
            alignment = Alignment.CenterEnd,
        )
    }

}

@Preview(
    showBackground = true,
    widthDp = 400,
    uiMode = UI_MODE_NIGHT_NO,
    name = "NextDaysBottomSheetContentPreview"
)
@Composable
private fun PreviewNextDaysBottomSheetContent() {
    val state = NextDaysState(
        weather = MockWeatherObject.weather
    )
    NextDaysBottomSheetContent(state)
}