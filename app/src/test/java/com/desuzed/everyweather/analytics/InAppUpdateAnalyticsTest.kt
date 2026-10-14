package com.desuzed.everyweather.analytics

import com.desuzed.everyweather.analytics.update.UpdateTelemetry
import com.desuzed.everyweather.data.repository.providers.app_update.AppUpdateProvider
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.presentation.features.in_app_update.InAppUpdateAction
import com.desuzed.everyweather.presentation.features.in_app_update.InAppUpdateViewModel
import com.desuzed.everyweather.testutil.MainDispatcherRule
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class InAppUpdateAnalyticsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `analytics preserves dialog actions and status`() {
        val telemetry = RecordingTelemetry()
        val analytics = InAppUpdateAnalytics(telemetry)

        analytics.onShown(InAppUpdateStatus.READY_TO_INSTALL)
        analytics.onAction(InAppUpdateAction.Dismiss)
        analytics.onAction(InAppUpdateAction.AgreedToUpdate)
        analytics.onAction(InAppUpdateAction.AgreedToInstallUpdate)

        assertEquals(
            listOf(
                "in_app_update_dialog_shown" to mapOf("dialog_status" to "ready_to_install"),
                "in_app_update_dialog_dismissed" to emptyMap(),
                "in_app_update_start" to emptyMap(),
                "in_app_update_install" to emptyMap(),
            ),
            telemetry.events,
        )
    }

    @Test
    fun `view model records shown only once while preserving actions`() {
        val telemetry = RecordingTelemetry()
        val viewModel = InAppUpdateViewModel(
            status = InAppUpdateStatus.READY_TO_LAUNCH_UPDATE,
            analytics = InAppUpdateAnalytics(telemetry),
            appUpdateProvider = mockk<AppUpdateProvider>(relaxed = true),
        )

        viewModel.onShown()
        viewModel.onShown()
        viewModel.onAction(InAppUpdateAction.Dismiss)
        viewModel.onAction(InAppUpdateAction.AgreedToUpdate)

        assertEquals(
            listOf(
                "in_app_update_dialog_shown" to mapOf("dialog_status" to "ready_to_launch_update"),
                "in_app_update_dialog_dismissed" to emptyMap(),
                "in_app_update_start" to emptyMap(),
            ),
            telemetry.events,
        )
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
