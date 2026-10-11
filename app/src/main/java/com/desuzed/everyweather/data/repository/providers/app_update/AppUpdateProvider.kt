package com.desuzed.everyweather.data.repository.providers.app_update

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.desuzed.everyweather.analytics.update.NoOpUpdateTelemetry
import com.desuzed.everyweather.analytics.update.UpdateAnalytics
import com.desuzed.everyweather.analytics.update.UpdateCheckOutcome
import com.desuzed.everyweather.analytics.update.UpdateConsentOutcome
import com.desuzed.everyweather.analytics.update.UpdateStage
import com.desuzed.everyweather.analytics.update.UpdateTelemetry
import com.desuzed.everyweather.domain.model.app_update.AppUpdateState
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CancellationException

class AppUpdateProvider(
    context: Context,
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context),
    private val telemetry: UpdateTelemetry = NoOpUpdateTelemetry,
) {
    private val analytics = UpdateAnalytics(telemetry)
    private var appUpdateInfo: AppUpdateInfo? = null
    private var isListeningForProgress = false
    private var lastInstallStatus: Int? = null
    private var installing = false
    private val _appUpdateState = MutableStateFlow<AppUpdateState?>(null)
    val appUpdateState: Flow<AppUpdateState?> = _appUpdateState.asStateFlow()

    @SuppressLint("SwitchIntDef")
    private val updateListener: InstallStateUpdatedListener = InstallStateUpdatedListener { state ->
        val status = state.installStatus()
        val changed = lastInstallStatus != status
        lastInstallStatus = status
        when (status) {
            InstallStatus.DOWNLOADED -> {
                if (changed) analytics.downloaded()
                clearProgress()
                setReadyToInstall()
                unsubscribeOnAppUpdate()
            }
            InstallStatus.DOWNLOADING -> {
                if (changed) analytics.downloadStarted()
                setProgressState(state.bytesDownloaded(), state.totalBytesToDownload(), true)
            }
            InstallStatus.INSTALLING -> installing = true
            InstallStatus.CANCELED -> {
                if (changed) analytics.canceled(if (installing) UpdateStage.INSTALL else UpdateStage.DOWNLOAD)
                telemetry.clearInstallRequest()
                if (!installing) clearProgress()
                unsubscribeOnAppUpdate()
                installing = false
            }
            InstallStatus.FAILED -> {
                if (changed) analytics.installError(
                    if (installing) UpdateStage.INSTALL else UpdateStage.DOWNLOAD,
                    state.installErrorCode(),
                )
                telemetry.clearInstallRequest()
                if (!installing) clearProgress()
                unsubscribeOnAppUpdate()
                installing = false
            }
            InstallStatus.INSTALLED -> {
                if (changed) analytics.installStatusInstalled()
                // Keep the marker: the next launch separately observes the running version.
                clearProgress()
                unsubscribeOnAppUpdate()
                installing = false
            }
        }
    }

    fun startListeningForUpdates() {
        telemetry.observeAppLaunch()
        telemetry.setTargetVersion(null)
        analytics.checkStarted()
        try {
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { updateInfo ->
                    telemetry.setTargetVersion(updateInfo.availableVersionCode().toLong())
                    val outcome: UpdateCheckOutcome = when {
                        updateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                            lastInstallStatus = InstallStatus.DOWNLOADED
                            clearProgress()
                            setReadyToInstall()
                            unsubscribeOnAppUpdate()
                            UpdateCheckOutcome.DOWNLOADED
                        }
                        updateInfo.installStatus() == InstallStatus.DOWNLOADING -> {
                            appUpdateInfo = updateInfo
                            lastInstallStatus = InstallStatus.DOWNLOADING
                            analytics.downloadStarted(resumed = true)
                            setProgressState(0, 0, true)
                            subscribeOnAppUpdateProgress()
                            UpdateCheckOutcome.DOWNLOADING
                        }
                        updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                            if (updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                                appUpdateInfo = updateInfo
                                setUpdateAvailable()
                                UpdateCheckOutcome.AVAILABLE
                            } else {
                                appUpdateInfo = null
                                UpdateCheckOutcome.NOT_ALLOWED
                            }
                        }
                        else -> {
                            appUpdateInfo = null
                            UpdateCheckOutcome.UNAVAILABLE
                        }
                    }
                    analytics.checkResult(outcome)
                }
                .addOnFailureListener { error -> onCheckFailure(error) }
        } catch (error: Exception) {
            onCheckFailure(error)
        }
    }

    private fun onCheckFailure(error: Exception) {
        preserveCancellation(error)
        analytics.error(UpdateStage.CHECK, error)
        appUpdateInfo = null
        clearProgress()
        unsubscribeOnAppUpdate()
    }

    fun completeUpdate() {
        telemetry.installRequested()
        installing = true
        try {
            subscribeOnAppUpdateProgress()
            appUpdateManager.completeUpdate()
                .addOnSuccessListener { analytics.installAccepted() }
                .addOnFailureListener { error -> onInstallFailure(error) }
        } catch (error: Exception) {
            onInstallFailure(error)
        }
    }

    private fun onInstallFailure(error: Exception) {
        preserveCancellation(error)
        telemetry.clearInstallRequest()
        analytics.error(UpdateStage.INSTALL, error)
        installing = false
        // A failed completion leaves the downloaded APK ready for another attempt.
        unsubscribeOnAppUpdate()
    }

    fun startUpdate(activity: Activity) {
        try {
            val info = appUpdateInfo ?: run {
                analytics.missingUpdateInfo()
                unsubscribeOnAppUpdate()
                return
            }
            lastInstallStatus = null
            subscribeOnAppUpdateProgress()
            val started = appUpdateManager.startUpdateFlowForResult(
                info, AppUpdateType.FLEXIBLE, activity, UPDATE_REQUEST_CODE,
            )
            if (started) {
                analytics.flowLaunched()
            } else {
                analytics.flowNotStarted()
                clearProgress()
                unsubscribeOnAppUpdate()
            }
        } catch (error: Exception) {
            preserveCancellation(error)
            analytics.error(UpdateStage.LAUNCH, error)
            clearProgress()
            unsubscribeOnAppUpdate()
        }
    }

    fun onUpdateActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode != UPDATE_REQUEST_CODE) return
        val outcome = when (resultCode) {
            Activity.RESULT_OK -> UpdateConsentOutcome.ACCEPTED
            Activity.RESULT_CANCELED -> UpdateConsentOutcome.CANCELED
            else -> UpdateConsentOutcome.FAILED
        }
        analytics.consentResult(outcome, resultCode)
        if (outcome == UpdateConsentOutcome.FAILED) {
            analytics.consentFailed(resultCode)
        }
        if (outcome != UpdateConsentOutcome.ACCEPTED) {
            clearProgress()
            unsubscribeOnAppUpdate()
        }
    }

    private fun preserveCancellation(error: Exception) {
        if (error is CancellationException) throw error
    }

    private fun subscribeOnAppUpdateProgress() {
        if (isListeningForProgress) return
        appUpdateManager.registerListener(updateListener)
        isListeningForProgress = true
    }

    private fun unsubscribeOnAppUpdate() {
        if (!isListeningForProgress) return
        appUpdateManager.unregisterListener(updateListener)
        isListeningForProgress = false
    }

    private fun setReadyToInstall() { _appUpdateState.value = AppUpdateState.ReadyToInstall }
    private fun setUpdateAvailable() { _appUpdateState.value = AppUpdateState.UpdateAvailable }
    private fun setProgressState(progress: Long, totalBytes: Long, isLoading: Boolean) {
        _appUpdateState.value = AppUpdateState.Downloading(totalBytes, progress, isLoading)
    }
    private fun clearProgress() { setProgressState(0, 0, false) }

    companion object { const val UPDATE_REQUEST_CODE = 55 }
}
