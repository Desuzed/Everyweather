package com.desuzed.everyweather.data.repository.local

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.domain.model.settings.DarkTheme
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.Language
import com.desuzed.everyweather.domain.model.settings.Pressure
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.SettingUiItem
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.domain.model.settings.Temperature
import com.desuzed.everyweather.domain.model.settings.WindSpeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = SETTINGS_PREFS)

    suspend fun setLanguage(lang: Lang) = edit { preferences ->
        preferences[KEY_LANGUAGE] = lang.lang
    }

    val lang: Flow<Language> = getFlowOf(KEY_LANGUAGE, Lang.RU.lang).map { langId ->
        val langValueId = when (langId) {
            Lang.RU.lang -> R.string.russian
            else -> R.string.english
        }
        Language(
            id = langId,
            categoryStringId = R.string.language,
            valueStringId = langValueId,
        )
    }

    suspend fun setDistanceDimension(dimension: DistanceDimen) = edit { preferences ->
        preferences[KEY_DISTANCE_DIMENSION] = dimension.dimensionName
    }

    val distanceDimen: Flow<WindSpeed> =
        getFlowOf(
            KEY_DISTANCE_DIMENSION,
            DistanceDimen.METRIC_KMH.dimensionName
        ).map { distanceDimenId ->
            val distValueId = when (distanceDimenId) {
                DistanceDimen.IMPERIAL.dimensionName -> R.string.mph
                DistanceDimen.METRIC_MS.dimensionName -> R.string.ms
                else -> R.string.kmh
            }
            WindSpeed(
                id = distanceDimenId,
                categoryStringId = R.string.distance_dimension,
                valueStringId = distValueId,
            )
        }

    suspend fun setTemperatureDimension(dimension: TempDimen) = edit { preferences ->
        preferences[KEY_TEMPERATURE_DIMENSION] = dimension.dimensionName
    }

    val tempDimen: Flow<Temperature> =
        getFlowOf(KEY_TEMPERATURE_DIMENSION, TempDimen.CELCIUS.dimensionName).map { tempDimenId ->
            val tempValueId = when (tempDimenId) {
                TempDimen.FAHRENHEIT.dimensionName -> R.string.fahrenheit
                else -> R.string.celcius
            }
            Temperature(
                id = tempDimenId,
                categoryStringId = R.string.temperature_dimension,
                valueStringId = tempValueId,
            )
        }

    suspend fun setPressureDimension(dimension: PressureDimen) = edit { preferences ->
        preferences[KEY_PRESSURE_DIMENSION] = dimension.dimensionName
    }

    val pressureDimen: Flow<Pressure> = getFlowOf(
        KEY_PRESSURE_DIMENSION,
        PressureDimen.MILLIMETERS.dimensionName
    ).map { pressureDimenId ->
        val pressureValueId = when (pressureDimenId) {
            PressureDimen.MILLIBAR.dimensionName -> R.string.mb
            PressureDimen.INCHES.dimensionName -> R.string.inch
            else -> R.string.mmhg

        }
        Pressure(
            id = pressureDimenId,
            categoryStringId = R.string.pressure,
            valueStringId = pressureValueId,
        )
    }

    fun getLanguageItemsList(): List<SettingUiItem<Lang>> {
        return Lang.values().toList().map {
            when (it) {
                Lang.RU -> SettingUiItem(it, R.string.russian)
                Lang.EN -> SettingUiItem(it, R.string.english)
            }
        }
    }

    fun getDistanceItemsList(): List<SettingUiItem<DistanceDimen>> {
        return DistanceDimen.values().toList().map {
            when (it) {
                DistanceDimen.METRIC_KMH -> SettingUiItem(it, R.string.kmh)
                DistanceDimen.METRIC_MS -> SettingUiItem(it, R.string.ms)
                DistanceDimen.IMPERIAL -> SettingUiItem(it, R.string.mph)
            }
        }
    }

    fun getDarkModeItemsList(): List<SettingUiItem<DarkMode>> {
        return DarkMode.values().toList().map {
            when (it) {
                DarkMode.ON -> SettingUiItem(it, R.string.on)
                DarkMode.OFF -> SettingUiItem(it, R.string.off)
                DarkMode.SYSTEM -> SettingUiItem(it, R.string.system)
            }
        }
    }

    fun getTemperatureItemsList(): List<SettingUiItem<TempDimen>> {
        return TempDimen.values().toList().map {
            when (it) {
                TempDimen.FAHRENHEIT -> SettingUiItem(it, R.string.fahrenheit)
                TempDimen.CELCIUS -> SettingUiItem(it, R.string.celcius)
            }
        }
    }

    fun getPressureItemsList(): List<SettingUiItem<PressureDimen>> {
        return PressureDimen.values().toList().map {
            when (it) {
                PressureDimen.MILLIBAR -> SettingUiItem(it, R.string.mb)
                PressureDimen.MILLIMETERS -> SettingUiItem(it, R.string.mmhg)
                PressureDimen.INCHES -> SettingUiItem(it, R.string.inch)
            }
        }
    }

    suspend fun setDarkMode(mode: DarkMode) = edit { preferences ->
        preferences[KEY_DARK_MODE] = mode.mode
    }

    val darkMode: Flow<DarkTheme> =
        getFlowOf(KEY_DARK_MODE, DarkMode.SYSTEM.mode).map { darkModeId ->
            val darkThemeValueId = when (darkModeId) {
                DarkMode.ON.mode -> R.string.on
                DarkMode.OFF.mode -> R.string.off
                else -> R.string.system
            }
            DarkTheme(
                id = darkModeId,
                categoryStringId = R.string.dark_mode,
                valueStringId = darkThemeValueId,

                )
        }

    private suspend inline fun edit(crossinline transform: suspend (MutablePreferences) -> Unit) {
        context.dataStore.edit { preferences ->
            transform(preferences)
        }
    }

    private fun <V> getFlowOf(key: Preferences.Key<V>, defaultValue: V): Flow<V> =
        context.dataStore.data.map { it[key] ?: defaultValue }

    companion object {
        private const val SETTINGS_PREFS = "settings"
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_DARK_MODE = stringPreferencesKey("dark_mode")
        private val KEY_DISTANCE_DIMENSION = stringPreferencesKey("distance_dimension")
        private val KEY_TEMPERATURE_DIMENSION = stringPreferencesKey("temperature_dimension")
        private val KEY_PRESSURE_DIMENSION = stringPreferencesKey("pressure_dimension")
    }

}