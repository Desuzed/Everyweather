package com.desuzed.everyweather.presentation.features.main_activity

import androidx.lifecycle.viewModelScope
import com.desuzed.everyweather.data.repository.local.SettingsDataStore
import com.desuzed.everyweather.data.repository.providers.UserLocationProvider
import com.desuzed.everyweather.data.repository.providers.action_result.ActionResult
import com.desuzed.everyweather.domain.model.UserLatLng
import com.desuzed.everyweather.domain.model.location.UserLocationResult
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.domain.model.settings.DarkTheme
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.Language
import com.desuzed.everyweather.domain.repository.local.SharedPrefsProvider
import com.desuzed.everyweather.presentation.base.BaseViewModel
import com.desuzed.everyweather.util.NetworkConnection
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainActivityViewModel(
    networkConnection: NetworkConnection,
    private val settingsDataStore: SettingsDataStore,
    private val sharedPrefsProvider: SharedPrefsProvider,
    private val userLocationProvider: UserLocationProvider,
) : BaseViewModel<MainActivityState, MainActivityAction>(MainActivityState()) {

    private val _isLookingForLocation = MutableSharedFlow<Boolean>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val isLookingForLocation: Flow<Boolean> = _isLookingForLocation.asSharedFlow()

    val hasInternet = networkConnection.hasInternetFlow()

    private val _userLatLng = MutableStateFlow<UserLatLng?>(null)
    val userLatLng: Flow<UserLatLng?> = _userLatLng.asStateFlow()

    private val _messageFlow = MutableSharedFlow<ActionResult>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messageFlow: Flow<ActionResult> = _messageFlow.asSharedFlow()

    init {
        collect(settingsDataStore.lang, ::collectLanguage)
        collect(settingsDataStore.darkMode, ::collectDarkTheme)
        collect(userLocationProvider.userLocationFlow, ::collectUserLocationResult)
    }

    fun onLanguage(appLanguage: String?) {
        viewModelScope.launch {
            val lang = when (appLanguage) {
                "ru" -> Lang.RU
                else -> Lang.EN
            }
            settingsDataStore.setLanguage(lang)
        }
    }

    fun findUserLocation() {
        toggleLookingForLocation(true)
        userLocationProvider.findUserLocation()
    }

    fun areLocationPermissionsGranted(): Boolean = userLocationProvider.arePermissionsGranted()

    fun isFirstRun() = sharedPrefsProvider.isFirstRunApp()

    private fun collectLanguage(lang: Language) {
        val lowercaseLang = lang.id.lowercase()
        setState { copy(lang = lowercaseLang) }
        setAction(MainActivityAction.ChangeLanguage(lowercaseLang))
    }

    private fun collectDarkTheme(theme: DarkTheme) {
        val mode = DarkMode.valueOf(theme.id.uppercase())
        setAction(MainActivityAction.ChangeDarkMode(mode))
    }

    private fun collectUserLocationResult(result: UserLocationResult?) {
        if (result?.userLatLng != null) {
            _userLatLng.value = result.userLatLng
        } else if (result?.actionResult != null) {
            postMessage(result.actionResult)
        }
        toggleLookingForLocation(false)

    }

    private fun postMessage(actionResult: ActionResult) {
        viewModelScope.launch {
            _messageFlow.emit(actionResult)
        }
    }

    private fun toggleLookingForLocation(state: Boolean) {
        viewModelScope.launch {
            _isLookingForLocation.emit(state)
        }
    }

}