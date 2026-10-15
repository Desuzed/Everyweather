package com.desuzed.everyweather.presentation.features.weather_main.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.presentation.features.weather_main.WeatherAction
import com.desuzed.everyweather.presentation.ui.main.MainWeatherMapper
import com.desuzed.everyweather.presentation.ui.main.WeatherMainUi
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysMapper
import com.desuzed.everyweather.ui.elements.AppSnackbar
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.MockWeatherObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyWeatherCard_navigatesToLocation() {
        val actions = mutableListOf<WeatherAction>()

        composeRule.setContent {
            EveryweatherTheme { EmptyWeatherCard(onAction = actions::add) }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.choose_location)).performClick()

        assertEquals(WeatherAction.Location, actions.single())
    }

    @Test
    fun weatherContent_saveAndNextDayCardsDispatchTheirCallbacks() {
        val resources = composeRule.activity.resources
        val mainWeather = MainWeatherMapper(
            windSpeed = DistanceDimen.METRIC_KMH,
            temperature = TempDimen.CELCIUS,
            pressure = PressureDimen.MILLIMETERS,
            resources = resources,
        ).mapToMainWeatherUi(MockWeatherObject.weather)
        val nextDay = NextDaysMapper(
            windSpeed = DistanceDimen.METRIC_KMH,
            temperature = TempDimen.CELCIUS,
            language = Lang.EN,
            resources = resources,
            pressure = PressureDimen.MILLIMETERS,
        ).mapToNextDaysList(MockWeatherObject.weather).first()
        val actions = mutableListOf<WeatherAction>()
        val selectedDays = mutableListOf<com.desuzed.everyweather.presentation.ui.next_days.NextDaysUi>()

        composeRule.setContent {
            EveryweatherTheme {
                WeatherMainBody(
                    weatherData = remember { mutableStateOf(mainWeather) },
                    nextDaysUiList = remember { mutableStateOf(listOf(nextDay)) },
                    isLoading = false,
                    isAddButtonEnabled = true,
                    onAction = actions::add,
                    onNextDayClick = selectedDays::add,
                )
            }
        }

        composeRule.onNodeWithTag("save_location").performClick()
        composeRule.onNodeWithTag("next_day_${nextDay.nextDaysMainInfo.date}")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        assertEquals(WeatherAction.SaveLocation, actions.single())
        assertSame(nextDay, selectedDays.last())
    }

    @Test
    fun loadingState_hidesEmptyWeatherUntilLoadingCompletes() {
        var isLoading by mutableStateOf(true)
        val emptyHeader = composeRule.activity.getString(R.string.no_weather_data_is_loaded_header)

        composeRule.setContent {
            EveryweatherTheme {
                WeatherMainBody(
                    weatherData = remember { mutableStateOf<WeatherMainUi?>(null) },
                    nextDaysUiList = remember { mutableStateOf<List<com.desuzed.everyweather.presentation.ui.next_days.NextDaysUi>?>(null) },
                    isLoading = isLoading,
                    isAddButtonEnabled = false,
                    onAction = {},
                    onNextDayClick = {},
                )
            }
        }

        composeRule.onNodeWithText(emptyHeader).assertDoesNotExist()
        composeRule.runOnIdle { isLoading = false }
        composeRule.onNodeWithText(emptyHeader).assertExists()
    }

    @Test
    fun snackbarRetry_reportsActionPerformedToItsCaller() {
        val hostState = SnackbarHostState()
        var result: SnackbarResult? = null

        composeRule.setContent {
            EveryweatherTheme {
                LaunchedEffect(Unit) {
                    result = hostState.showSnackbar(message = "Location unavailable", actionLabel = "Retry")
                }
                Box { AppSnackbar(snackbarState = hostState) }
            }
        }

        composeRule.onNodeWithText("Retry").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { result == SnackbarResult.ActionPerformed }

        assertEquals(SnackbarResult.ActionPerformed, result)
    }
}
