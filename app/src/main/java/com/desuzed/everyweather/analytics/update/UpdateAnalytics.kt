package com.desuzed.everyweather.analytics.update

import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.ErrorNames
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.Events
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.Params
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.Values
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus

/** Reusable update logging API; callers describe facts without constructing Firebase payloads. */
class UpdateAnalytics(private val telemetry: UpdateTelemetry) {
    fun checkStarted() = checkResult(UpdateCheckOutcome.STARTED)

    fun checkResult(outcome: UpdateCheckOutcome) {
        record(Events.CHECK, mapOf(Params.OUTCOME to outcome.parameterValue))
    }

    fun dialogShown(status: InAppUpdateStatus?) {
        val value = when (status) {
            InAppUpdateStatus.READY_TO_LAUNCH_UPDATE -> Values.READY_TO_LAUNCH_UPDATE
            InAppUpdateStatus.READY_TO_INSTALL -> Values.READY_TO_INSTALL
            null -> Values.UNKNOWN
        }
        record(Events.DIALOG_SHOWN, mapOf(Params.DIALOG_STATUS to value))
    }

    fun dialogDismissed() = record(Events.DIALOG_DISMISSED)
    fun updateClicked() = record(Events.UPDATE_CLICKED)
    fun installClicked() = record(Events.INSTALL_CLICKED)
    fun settingsUpdateOpened() = record(Events.SETTINGS_UPDATE_OPENED)
    fun settingsInstallOpened() = record(Events.SETTINGS_INSTALL_OPENED)
    fun flowLaunched() = record(Events.FLOW_LAUNCHED)

    fun consentResult(outcome: UpdateConsentOutcome, resultCode: Int) {
        record(Events.CONSENT, mapOf(
            Params.OUTCOME to outcome.parameterValue,
            Params.RESULT_CODE to resultCode.toString(),
        ))
    }

    fun downloadStarted(resumed: Boolean = false) {
        record(Events.DOWNLOAD_STARTED, if (resumed) mapOf(Params.RESUMED to Values.TRUE) else emptyMap())
    }

    fun downloaded() = record(Events.DOWNLOADED)

    fun canceled(stage: UpdateStage) {
        val event = when (stage) {
            UpdateStage.DOWNLOAD -> Events.DOWNLOAD_CANCELED
            UpdateStage.INSTALL -> Events.INSTALL_CANCELED
            else -> error("Cancellation is only emitted for download/install stages")
        }
        record(event)
    }

    fun installAccepted() = record(Events.INSTALL_ACCEPTED)
    fun installStatusInstalled() = record(Events.INSTALL_STATUS_INSTALLED)

    fun error(stage: UpdateStage, error: Exception) {
        recordError(UpdateAnalyticsError.fromException(stage, error))
    }

    fun installError(stage: UpdateStage, errorCode: Int) {
        recordError(UpdateAnalyticsError.fromInstallCode(stage, errorCode))
    }

    fun missingUpdateInfo() {
        recordError(UpdateAnalyticsError(UpdateStage.LAUNCH, ErrorNames.MISSING_UPDATE_INFO))
    }

    fun flowNotStarted() {
        recordError(UpdateAnalyticsError(UpdateStage.LAUNCH, ErrorNames.FLOW_NOT_STARTED))
    }

    fun consentFailed(resultCode: Int) {
        recordError(UpdateAnalyticsError(UpdateStage.CONSENT, ErrorNames.FLOW_FAILED, resultCode = resultCode))
    }

    private fun recordError(error: UpdateAnalyticsError) = record(Events.ERROR, error.toParameters())

    private fun record(event: String, parameters: Map<String, String> = emptyMap()) {
        telemetry.record(event, parameters)
    }
}
