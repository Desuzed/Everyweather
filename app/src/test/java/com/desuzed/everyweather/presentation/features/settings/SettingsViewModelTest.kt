package com.desuzed.everyweather.presentation.features.settings

import com.desuzed.everyweather.analytics.SettingsAnalytics
import com.desuzed.everyweather.data.repository.providers.app_update.AppUpdateProvider
import com.desuzed.everyweather.domain.interactor.SystemSettingsInteractor
import com.desuzed.everyweather.domain.interactor.WeatherSettingsInteractor
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.SettingsType
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var weatherSettings: WeatherSettingsInteractor
    private lateinit var systemSettings: SystemSettingsInteractor
    private lateinit var analytics: SettingsAnalytics
    private lateinit var updates: AppUpdateProvider

    @Before
    fun setUp() {
        weatherSettings = mockk(relaxed = true)
        systemSettings = mockk(relaxed = true)
        analytics = mockk(relaxed = true)
        updates = mockk(relaxed = true)
        every { systemSettings.lang } returns flowOf(Lang.EN)
        every { systemSettings.darkMode } returns flowOf(DarkMode.SYSTEM)
        every { weatherSettings.distanceDimen } returns flowOf(DistanceDimen.METRIC_KMH)
        every { weatherSettings.tempDimen } returns flowOf(TempDimen.CELCIUS)
        every { weatherSettings.pressureDimen } returns flowOf(PressureDimen.MILLIMETERS)
        every { updates.appUpdateState } returns flowOf(null)
    }

    @Test
    fun `all settings actions persist values and dismiss their dialog`() = runTest {
        val viewModel = SettingsViewModel(weatherSettings, systemSettings, analytics, updates)
        advanceUntilIdle()

        viewModel.onAction(SettingsAction.ShowSettingDialog(SettingsType.LANG))
        viewModel.onAction(SettingsAction.ChangeLanguage(Lang.RU))
        viewModel.onAction(SettingsAction.ShowSettingDialog(SettingsType.DARK_MODE))
        viewModel.onAction(SettingsAction.ChangeDarkMode(DarkMode.ON))
        viewModel.onAction(SettingsAction.ShowSettingDialog(SettingsType.DISTANCE))
        viewModel.onAction(SettingsAction.ChangeDistanceDimension(DistanceDimen.IMPERIAL))
        viewModel.onAction(SettingsAction.ShowSettingDialog(SettingsType.TEMP))
        viewModel.onAction(SettingsAction.ChangeTemperatureDimension(TempDimen.FAHRENHEIT))
        viewModel.onAction(SettingsAction.ShowSettingDialog(SettingsType.PRESSURE))
        viewModel.onAction(SettingsAction.ChangePressureDimension(PressureDimen.INCHES))
        advanceUntilIdle()

        coVerify { systemSettings.setLanguage(Lang.RU) }
        coVerify { systemSettings.setDarkMode(DarkMode.ON) }
        coVerify { weatherSettings.setDistanceDimension(DistanceDimen.IMPERIAL) }
        coVerify { weatherSettings.setTemperatureDimension(TempDimen.FAHRENHEIT) }
        coVerify { weatherSettings.setPressureDimension(PressureDimen.INCHES) }
        assertNull(viewModel.state.value.showDialogType)
    }

    @Test
    fun `dismiss action clears selected dialog`() = runTest {
        val viewModel = SettingsViewModel(weatherSettings, systemSettings, analytics, updates)
        viewModel.onAction(SettingsAction.ShowSettingDialog(SettingsType.PRESSURE))
        viewModel.onAction(SettingsAction.DismissDialog)
        assertNull(viewModel.state.value.showDialogType)
    }

    @Test
    fun `settings flows update displayed state`() = runTest {
        every { systemSettings.lang } returns flowOf(Lang.RU)
        every { systemSettings.darkMode } returns flowOf(DarkMode.ON)
        every { weatherSettings.distanceDimen } returns flowOf(DistanceDimen.METRIC_MS)
        every { weatherSettings.tempDimen } returns flowOf(TempDimen.FAHRENHEIT)
        every { weatherSettings.pressureDimen } returns flowOf(PressureDimen.INCHES)

        val viewModel = SettingsViewModel(weatherSettings, systemSettings, analytics, updates)
        advanceUntilIdle()

        assertEquals(Lang.RU, viewModel.state.value.selectedLang)
        assertEquals(DarkMode.ON, viewModel.state.value.selectedMode)
        assertEquals(DistanceDimen.METRIC_MS, viewModel.state.value.selectedDistanceDimen)
        assertEquals(TempDimen.FAHRENHEIT, viewModel.state.value.selectedTempDimen)
        assertEquals(PressureDimen.INCHES, viewModel.state.value.selectedPressure)
    }
}
