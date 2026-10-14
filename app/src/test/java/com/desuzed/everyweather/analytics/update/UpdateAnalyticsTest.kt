package com.desuzed.everyweather.analytics.update

import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UpdateAnalyticsTest {

    @Test
    fun `facade preserves update event names and parameters`() {
        val telemetry = RecordingTelemetry()
        val analytics = UpdateAnalytics(telemetry)

        analytics.checkStarted()
        UpdateCheckOutcome.values().forEach(analytics::checkResult)
        analytics.flowLaunched()
        analytics.consentResult(UpdateConsentOutcome.ACCEPTED, 0)
        analytics.consentResult(UpdateConsentOutcome.CANCELED, 1)
        analytics.consentResult(UpdateConsentOutcome.FAILED, 2)
        analytics.downloadStarted()
        analytics.downloadStarted(resumed = true)
        analytics.downloaded()
        analytics.canceled(UpdateStage.DOWNLOAD)
        analytics.canceled(UpdateStage.INSTALL)
        analytics.installAccepted()
        analytics.installStatusInstalled()
        analytics.error(UpdateStage.CHECK, IllegalStateException("private sdk detail"))
        analytics.installError(UpdateStage.DOWNLOAD, -7)
        analytics.installError(UpdateStage.INSTALL, -8)
        analytics.missingUpdateInfo()
        analytics.flowNotStarted()
        analytics.consentFailed(3)
        analytics.dialogShown(InAppUpdateStatus.READY_TO_INSTALL)
        analytics.dialogShown(null)
        analytics.dialogDismissed()
        analytics.updateClicked()
        analytics.installClicked()
        analytics.settingsUpdateOpened()
        analytics.settingsInstallOpened()

        assertEquals(
            listOf(
                "in_app_update_check" to mapOf("outcome" to "started"),
                "in_app_update_check" to mapOf("outcome" to "started"),
                "in_app_update_check" to mapOf("outcome" to "available"),
                "in_app_update_check" to mapOf("outcome" to "unavailable"),
                "in_app_update_check" to mapOf("outcome" to "not_allowed"),
                "in_app_update_check" to mapOf("outcome" to "downloading"),
                "in_app_update_check" to mapOf("outcome" to "downloaded"),
                "in_app_update_flow_launched" to emptyMap(),
                "in_app_update_consent" to mapOf("outcome" to "accepted", "result_code" to "0"),
                "in_app_update_consent" to mapOf("outcome" to "canceled", "result_code" to "1"),
                "in_app_update_consent" to mapOf("outcome" to "failed", "result_code" to "2"),
                "in_app_update_download_started" to emptyMap(),
                "in_app_update_download_started" to mapOf("resumed" to "true"),
                "in_app_update_downloaded" to emptyMap(),
                "in_app_update_download_canceled" to emptyMap(),
                "in_app_update_install_canceled" to emptyMap(),
                "in_app_update_install_accepted" to emptyMap(),
                "in_app_update_install_status_installed" to emptyMap(),
                "in_app_update_error" to mapOf(
                    "stage" to "check",
                    "exception_type" to "IllegalStateException",
                    "error_name" to "exception",
                ),
                "in_app_update_error" to mapOf(
                    "stage" to "download",
                    "error_code" to "-7",
                    "error_name" to "download_not_present",
                ),
                "in_app_update_error" to mapOf(
                    "stage" to "install",
                    "error_code" to "-8",
                    "error_name" to "install_in_progress",
                ),
                "in_app_update_error" to mapOf("stage" to "launch", "error_name" to "missing_update_info"),
                "in_app_update_error" to mapOf("stage" to "launch", "error_name" to "flow_not_started"),
                "in_app_update_error" to mapOf(
                    "stage" to "consent",
                    "error_name" to "flow_failed",
                    "result_code" to "3",
                ),
                "in_app_update_dialog_shown" to mapOf("dialog_status" to "ready_to_install"),
                "in_app_update_dialog_shown" to mapOf("dialog_status" to "unknown"),
                "in_app_update_dialog_dismissed" to emptyMap(),
                "in_app_update_start" to emptyMap(),
                "in_app_update_install" to emptyMap(),
                "settings_launch_update" to emptyMap(),
                "settings_install_update" to emptyMap(),
            ),
            telemetry.events,
        )
        assertFalse(telemetry.events.any { values -> values.second.values.any { it.contains("private sdk detail") } })
    }

    private class RecordingTelemetry : UpdateTelemetry {
        val events = mutableListOf<Pair<String, Map<String, String>>>()

        override fun record(event: String, parameters: Map<String, String>) {
            events += event to parameters
        }

        override fun observeAppLaunch() = Unit
        override fun setTargetVersion(version: Long?) = Unit
        override fun installRequested() = Unit
        override fun clearInstallRequest() = Unit
    }
}
