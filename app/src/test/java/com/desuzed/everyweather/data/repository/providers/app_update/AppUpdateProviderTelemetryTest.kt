package com.desuzed.everyweather.data.repository.providers.app_update

import android.app.Activity
import android.content.Context
import com.desuzed.everyweather.analytics.update.UpdateTelemetry
import com.desuzed.everyweather.domain.model.app_update.AppUpdateState
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import java.util.concurrent.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateProviderTelemetryTest {

    @Test
    fun `availability outcomes record check and target version`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val success = slot<OnSuccessListener<AppUpdateInfo>>()
        every { manager.appUpdateInfo } returns taskCapturingSuccess(success)
        val provider = provider(manager, telemetry)

        provider.startListeningForUpdates()
        success.captured.onSuccess(updateInfo(UpdateAvailability.UPDATE_AVAILABLE, allowed = true))

        assertTrue(telemetry.calls.contains("observeAppLaunch"))
        assertTrue(telemetry.calls.contains("setTargetVersion:20"))
        assertEquals(
            "available",
            telemetry.events.single { it.first == "in_app_update_check" && it.second["outcome"] == "available" }
                .second["outcome"],
        )
        assertEquals(20L, telemetry.recordedTargetVersion)
    }

    @Test
    fun `unavailable and disallowed updates record distinct outcomes`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val first = slot<OnSuccessListener<AppUpdateInfo>>()
        val second = slot<OnSuccessListener<AppUpdateInfo>>()
        every { manager.appUpdateInfo } returnsMany listOf(
            taskCapturingSuccess(first),
            taskCapturingSuccess(second),
        )
        val provider = provider(manager, telemetry)

        provider.startListeningForUpdates()
        first.captured.onSuccess(updateInfo(UpdateAvailability.UPDATE_NOT_AVAILABLE, allowed = true))
        provider.startListeningForUpdates()
        second.captured.onSuccess(updateInfo(UpdateAvailability.UPDATE_AVAILABLE, allowed = false))

        assertEquals(
            listOf("started", "unavailable", "started", "not_allowed"),
            telemetry.events.filter { it.first == "in_app_update_check" }.map { it.second["outcome"] },
        )
    }

    @Test
    fun `downloading and downloaded checks are recorded`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val first = slot<OnSuccessListener<AppUpdateInfo>>()
        val second = slot<OnSuccessListener<AppUpdateInfo>>()
        every { manager.appUpdateInfo } returnsMany listOf(
            taskCapturingSuccess(first),
            taskCapturingSuccess(second),
        )
        every { manager.registerListener(any()) } just runs
        every { manager.unregisterListener(any()) } just runs
        val provider = provider(manager, telemetry)

        provider.startListeningForUpdates()
        first.captured.onSuccess(updateInfo(UpdateAvailability.UPDATE_AVAILABLE, InstallStatus.DOWNLOADING))
        provider.startListeningForUpdates()
        second.captured.onSuccess(updateInfo(UpdateAvailability.UPDATE_AVAILABLE, InstallStatus.DOWNLOADED))

        assertEquals(
            listOf("started", "downloading", "started", "downloaded"),
            telemetry.events.filter { it.first == "in_app_update_check" }.map { it.second["outcome"] },
        )
    }

    @Test
    fun `app not owned failure records stable error without exception message`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val failure = slot<OnFailureListener>()
        every { manager.appUpdateInfo } returns taskCapturingFailure(failure)
        val provider = provider(manager, telemetry)

        provider.startListeningForUpdates()
        failure.captured.onFailure(InstallException(-10))

        val error = telemetry.events.single { it.first == "in_app_update_error" }.second
        assertEquals("check", error["stage"])
        assertEquals("-10", error["error_code"])
        assertEquals("app_not_owned", error["error_name"])
        assertFalse(error.containsKey("message"))
    }

    @Test
    fun `successful and failed launch record flow telemetry`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val success = slot<OnSuccessListener<AppUpdateInfo>>()
        every { manager.appUpdateInfo } returns taskCapturingSuccess(success)
        every { manager.registerListener(any()) } just runs
        every { manager.unregisterListener(any()) } just runs
        every {
            manager.startUpdateFlowForResult(any(), AppUpdateType.FLEXIBLE, any<Activity>(), AppUpdateProvider.UPDATE_REQUEST_CODE)
        } returnsMany listOf(true, false)
        val provider = provider(manager, telemetry)
        provider.startListeningForUpdates()
        success.captured.onSuccess(updateInfo())

        provider.startUpdate(mockk())
        provider.startUpdate(mockk())

        assertTrue(telemetry.events.any { it.first == "in_app_update_flow_launched" })
        val error = telemetry.events.single { it.first == "in_app_update_error" }.second
        assertEquals("launch", error["stage"])
        assertEquals("flow_not_started", error["error_name"])
    }

    @Test
    fun `missing update info records flow error`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        every { manager.registerListener(any()) } just runs
        every { manager.unregisterListener(any()) } just runs
        val provider = provider(manager, telemetry)

        provider.startUpdate(mockk())

        val error = telemetry.events.single { it.first == "in_app_update_error" }.second
        assertEquals("launch", error["stage"])
        assertEquals("missing_update_info", error["error_name"])
    }

    @Test
    fun `activity results record consent and failed flow`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val success = slot<OnSuccessListener<AppUpdateInfo>>()
        val listener = slot<InstallStateUpdatedListener>()
        every { manager.appUpdateInfo } returns taskCapturingSuccess(success)
        every { manager.registerListener(capture(listener)) } just runs
        every { manager.unregisterListener(any()) } just runs
        every {
            manager.startUpdateFlowForResult(any(), AppUpdateType.FLEXIBLE, any<Activity>(), AppUpdateProvider.UPDATE_REQUEST_CODE)
        } returns true
        val provider = provider(manager, telemetry)

        provider.startListeningForUpdates()
        success.captured.onSuccess(updateInfo())
        provider.startUpdate(mockk())
        provider.onUpdateActivityResult(AppUpdateProvider.UPDATE_REQUEST_CODE, Activity.RESULT_OK)
        provider.onUpdateActivityResult(AppUpdateProvider.UPDATE_REQUEST_CODE, Activity.RESULT_CANCELED)
        provider.onUpdateActivityResult(AppUpdateProvider.UPDATE_REQUEST_CODE, 1)
        provider.onUpdateActivityResult(AppUpdateProvider.UPDATE_REQUEST_CODE + 1, Activity.RESULT_OK)

        assertEquals(
            listOf("accepted", "canceled", "failed"),
            telemetry.events.filter { it.first == "in_app_update_consent" }.map { it.second["outcome"] },
        )
        val error = telemetry.events.single { it.first == "in_app_update_error" }.second
        assertEquals("consent", error["stage"])
        assertEquals("flow_failed", error["error_name"])
        assertEquals(
            AppUpdateState.Downloading(totalBytes = 0, bytesDownloaded = 0, isUpdateLoading = false),
            runBlocking { provider.appUpdateState.first() },
        )
        verify(exactly = 1) { manager.unregisterListener(any()) }
    }

    @Test
    fun `download telemetry is emitted once per attempt and cancellation unregisters`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val success = slot<OnSuccessListener<AppUpdateInfo>>()
        val listener = slot<InstallStateUpdatedListener>()
        every { manager.appUpdateInfo } returns taskCapturingSuccess(success)
        every { manager.registerListener(capture(listener)) } just runs
        every { manager.unregisterListener(any()) } just runs
        every {
            manager.startUpdateFlowForResult(any(), AppUpdateType.FLEXIBLE, any<Activity>(), AppUpdateProvider.UPDATE_REQUEST_CODE)
        } returns true
        val provider = provider(manager, telemetry)
        provider.startListeningForUpdates()
        success.captured.onSuccess(updateInfo())
        provider.startUpdate(mockk())
        listener.captured.onStateUpdate(installState(InstallStatus.DOWNLOADING))
        listener.captured.onStateUpdate(installState(InstallStatus.DOWNLOADING))
        listener.captured.onStateUpdate(installState(InstallStatus.CANCELED))

        assertEquals(1, telemetry.events.count { it.first == "in_app_update_download_started" })
        verify(exactly = 1) { manager.unregisterListener(any()) }
    }

    @Test
    fun `download failure records install error code`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val success = slot<OnSuccessListener<AppUpdateInfo>>()
        val listener = slot<InstallStateUpdatedListener>()
        every { manager.appUpdateInfo } returns taskCapturingSuccess(success)
        every { manager.registerListener(capture(listener)) } just runs
        every { manager.unregisterListener(any()) } just runs
        every {
            manager.startUpdateFlowForResult(any(), AppUpdateType.FLEXIBLE, any<Activity>(), AppUpdateProvider.UPDATE_REQUEST_CODE)
        } returns true
        val provider = provider(manager, telemetry)
        provider.startListeningForUpdates()
        success.captured.onSuccess(updateInfo())
        provider.startUpdate(mockk())
        listener.captured.onStateUpdate(installState(InstallStatus.FAILED, errorCode = -7))

        val error = telemetry.events.single { it.first == "in_app_update_error" }.second
        assertEquals("download", error["stage"])
        assertEquals("-7", error["error_code"])
    }

    @Test
    fun `downloaded and installed statuses record terminal telemetry`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val listener = slot<InstallStateUpdatedListener>()
        val success = slot<OnSuccessListener<Void>>()
        val task = taskCapturingVoidSuccess(success)
        every { manager.completeUpdate() } returns task
        every { manager.registerListener(capture(listener)) } just runs
        every { manager.unregisterListener(any()) } just runs
        val provider = provider(manager, telemetry)
        provider.completeUpdate()

        listener.captured.onStateUpdate(installState(InstallStatus.DOWNLOADED))
        listener.captured.onStateUpdate(installState(InstallStatus.INSTALLED))

        assertTrue(telemetry.events.any { it.first == "in_app_update_downloaded" })
        assertTrue(telemetry.events.any { it.first == "in_app_update_install_status_installed" })
    }

    @Test
    fun `failure after installing records install stage`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val infoSuccess = slot<OnSuccessListener<AppUpdateInfo>>()
        val listener = slot<InstallStateUpdatedListener>()
        val failure = slot<OnFailureListener>()
        every { manager.appUpdateInfo } returns taskCapturingSuccess(infoSuccess)
        every { manager.completeUpdate() } returns taskCapturingVoidFailure(failure)
        every { manager.registerListener(capture(listener)) } just runs
        every { manager.unregisterListener(any()) } just runs
        val provider = provider(manager, telemetry)
        provider.startListeningForUpdates()
        infoSuccess.captured.onSuccess(updateInfo(status = InstallStatus.DOWNLOADED))
        assertEquals(AppUpdateState.ReadyToInstall, runBlocking { provider.appUpdateState.first() })
        provider.completeUpdate()

        listener.captured.onStateUpdate(installState(InstallStatus.INSTALLING))
        listener.captured.onStateUpdate(installState(InstallStatus.FAILED, errorCode = -9))

        assertEquals(AppUpdateState.ReadyToInstall, runBlocking { provider.appUpdateState.first() })
        val error = telemetry.events.single { it.first == "in_app_update_error" }.second
        assertEquals("install", error["stage"])
        assertEquals("-9", error["error_code"])
    }

    @Test
    fun `canceled install records install telemetry and preserves ready state`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val infoSuccess = slot<OnSuccessListener<AppUpdateInfo>>()
        val listener = slot<InstallStateUpdatedListener>()
        val success = slot<OnSuccessListener<Void>>()
        every { manager.appUpdateInfo } returns taskCapturingSuccess(infoSuccess)
        every { manager.completeUpdate() } returns taskCapturingVoidSuccess(success)
        every { manager.registerListener(capture(listener)) } just runs
        every { manager.unregisterListener(any()) } just runs
        val provider = provider(manager, telemetry)
        provider.startListeningForUpdates()
        infoSuccess.captured.onSuccess(updateInfo(status = InstallStatus.DOWNLOADED))
        assertEquals(AppUpdateState.ReadyToInstall, runBlocking { provider.appUpdateState.first() })
        provider.completeUpdate()

        listener.captured.onStateUpdate(installState(InstallStatus.INSTALLING))
        listener.captured.onStateUpdate(installState(InstallStatus.CANCELED))

        assertTrue(telemetry.events.any { it.first == "in_app_update_install_canceled" })
        assertEquals(AppUpdateState.ReadyToInstall, runBlocking { provider.appUpdateState.first() })
    }

    @Test
    fun `complete update marks request before sdk and accepts success`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val success = slot<OnSuccessListener<Void>>()
        every { manager.completeUpdate() } answers {
            telemetry.trace += "sdk"
            taskCapturingVoidSuccess(success)
        }
        every { manager.registerListener(any()) } just runs
        every { manager.unregisterListener(any()) } just runs
        val provider = provider(manager, telemetry)

        provider.completeUpdate()
        success.captured.onSuccess(null)

        assertEquals("installRequested", telemetry.calls.first())
        assertEquals(listOf("installRequested", "sdk"), telemetry.trace.take(2))
        assertTrue(telemetry.events.any { it.first == "in_app_update_install_accepted" })
        assertFalse(telemetry.events.any { it.first == "in_app_update_version_observed" })
    }

    @Test
    fun `failed complete update clears request and records install error`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val failure = slot<OnFailureListener>()
        every { manager.completeUpdate() } returns taskCapturingVoidFailure(failure)
        every { manager.registerListener(any()) } just runs
        every { manager.unregisterListener(any()) } just runs
        val provider = provider(manager, telemetry)

        provider.completeUpdate()
        failure.captured.onFailure(IllegalStateException("raw sdk message"))

        assertTrue(telemetry.calls.contains("clearInstallRequest"))
        val error = telemetry.events.single { it.first == "in_app_update_error" }.second
        assertEquals("install", error["stage"])
        assertFalse(error.values.any { it.contains("raw sdk message") })
    }

    @Test
    fun `failed completion preserves ready to install state for retry`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        val infoSuccess = slot<OnSuccessListener<AppUpdateInfo>>()
        val failure = slot<OnFailureListener>()
        every { manager.appUpdateInfo } returns taskCapturingSuccess(infoSuccess)
        every { manager.registerListener(any()) } just runs
        every { manager.unregisterListener(any()) } just runs
        every { manager.completeUpdate() } returns taskCapturingVoidFailure(failure)
        val provider = provider(manager, telemetry)

        provider.startListeningForUpdates()
        infoSuccess.captured.onSuccess(updateInfo(status = InstallStatus.DOWNLOADED))
        provider.completeUpdate()
        failure.captured.onFailure(IllegalStateException("install failed"))

        assertEquals(
            AppUpdateState.ReadyToInstall,
            runBlocking { provider.appUpdateState.first() },
        )
        assertTrue(telemetry.calls.contains("clearInstallRequest"))
        val error = telemetry.events.single { it.first == "in_app_update_error" }.second
        assertEquals("install", error["stage"])
    }

    @Test
    fun `cancellation from launch is rethrown`() {
        val manager = mockk<AppUpdateManager>()
        val telemetry = RecordingTelemetry()
        every { manager.registerListener(any()) } just runs
        every {
            manager.startUpdateFlowForResult(any(), AppUpdateType.FLEXIBLE, any<Activity>(), AppUpdateProvider.UPDATE_REQUEST_CODE)
        } throws CancellationException("cancelled")
        val provider = provider(manager, telemetry)
        val info = updateInfo()
        val success = slot<OnSuccessListener<AppUpdateInfo>>()
        every { manager.appUpdateInfo } returns taskCapturingSuccess(success)
        provider.startListeningForUpdates()
        success.captured.onSuccess(info)

        var thrown: CancellationException? = null
        try {
            provider.startUpdate(mockk())
        } catch (error: CancellationException) {
            thrown = error
        }
        assertTrue(thrown != null)
    }

    private fun provider(manager: AppUpdateManager, telemetry: RecordingTelemetry) = AppUpdateProvider(
        context = mockk<Context>(relaxed = true),
        appUpdateManager = manager,
        telemetry = telemetry,
    )

    private fun updateInfo(
        availability: Int = UpdateAvailability.UPDATE_AVAILABLE,
        status: Int = InstallStatus.UNKNOWN,
        allowed: Boolean = true,
    ): AppUpdateInfo = mockk {
        every { availableVersionCode() } returns 20
        every { installStatus() } returns status
        every { updateAvailability() } returns availability
        every { isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) } returns allowed
    }

    private fun installState(status: Int, errorCode: Int = 0): InstallState = mockk {
        every { installStatus() } returns status
        every { installErrorCode() } returns errorCode
        every { bytesDownloaded() } returns 10
        every { totalBytesToDownload() } returns 100
    }

    private fun taskCapturingSuccess(success: CapturingSlot<OnSuccessListener<AppUpdateInfo>>): Task<AppUpdateInfo> =
        mockk<Task<AppUpdateInfo>>().also { task ->
            every { task.addOnSuccessListener(capture(success)) } returns task
            every { task.addOnFailureListener(any()) } returns task
        }

    private fun taskCapturingFailure(failure: CapturingSlot<OnFailureListener>): Task<AppUpdateInfo> =
        mockk<Task<AppUpdateInfo>>().also { task ->
            every { task.addOnSuccessListener(any()) } returns task
            every { task.addOnFailureListener(capture(failure)) } returns task
        }

    private fun taskCapturingVoidSuccess(success: CapturingSlot<OnSuccessListener<Void>>): Task<Void> =
        mockk<Task<Void>>().also { task ->
            every { task.addOnSuccessListener(capture(success)) } returns task
            every { task.addOnFailureListener(any()) } returns task
        }

    private fun taskCapturingVoidFailure(failure: CapturingSlot<OnFailureListener>): Task<Void> =
        mockk<Task<Void>>().also { task ->
            every { task.addOnSuccessListener(any()) } returns task
            every { task.addOnFailureListener(capture(failure)) } returns task
        }

    private class RecordingTelemetry : UpdateTelemetry {
        val calls = mutableListOf<String>()
        val trace = mutableListOf<String>()
        val events = mutableListOf<Pair<String, Map<String, String>>>()
        var recordedTargetVersion: Long? = null

        override fun record(event: String, parameters: Map<String, String>) {
            calls += event
            events += event to parameters
        }

        override fun observeAppLaunch() {
            calls += "observeAppLaunch"
        }

        override fun setTargetVersion(version: Long?) {
            recordedTargetVersion = version
            calls += "setTargetVersion:$version"
        }

        override fun installRequested() {
            calls += "installRequested"
            trace += "installRequested"
        }

        override fun clearInstallRequest() {
            calls += "clearInstallRequest"
        }
    }
}
