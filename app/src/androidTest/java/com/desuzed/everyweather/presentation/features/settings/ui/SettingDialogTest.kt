package com.desuzed.everyweather.presentation.features.settings.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.pressBack
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.SettingsType
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.presentation.features.settings.SettingsAction
import com.desuzed.everyweather.presentation.ui.settings.SettingsMapper
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingDialogTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun languagePicker_dispatchesChosenLanguage() {
        val actions = mutableListOf<SettingsAction>()

        composeRule.setContent {
            EveryweatherTheme {
                SettingDialog(
                    showDialogType = SettingsType.LANG,
                    settings = testSettings(),
                    onAction = actions::add,
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.english)).performClick()

        assertEquals(Lang.EN, (actions.single() as SettingsAction.ChangeLanguage).lang)
    }

    @Test
    fun darkModePicker_dispatchesChosenMode_andBackDismissesSheet() {
        val actions = mutableListOf<SettingsAction>()
        var dismissed = false

        composeRule.setContent {
            EveryweatherTheme {
                SettingDialog(
                    showDialogType = SettingsType.DARK_MODE,
                    settings = testSettings(),
                    onAction = actions::add,
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.on)).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.system)).performClick()
        pressBack()
        composeRule.runOnIdle { }

        assertEquals(DarkMode.ON, (actions.first() as SettingsAction.ChangeDarkMode).darkMode)
        assertEquals(DarkMode.SYSTEM, (actions.last() as SettingsAction.ChangeDarkMode).darkMode)
        assertTrue(dismissed)
    }

    @Test
    fun temperaturePicker_dispatchesChosenTemperature() {
        val actions = mutableListOf<SettingsAction>()

        composeRule.setContent {
            EveryweatherTheme {
                SettingDialog(
                    showDialogType = SettingsType.TEMP,
                    settings = testSettings(),
                    onAction = actions::add,
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.fahrenheit)).performClick()

        assertEquals(
            TempDimen.FAHRENHEIT,
            (actions.single() as SettingsAction.ChangeTemperatureDimension).tempDimen,
        )
    }

    @Test
    fun windSpeedPicker_dispatchesChosenDimension() {
        val actions = mutableListOf<SettingsAction>()

        composeRule.setContent {
            EveryweatherTheme {
                SettingDialog(
                    showDialogType = SettingsType.DISTANCE,
                    settings = testSettings(),
                    onAction = actions::add,
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.ms)).performClick()

        assertEquals(
            DistanceDimen.METRIC_MS,
            (actions.single() as SettingsAction.ChangeDistanceDimension).distanceDimen,
        )
    }

    @Test
    fun pressurePicker_dispatchesChosenDimension() {
        val actions = mutableListOf<SettingsAction>()

        composeRule.setContent {
            EveryweatherTheme {
                SettingDialog(
                    showDialogType = SettingsType.PRESSURE,
                    settings = testSettings(),
                    onAction = actions::add,
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.inch)).performClick()

        assertEquals(
            PressureDimen.INCHES,
            (actions.single() as SettingsAction.ChangePressureDimension).pressureDimen,
        )
    }

    private fun testSettings(): TestSettings {
        val params = SettingsMapper.getSettingsUiParams(
            selectedLang = Lang.RU,
            selectedMode = DarkMode.OFF,
            selectedDistanceDimen = DistanceDimen.METRIC_KMH,
            selectedTempDimen = TempDimen.CELCIUS,
            selectedPressureDimen = PressureDimen.MILLIBAR,
            distanceDimenList = DistanceDimen.entries,
            tempList = TempDimen.entries,
            pressureList = PressureDimen.entries,
            langList = Lang.entries,
            darkModeList = DarkMode.entries,
        )
        return TestSettings(params)
    }

    private data class TestSettings(
        val params: com.desuzed.everyweather.presentation.ui.settings.SettingsUiParams,
    )

    @androidx.compose.runtime.Composable
    private fun SettingDialog(
        showDialogType: SettingsType,
        settings: TestSettings,
        onAction: (SettingsAction) -> Unit,
        onDismiss: () -> Unit,
    ) {
        val params = settings.params
        SettingDialog(
            showDialogType = showDialogType,
            language = params.selectedLang,
            windSpeed = params.selectedDistance,
            temperature = params.selectedTemp,
            darkTheme = params.selectedMode,
            pressure = params.selectedPressure,
            langDialogItems = params.systemSettingsList.languageList,
            darkModeDialogItems = params.systemSettingsList.darkModeList,
            temperatureDialogItems = params.weatherUiList.temperatureSettingsList,
            distanceDialogItems = params.weatherUiList.distanceSettingsList,
            pressureDialogItems = params.weatherUiList.pressureSettingsList,
            onAction = onAction,
            onDismiss = onDismiss,
        )
    }
}
