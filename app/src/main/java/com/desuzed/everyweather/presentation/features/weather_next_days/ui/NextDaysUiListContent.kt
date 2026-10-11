package com.desuzed.everyweather.presentation.features.weather_next_days.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysUi

@Composable
fun ColumnScope.NextDaysUiListContent(
    nextDaysWeatherData: List<NextDaysUi>?,
    onNextDayClick: (NextDaysUi) -> Unit,
) {
    nextDaysWeatherData?.forEachIndexed { _, forecastItem ->
        NextDayItem(
            dayItem = forecastItem,
            onNextDayClick = onNextDayClick,
        )
    }
}
