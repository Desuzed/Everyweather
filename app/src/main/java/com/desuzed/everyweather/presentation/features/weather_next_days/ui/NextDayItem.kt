package com.desuzed.everyweather.presentation.features.weather_next_days.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.presentation.ui.next_days.DetailCardNextDays
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysMainInfo
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysUi
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.elements.RoundedCardItem
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.MockWeatherObject
import java.util.TimeZone

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        NextDayItem(
            dayItem = NextDaysUi(
                nextDaysMainInfo = NextDaysMainInfo(
                    language = Lang.RU,
                    temperature = TempDimen.CELCIUS,
                    forecastDay = MockWeatherObject.forecastDay,
                    timeZone = TimeZone.getDefault().toString(),
                    res = LocalContext.current.resources,
                ),
                detailCard = DetailCardNextDays(
                    windSpeed = DistanceDimen.METRIC_MS,
                    forecastDay = MockWeatherObject.forecastDay,
                    pressureDimen = PressureDimen.INCHES,
                    res = LocalContext.current.resources
                ),
                hourList = listOf(),
            ),
            onNextDayClick = {},
        )
    }
}

@Composable
fun NextDayItem(
    dayItem: NextDaysUi,
    onNextDayClick: (NextDaysUi) -> Unit,
) {
    RoundedCardItem(
        onClick = {
            Log.e("CLICK", "NextDayItem: CLICK", )
            onNextDayClick(dayItem)
        }
    ) {
        NextDayItemContent(nextDaysMainInfo = dayItem.nextDaysMainInfo)
    }
}