package com.desuzed.everyweather.presentation.features.main_activity

import androidx.lifecycle.viewModelScope
import com.desuzed.everyweather.data.repository.providers.app_update.AppUpdateProvider
import com.desuzed.everyweather.domain.interactor.SystemInteractor
import com.desuzed.everyweather.domain.interactor.SystemSettingsInteractor
import com.desuzed.everyweather.domain.model.app_update.AppUpdateState
import com.desuzed.everyweather.domain.model.location.UserLocationResult
import com.desuzed.everyweather.domain.model.result.ActionResult
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.presentation.base.Action
import com.desuzed.everyweather.presentation.base.BaseViewModel
import com.desuzed.everyweather.util.Constants.LANG_RU_LOWERCASE
import com.desuzed.everyweather.util.Constants.ZERO_LONG
import com.desuzed.everyweather.util.Timer
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MainActivityViewModel(
    private val systemInteractor: SystemInteractor,
    private val systemSettingsInteractor: SystemSettingsInteractor,
    private val weatherDataRepository: WeatherDataRepository,
    private val appUpdateProvider: AppUpdateProvider,
) : BaseViewModel<MainActivityState, MainActivitySideEffect, Action>(MainActivityState()) {

    private val _messageFlow = MutableSharedFlow<ActionResult>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messageFlow: Flow<ActionResult> = _messageFlow.asSharedFlow()

    private var timerJob: Job? = null

    init {
        collect(systemSettingsInteractor.lang, ::collectLanguage)
        collect(systemSettingsInteractor.darkMode, ::collectDarkTheme)
        collect(systemInteractor.userLocationFlow(), ::collectUserLocationResult)
        collect(systemInteractor.hasInternetFlow(), ::onHasInternet)
        collect(appUpdateProvider.appUpdateState, ::onAppUpdateState)
    }

    fun onLanguage(appLanguage: String?) {
        viewModelScope.launch {
            val lang = when (appLanguage) {
                LANG_RU_LOWERCASE -> Lang.RU
                else -> Lang.EN
            }
            systemSettingsInteractor.setLanguage(lang)
        }
    }

    fun findUserLocation() {
        val shouldToggle = systemInteractor.findUserLocation()
        toggleLookingForLocation(shouldToggle)
        if (shouldToggle) {
            timerJob = viewModelScope.launch {
                Timer.timerFlow(TIMER_DURATION).onCompletion {
                    if (it !is CancellationException) {
                        systemInteractor.cancelLookingForLocation()
                    }
                }.collect()
            }
        }
    }

    fun areLocationPermissionsGranted(): Boolean = systemInteractor.arePermissionsGranted()

    fun isFirstRun() = systemInteractor.isFirstRunApp()

    fun startListeningForUpdates() {
        appUpdateProvider.startListeningForUpdates()
    }

    private fun collectLanguage(lang: Lang) {
        val lowercaseLang = lang.lang.lowercase()
        setState { copy(lang = lowercaseLang) }
        setSideEffect(MainActivitySideEffect.ChangeLanguage(lowercaseLang))
    }

    private fun collectDarkTheme(darkMode: DarkMode) {
        val mode = DarkMode.valueOf(darkMode.mode.uppercase())
        setSideEffect(MainActivitySideEffect.ChangeDarkMode(mode))
    }

    private fun collectUserLocationResult(result: UserLocationResult?) {
        launch {
            if (result?.userLatLng != null) {
                weatherDataRepository.saveQuery(
                    query = result.userLatLng.toString(),
                    shouldTriggerWeatherRequest = true,
                    userLatLng = result.userLatLng,
                )
            } else if (result?.actionResult != null) {
                postMessage(result.actionResult)
            }
            cancelTimerJob()
            toggleLookingForLocation(false)
        }

    }

    private fun postMessage(actionResult: ActionResult) {
        viewModelScope.launch {
            _messageFlow.emit(actionResult)
        }
    }

    private fun cancelTimerJob() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun toggleLookingForLocation(isLookingForLocation: Boolean) {
        setState { copy(isLookingForLocation = isLookingForLocation) }
    }

    private fun onHasInternet(hasInternet: Boolean) {
        setState { copy(isInternetUnavailable = !hasInternet) }
    }

    private fun onAppUpdateState(appUpdateState: AppUpdateState?) {
        when (appUpdateState) {
            is AppUpdateState.Downloading -> onDownloadingUpdateProgress(appUpdateState)
            AppUpdateState.ReadyToInstall -> updateReadyToComplete()
            AppUpdateState.UpdateAvailable -> showUpdateAvailableDialog()
            null -> {}
        }
    }

    private fun onDownloadingUpdateProgress(state: AppUpdateState.Downloading) {
        setState {
            copy(
                totalBytes = state.totalBytes,
                bytesDownloaded = state.bytesDownloaded,
                isUpdateLoading = state.isUpdateLoading,
            )
        }
    }

    private fun updateReadyToComplete() {
        showUpdateReadyToInstallDialog()
        setState {
            copy(
                totalBytes = ZERO_LONG,
                bytesDownloaded = ZERO_LONG,
                isUpdateLoading = false,
            )
        }
    }

    private fun showUpdateAvailableDialog() {
        setSideEffect(MainActivitySideEffect.UpdateAvailableDialog)
    }

    private fun showUpdateReadyToInstallDialog() {
        setSideEffect(MainActivitySideEffect.UpdateReadyToInstallDialog)
    }


    private companion object {
        private const val TIMER_DURATION = 20
    }

}