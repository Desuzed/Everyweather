package com.desuzed.everyweather.presentation.features.settings.persistence

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.desuzed.everyweather.R
import com.desuzed.everyweather.data.repository.settings.SystemSettingsRepositoryImpl
import com.desuzed.everyweather.data.repository.settings.WeatherSettingRepositoryImpl
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.SettingsType
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.domain.repository.settings.SettingsDatastoreApiProvider
import com.desuzed.everyweather.presentation.features.settings.SettingsAction
import com.desuzed.everyweather.presentation.features.settings.SettingsState
import com.desuzed.everyweather.presentation.features.settings.ui.SettingsScreen
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI + real repositories/DataStore; screen rebinding is not process-death coverage. */
@RunWith(AndroidJUnit4::class)
class SettingsPersistenceUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun changedSettings_surviveScreenRebinding_withIsolatedPersistentStorage() {
        // Instrumentation executes with the target app's UID; the test APK's
        // private cache belongs to a different UID and is not writable here.
        val testContext = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(testContext.cacheDir, "settings-ui-${UUID.randomUUID()}")
        val isolatedContext = object : ContextWrapper(testContext) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = directory.apply { mkdirs() }
        }
        val provider = SettingsDatastoreApiProvider(isolatedContext)
        val observed = AtomicReference<SettingsState?>()
        var visible by mutableStateOf(true)
        var generation by mutableStateOf(0)

        try {
            composeRule.setContent {
                if (visible) key(generation) {
                    // Recreate repositories and collectors when returning to the screen.
                    val system = remember { SystemSettingsRepositoryImpl(provider) }
                    val weather = remember { WeatherSettingRepositoryImpl(provider) }
                    val settingsFlow = remember {
                        combine(system.lang, system.darkMode, weather.distanceDimen,
                            weather.tempDimen, weather.pressureDimen) { lang, mode, wind, temp, pressure ->
                            SettingsState(selectedLang = lang, selectedMode = mode,
                                selectedDistanceDimen = wind, selectedTempDimen = temp,
                                selectedPressure = pressure)
                        }
                    }
                    val stored by settingsFlow.collectAsState(initial = SettingsState())
                    var dialog by remember { mutableStateOf<SettingsType?>(null) }
                    val scope = rememberCoroutineScope()
                    SideEffect { observed.set(stored.copy(showDialogType = dialog)) }
                    EveryweatherTheme(darkTheme = stored.selectedMode == DarkMode.ON) {
                        SettingsScreen(state = stored.copy(showDialogType = dialog), onAction = { action ->
                            when (action) {
                                is SettingsAction.ShowSettingDialog -> dialog = action.type
                                SettingsAction.DismissDialog -> dialog = null
                                else -> scope.launch {
                                    when (action) {
                                        is SettingsAction.ChangeLanguage -> system.setLanguage(action.lang)
                                        is SettingsAction.ChangeDarkMode -> system.setDarkMode(action.darkMode)
                                        is SettingsAction.ChangeDistanceDimension ->
                                            weather.setDistanceDimension(action.distanceDimen)
                                        is SettingsAction.ChangeTemperatureDimension ->
                                            weather.setTemperatureDimension(action.tempDimen)
                                        is SettingsAction.ChangePressureDimension ->
                                            weather.setPressureDimension(action.pressureDimen)
                                        else -> error("Unexpected settings action: $action")
                                    }
                                    dialog = null
                                }
                            }
                        })
                    }
                }
            }

            composeRule.waitUntil(5_000) { observed.get()?.selectedLang == Lang.RU }
            choose(R.string.language, R.string.english)
            composeRule.waitUntil(5_000) {
                observed.get()?.let { it.selectedLang == Lang.EN && it.showDialogType == null } == true
            }
            choose(R.string.dark_mode, R.string.on)
            composeRule.waitUntil(5_000) {
                observed.get()?.let { it.selectedMode == DarkMode.ON && it.showDialogType == null } == true
            }
            choose(R.string.temperature_dimension, R.string.fahrenheit)
            composeRule.waitUntil(5_000) {
                observed.get()?.let { it.selectedTempDimen == TempDimen.FAHRENHEIT && it.showDialogType == null } == true
            }
            choose(R.string.distance_dimension, R.string.mph)
            composeRule.waitUntil(5_000) {
                observed.get()?.let { it.selectedDistanceDimen == DistanceDimen.IMPERIAL && it.showDialogType == null } == true
            }
            choose(R.string.pressure, R.string.inch)
            composeRule.waitUntil(5_000) {
                observed.get()?.let { it.selectedPressure == PressureDimen.INCHES && it.showDialogType == null } == true
            }

            composeRule.runOnIdle { visible = false }
            composeRule.onNodeWithText(text(R.string.settings)).assertDoesNotExist()
            observed.set(null)
            composeRule.runOnIdle { generation++; visible = true }
            composeRule.waitUntil(5_000) {
                observed.get()?.let { it.selectedLang == Lang.EN && it.selectedMode == DarkMode.ON
                    && it.selectedTempDimen == TempDimen.FAHRENHEIT
                    && it.selectedDistanceDimen == DistanceDimen.IMPERIAL
                    && it.selectedPressure == PressureDimen.INCHES } == true
            }
            for (value in listOf(R.string.english, R.string.on, R.string.fahrenheit,
                R.string.mph, R.string.inch)) {
                composeRule.onNodeWithText(text(value)).performScrollTo().assertIsDisplayed()
            }

            runBlocking {
                assertEquals(Lang.EN, SystemSettingsRepositoryImpl(provider).lang.first())
                assertEquals(TempDimen.FAHRENHEIT, WeatherSettingRepositoryImpl(provider).tempDimen.first())
            }
            assertTrue("DataStore must write only to the isolated test directory",
                File(directory, "datastore/settings.preferences_pb").let { it.isFile && it.length() > 0 })
        } finally {
            composeRule.runOnIdle { visible = false }
            composeRule.waitForIdle()
            directory.deleteRecursively()
        }
    }

    private fun choose(category: Int, value: Int) {
        composeRule.onNodeWithText(text(category)).performScrollTo().performClick()
        composeRule.onNodeWithText(text(value)).performClick()
    }

    private fun text(id: Int) = composeRule.activity.getString(id)
}
