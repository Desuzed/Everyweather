package com.desuzed.everyweather.presentation.features.weather_next_days.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.presentation.features.weather_main.WeatherState
import com.desuzed.everyweather.presentation.features.weather_main.ui.WeatherMain
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysMapper
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.MockWeatherObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherForecastFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun weatherMain_nextDayCardOpensDetails_andBackLeavesForecastCard() {
        val weatherFixture = MockWeatherObject.weather.copy(
            forecastDay = listOf(MockWeatherObject.forecastDay),
        )
        val expectedDay = NextDaysMapper(
            windSpeed = DistanceDimen.METRIC_KMH,
            temperature = TempDimen.CELCIUS,
            language = Lang.EN,
            resources = composeRule.activity.resources,
            pressure = PressureDimen.MILLIMETERS,
        ).mapToNextDaysList(weatherFixture).first()

        composeRule.setContent {
            EveryweatherTheme {
                WeatherMain(
                    state = WeatherState(
                        weatherData = weatherFixture,
                        isLoading = false,
                        selectedLang = Lang.EN,
                        windSpeed = DistanceDimen.METRIC_KMH,
                        temperature = TempDimen.CELCIUS,
                        pressure = PressureDimen.MILLIMETERS,
                    ),
                    onAction = {},
                )
            }
        }

        val cardTag = "next_day_${expectedDay.nextDaysMainInfo.date}"
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(cardTag).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(cardTag)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(expectedDay.detailCard.humidity)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(expectedDay.detailCard.humidity).assertIsDisplayed()

        pressBack()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(expectedDay.nextDaysMainInfo.date)
                .fetchSemanticsNodes().size == 1
        }
        composeRule.onNodeWithTag(cardTag).assertIsDisplayed()
        composeRule.onNodeWithText(expectedDay.detailCard.humidity).assertDoesNotExist()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.next_days_forecast))
            .assertIsDisplayed()
    }
}
