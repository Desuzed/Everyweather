package com.desuzed.everyweather.data.repository.providers.app_update

import android.app.Activity
import android.content.Context
import com.desuzed.everyweather.domain.model.app_update.AppUpdateState
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AppUpdateProviderTest {

    @Test
    fun `each availability check requests fresh update info`() = runBlocking {
        val manager = mockk<AppUpdateManager>()
        val info = updateInfo()
        val firstSuccess = slot<OnSuccessListener<AppUpdateInfo>>()
        val secondSuccess = slot<OnSuccessListener<AppUpdateInfo>>()
        val firstTask = taskCapturingSuccess(firstSuccess)
        val secondTask = taskCapturingSuccess(secondSuccess)
        every { manager.appUpdateInfo } returnsMany listOf(firstTask, secondTask)
        val provider = provider(manager)

        provider.startListeningForUpdates()
        firstSuccess.captured.onSuccess(info)
        provider.startListeningForUpdates()
        secondSuccess.captured.onSuccess(info)

        assertEquals(AppUpdateState.UpdateAvailable, provider.appUpdateState.first())
        verify(exactly = 2) { manager.appUpdateInfo }
    }

    @Test
    fun `registers once before launching and clears progress after cancellation`() = runBlocking {
        val manager = mockk<AppUpdateManager>()
        val info = updateInfo()
        val listener = slot<InstallStateUpdatedListener>()
        val success = slot<OnSuccessListener<AppUpdateInfo>>()
        val task = taskCapturingSuccess(success)
        every { manager.appUpdateInfo } returns task
        every { manager.registerListener(capture(listener)) } just runs
        every { manager.unregisterListener(any()) } just runs
        every {
            manager.startUpdateFlowForResult(info, AppUpdateType.FLEXIBLE, any<Activity>(), AppUpdateProvider.UPDATE_REQUEST_CODE)
        } returns true
        val provider = provider(manager)

        provider.startListeningForUpdates()
        success.captured.onSuccess(info)
        provider.startUpdate(mockk<Activity>())
        provider.startUpdate(mockk<Activity>())

        verify(exactly = 1) { manager.registerListener(any()) }
        verify(exactly = 2) {
            manager.startUpdateFlowForResult(info, AppUpdateType.FLEXIBLE, any<Activity>(), AppUpdateProvider.UPDATE_REQUEST_CODE)
        }

        val cancelled = mockk<InstallState>()
        every { cancelled.installStatus() } returns InstallStatus.CANCELED
        listener.captured.onStateUpdate(cancelled)

        assertEquals(
            AppUpdateState.Downloading(totalBytes = 0, bytesDownloaded = 0, isUpdateLoading = false),
            provider.appUpdateState.first(),
        )
        verify(exactly = 1) { manager.unregisterListener(any()) }
    }

    @Test
    fun `failed availability check clears progress state`() = runBlocking {
        val manager = mockk<AppUpdateManager>()
        val failure = slot<OnFailureListener>()
        val task = taskCapturingFailure(failure)
        every { manager.appUpdateInfo } returns task
        val provider = provider(manager)

        provider.startListeningForUpdates()
        failure.captured.onFailure(IllegalStateException("Play unavailable"))

        assertEquals(
            AppUpdateState.Downloading(totalBytes = 0, bytesDownloaded = 0, isUpdateLoading = false),
            provider.appUpdateState.first(),
        )
    }

    @Test
    fun `failed update launch clears progress and unregisters listener`() = runBlocking {
        val manager = mockk<AppUpdateManager>()
        val info = updateInfo()
        val success = slot<OnSuccessListener<AppUpdateInfo>>()
        val task = taskCapturingSuccess(success)
        every { manager.appUpdateInfo } returns task
        every { manager.registerListener(any()) } just runs
        every { manager.unregisterListener(any()) } just runs
        every {
            manager.startUpdateFlowForResult(info, AppUpdateType.FLEXIBLE, any<Activity>(), AppUpdateProvider.UPDATE_REQUEST_CODE)
        } returns false
        val provider = provider(manager)

        provider.startListeningForUpdates()
        success.captured.onSuccess(info)
        provider.startUpdate(mockk<Activity>())

        assertEquals(
            AppUpdateState.Downloading(totalBytes = 0, bytesDownloaded = 0, isUpdateLoading = false),
            provider.appUpdateState.first(),
        )
        verify(exactly = 1) { manager.unregisterListener(any()) }
    }

    private fun provider(manager: AppUpdateManager) = AppUpdateProvider(
        context = mockk<Context>(relaxed = true),
        appUpdateManager = manager,
    )

    private fun updateInfo(): AppUpdateInfo = mockk {
        every { availableVersionCode() } returns 20
        every { installStatus() } returns InstallStatus.UNKNOWN
        every { updateAvailability() } returns UpdateAvailability.UPDATE_AVAILABLE
        every { isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) } returns true
    }

    private fun taskCapturingSuccess(
        success: io.mockk.CapturingSlot<OnSuccessListener<AppUpdateInfo>>,
    ): Task<AppUpdateInfo> = mockk<Task<AppUpdateInfo>>().also { task ->
        every { task.addOnSuccessListener(capture(success)) } returns task
        every { task.addOnFailureListener(any()) } returns task
    }

    private fun taskCapturingFailure(
        failure: io.mockk.CapturingSlot<OnFailureListener>,
    ): Task<AppUpdateInfo> = mockk<Task<AppUpdateInfo>>().also { task ->
        every { task.addOnSuccessListener(any()) } returns task
        every { task.addOnFailureListener(capture(failure)) } returns task
    }
}
