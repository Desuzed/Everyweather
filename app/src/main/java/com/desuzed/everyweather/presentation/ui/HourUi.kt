package com.desuzed.everyweather.presentation.ui

import android.content.res.Resources
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.domain.model.weather.Hour
import com.desuzed.everyweather.domain.model.weather.HourMappingException
import com.desuzed.everyweather.domain.model.weather.WeatherContent
import com.desuzed.everyweather.presentation.ui.settings.SettingsMapper
import com.desuzed.everyweather.util.Constants.HTTPS_SCHEME
import com.desuzed.everyweather.util.DateFormatter
import com.desuzed.everyweather.util.DecimalFormatter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.math.roundToInt

class HourUi(
    windSpeed: DistanceDimen,
    temperature: TempDimen,
    hour: Hour,
    timeZone: String,
    res: Resources
) {

    val time: String
    val temp: String
    val wind: String
    val iconUrl: String
    val rotation: Float

    init {
        time = DateFormatter.format(
            pattern = DateFormatter.timePattern,
            timeInMills = hour.timeEpoch,
            timeZone = timeZone,
        )
        val tempHour = when (temperature) {
            TempDimen.CELCIUS -> hour.tempC.roundToInt()
            TempDimen.FAHRENHEIT -> hour.tempF.roundToInt()
        }
        temp = tempHour.toString() + res.getString(R.string.dot_temperature)
        val windHour = when (windSpeed) {
            DistanceDimen.METRIC_KMH -> hour.windSpeedKmh
            DistanceDimen.METRIC_MS -> hour.windSpeedKmh.times(DecimalFormatter.KPH_TO_MS_MULTIPLIER)
            DistanceDimen.IMPERIAL -> hour.windSpeedMph
        }
        val windSpeedUi = SettingsMapper.getSelectedWindSpeed(windSpeed)
        val formattedWindSpeed = DecimalFormatter.formatFloat(windHour)
        wind = "$formattedWindSpeed " + res.getString(windSpeedUi.valueStringId)
        iconUrl = "$HTTPS_SCHEME${hour.icon}"
        rotation = hour.windDegree.toFloat() - 180
    }

    companion object {
        /**
         *  Generates list for HourAdapter since current time and plus next 24 items
         */
        fun generateCurrentDayList(response: WeatherContent): List<Hour> {
            val hour = DateFormatter.format(
                pattern = DateFormatter.hourPattern,
                timeInMills = response.location.localtimeEpoch,
                timeZone = response.location.timezone,
            ).toInt()
            val forecastDay = response.forecastDay
            return try {
                forecastDay[0].hourForecast
                    .drop(hour)
                    .plus(forecastDay[1].hourForecast.take(hour))
            } catch (e: IndexOutOfBoundsException) {
                val error = HourMappingException(
                    "Could not map hour weather. Cause:\nlocation = " +
                            "${response.location.country}, ${response.location.region}, " +
                            response.location.name +
                            "\nlat lon = ${response.location.lat} ; ${response.location.lon}"
                )
                FirebaseCrashlytics.getInstance().recordException(error)
                emptyList()
            }
        }
    }

}