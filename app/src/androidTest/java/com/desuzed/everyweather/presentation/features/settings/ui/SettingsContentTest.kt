package com.desuzed.everyweather.presentation.features.settings.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.SettingsType
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.presentation.features.settings.SettingsAction
import com.desuzed.everyweather.presentation.ui.settings.SettingsMapper
import com.desuzed.everyweather.presentation.ui.settings.SettingsUiParams
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsBody_displaysCurrentValues_andMenuClickOpensSettingDialog() {
        val actions = mutableListOf<SettingsAction>()

        composeRule.setContent {
            EveryweatherTheme {
                SettingsScreenBody(
                    settingsParams = testSettings(),
                    updateStatus = null,
                    onAction = actions::add,
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.settings)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.english)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.system)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.celcius)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.kmh)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.mmhg)).assertIsDisplayed()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.language)).performClick()

        assertEquals(SettingsType.LANG, (actions.single() as SettingsAction.ShowSettingDialog).type)
    }

    @Test
    fun settingsBody_readyToLaunchUpdate_displaysButton_andDispatchesAction() {
        val actions = mutableListOf<SettingsAction>()

        composeRule.setContent {
            EveryweatherTheme {
                SettingsScreenBody(
                    settingsParams = testSettings(),
                    updateStatus = InAppUpdateStatus.READY_TO_LAUNCH_UPDATE,
                    onAction = actions::add,
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.update_available_title)
        ).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.update_available_update_in_background_button)
        ).performScrollTo().performClick()

        assertEquals(SettingsAction.ReadyToLaunchUpdate, actions.single())
    }

    @Test
    fun settingsBody_readyToInstall_displaysInstallButton_andDispatchesAction() {
        val actions = mutableListOf<SettingsAction>()

        composeRule.setContent {
            EveryweatherTheme {
                SettingsScreenBody(
                    settingsParams = testSettings(),
                    updateStatus = InAppUpdateStatus.READY_TO_INSTALL,
                    onAction = actions::add,
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.update_ready_to_install_title)
        ).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.update_install_button))
            .performScrollTo()
            .performClick()

        assertEquals(SettingsAction.ReadyToInstall, actions.single())
    }

    @Test
    fun settingsBody_withoutUpdate_doesNotDisplayUpdateControls() {
        composeRule.setContent {
            EveryweatherTheme {
                SettingsScreenBody(
                    settingsParams = testSettings(),
                    updateStatus = null,
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.update_install_button))
            .assertDoesNotExist()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.update_available_update_in_background_button)
        ).assertDoesNotExist()
    }

    private fun testSettings(): SettingsUiParams = SettingsMapper.getSettingsUiParams(
        selectedLang = Lang.EN,
        selectedMode = DarkMode.SYSTEM,
        selectedDistanceDimen = DistanceDimen.METRIC_KMH,
        selectedTempDimen = TempDimen.CELCIUS,
        selectedPressureDimen = PressureDimen.MILLIMETERS,
        distanceDimenList = DistanceDimen.entries,
        tempList = TempDimen.entries,
        pressureList = PressureDimen.entries,
        langList = Lang.entries,
        darkModeList = DarkMode.entries,
    )
}
