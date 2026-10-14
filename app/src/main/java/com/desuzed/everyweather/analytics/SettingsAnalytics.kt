package com.desuzed.everyweather.analytics

import android.content.Context
import com.desuzed.everyweather.analytics.update.UpdateAnalytics
import com.desuzed.everyweather.analytics.update.UpdateTelemetry
import com.desuzed.everyweather.presentation.features.settings.SettingsAction

class SettingsAnalytics(context: Context, private val updateTelemetry: UpdateTelemetry) : Analytics(context = context) {
    private val updateAnalytics = UpdateAnalytics(updateTelemetry)

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.ChangeDarkMode -> logEvent(CHANGE_DARK_MODE)
            is SettingsAction.ChangeDistanceDimension -> logEvent(CHANGE_DISTANCE)
            is SettingsAction.ChangeLanguage -> logEvent(CHANGE_LANG)
            is SettingsAction.ChangePressureDimension -> logEvent(CHANGE_PRESSURE)
            is SettingsAction.ChangeTemperatureDimension -> logEvent(CHANGE_TEMPERATURE)
            is SettingsAction.ReadyToLaunchUpdate -> updateAnalytics.settingsUpdateOpened()
            is SettingsAction.ReadyToInstall -> updateAnalytics.settingsInstallOpened()
            else -> {}
        }
    }

    companion object {
        private const val CHANGE_LANG = "settings_change_lang"
        private const val CHANGE_DARK_MODE = "settings_change_dark_mode"
        private const val CHANGE_TEMPERATURE = "settings_change_temp"
        private const val CHANGE_DISTANCE = "settings_change_distance"
        private const val CHANGE_PRESSURE = "settings_change_pressure"
    }
}
