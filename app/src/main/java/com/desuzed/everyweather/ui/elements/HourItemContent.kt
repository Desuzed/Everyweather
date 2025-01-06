package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.presentation.ui.HourUi
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.Constants.EMPTY_STRING
import com.desuzed.everyweather.util.MockWeatherObject

@Composable
fun HourItemContent(hourItem: HourUi) {
    Card(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.dimen_8))
            .wrapContentWidth(),
        shape = RoundedCornerShape(
            dimensionResource(id = R.dimen.corner_radius_16),
        ),
        backgroundColor = EveryweatherTheme.colors.surfacePrimary,
        elevation = dimensionResource(id = R.dimen.dimen_4)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.dimen_8)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmallText(text = hourItem.time)
            BoldText(text = hourItem.temp)
            Image(
                painter = rememberAsyncImagePainter(hourItem.iconUrl),
                contentDescription = EMPTY_STRING,
                modifier = Modifier.size(size = dimensionResource(id = R.dimen.dimen_34))
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_wind_direction),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(EveryweatherTheme.colors.onBackgroundPrimary),
                    modifier = Modifier
                        .rotate(hourItem.rotation)
                        .size(size = dimensionResource(id = R.dimen.dimen_12))
                )
                SmallText(
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.dimen_4)),
                    text = hourItem.wind
                )
            }
        }
    }
}

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        HourItemContent(
            HourUi(
                windSpeed = DistanceDimen.METRIC_KMH,
                temperature = TempDimen.CELCIUS,
                hour = MockWeatherObject.weather.forecastDay[0].hourForecast[0],
                timeZone = MockWeatherObject.weather.location.timezone,
                res = LocalContext.current.resources,
            )
        )
    }
}