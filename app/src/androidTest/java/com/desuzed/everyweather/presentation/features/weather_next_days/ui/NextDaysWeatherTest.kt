package com.desuzed.everyweather.presentation.features.weather_next_days.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysMapper
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysUi
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.MockWeatherObject
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class NextDaysWeatherTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nextDayList_rendersForecastAndDispatchesSelectedDay() {
        val day = nextDays().first()
        val selected = mutableListOf<NextDaysUi>()

        composeRule.setContent {
            EveryweatherTheme {
                Column {
                    NextDaysUiListContent(
                        nextDaysWeatherData = listOf(day),
                        onNextDayClick = selected::add,
                    )
                }
            }
        }

        composeRule.onNodeWithTag("next_day_${day.nextDaysMainInfo.date}").assertExists().performClick()
        composeRule.onNodeWithText(day.nextDaysMainInfo.date).assertIsDisplayed()

        assertSame(day, selected.single())
    }

    @Test
    fun nextDayBottomSheet_rendersDetailsAndHourlyForecast() {
        val day = nextDays().first().let { source ->
            source.copy(hourList = listOf(source.hourList.first()))
        }

        composeRule.setContent {
            EveryweatherTheme {
                NextDayBottomSheetContent(dayItem = day)
            }
        }

        composeRule.onNodeWithText(day.nextDaysMainInfo.date).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.humidity)).assertIsDisplayed()
        composeRule.onNodeWithText(day.detailCard.humidity).assertIsDisplayed()
        composeRule.onNodeWithText(day.hourList.first().time).assertIsDisplayed()
        composeRule.onNodeWithText(day.hourList.first().temp).assertIsDisplayed()
    }

    @Test
    fun nextDayBottomSheet_backDismissesAndCallsDismissRequest() {
        val day = nextDays().first()
        var dismissed by mutableStateOf(false)

        composeRule.setContent {
            EveryweatherTheme {
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                    confirmValueChange = { true },
                )
                LaunchedEffect(Unit) {
                    sheetState.show()
                }
                if (!dismissed) {
                    NextDaysWeatherBottomSheet(
                        sheetState = sheetState,
                        dayItem = day,
                        onBackClick = { dismissed = true },
                    )
                }
            }
        }

        composeRule.onNodeWithText(day.nextDaysMainInfo.date).assertIsDisplayed()
        pressBack()
        composeRule.runOnIdle { }

        assertTrue(dismissed)
        composeRule.onNodeWithText(day.nextDaysMainInfo.date).assertDoesNotExist()
    }

    private fun nextDays(): List<NextDaysUi> = NextDaysMapper(
        windSpeed = DistanceDimen.METRIC_KMH,
        temperature = TempDimen.CELCIUS,
        language = Lang.EN,
        resources = composeRule.activity.resources,
        pressure = PressureDimen.MILLIMETERS,
    ).mapToNextDaysList(MockWeatherObject.weather)
}
