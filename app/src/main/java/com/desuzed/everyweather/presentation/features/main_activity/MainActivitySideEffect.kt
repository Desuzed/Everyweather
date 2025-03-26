package com.desuzed.everyweather.presentation.features.main_activity

import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.presentation.base.SideEffect

sealed interface MainActivitySideEffect : SideEffect {
    data class ChangeLanguage(val lang: String) : MainActivitySideEffect
    data class ChangeDarkMode(val mode: DarkMode) : MainActivitySideEffect
    data object UpdateAvailableDialog : MainActivitySideEffect
    data object UpdateReadyToInstallDialog : MainActivitySideEffect
}