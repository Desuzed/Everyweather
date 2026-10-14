package com.desuzed.everyweather.data.repository.providers

import android.content.Context
import android.location.Location
import com.desuzed.everyweather.data.mapper.location.UserLatLngMapper
import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserLocationProviderTest {

    @Test
    fun `a late callback from a cancelled request cannot replace a newer search`() = runBlocking {
        val firstSuccess = slot<OnSuccessListener<Location?>>()
        val secondSuccess = slot<OnSuccessListener<Location?>>()
        val firstTask = taskCapturingSuccess(firstSuccess)
        val secondTask = taskCapturingSuccess(secondSuccess)
        val client = mockk<FusedLocationProviderClient>()
        every {
            client.getCurrentLocation(any<Int>(), any<CancellationToken>())
        } returnsMany listOf(firstTask, secondTask)
        val provider = provider(client)

        assertTrue(provider.findUserLocation())
        provider.onCancel()
        assertTrue(provider.findUserLocation())

        firstSuccess.captured.onSuccess(location(1.0, 2.0))
        assertNotNull(provider.userLocationFlow.first()!!.actionResult)

        secondSuccess.captured.onSuccess(location(55.7558, 37.6173))
        val result = provider.userLocationFlow.first()!!.userLatLng
        assertEquals(UserLatLng(55.7558, 37.6173, LOCATION_TIME), result)
    }

    @Test
    fun `security exception clears the active request so the next search can retry`() = runBlocking {
        val success = slot<OnSuccessListener<Location?>>()
        val retryTask = taskCapturingSuccess(success)
        val client = mockk<FusedLocationProviderClient>()
        var attempt = 0
        every { client.getCurrentLocation(any<Int>(), any<CancellationToken>()) } answers {
            if (attempt++ == 0) throw SecurityException("permission revoked")
            retryTask
        }
        val provider = provider(client)

        assertFalse(provider.findUserLocation())
        assertNotNull(provider.userLocationFlow.first()!!.actionResult)
        assertTrue(provider.findUserLocation())

        success.captured.onSuccess(location(10.0, 20.0))
        assertEquals(UserLatLng(10.0, 20.0, LOCATION_TIME), provider.userLocationFlow.first()!!.userLatLng)
        verify(exactly = 2) {
            client.getCurrentLocation(any<Int>(), any<CancellationToken>())
        }
    }

    @Test
    fun `failed location task clears the active request so a retry starts`() = runBlocking {
        val failure = slot<OnFailureListener>()
        val failedTask = mockk<Task<Location?>>()
        every { failedTask.addOnCanceledListener(any()) } returns failedTask
        every { failedTask.addOnFailureListener(capture(failure)) } returns failedTask
        every { failedTask.addOnSuccessListener(any()) } returns failedTask
        val retryTask = taskCapturingSuccess(slot())
        val client = mockk<FusedLocationProviderClient>()
        every {
            client.getCurrentLocation(any<Int>(), any<CancellationToken>())
        } returnsMany listOf(failedTask, retryTask)
        val provider = provider(client)

        assertTrue(provider.findUserLocation())
        failure.captured.onFailure(IllegalStateException("location unavailable"))

        assertNotNull(provider.userLocationFlow.first()!!.actionResult)
        assertTrue(provider.findUserLocation())
        verify(exactly = 2) {
            client.getCurrentLocation(any<Int>(), any<CancellationToken>())
        }
    }

    private fun provider(client: FusedLocationProviderClient): UserLocationProvider = UserLocationProvider(
        context = mockk<Context>(relaxed = true),
        userLatLngMapper = UserLatLngMapper(),
        fusedLocationClient = client,
        permissionChecker = { true },
    )

    private fun taskCapturingSuccess(slot: io.mockk.CapturingSlot<OnSuccessListener<Location?>>): Task<Location?> {
        val task = mockk<Task<Location?>>()
        every { task.addOnCanceledListener(any()) } returns task
        every { task.addOnFailureListener(any()) } returns task
        every { task.addOnSuccessListener(capture(slot)) } returns task
        return task
    }

    private fun location(latitude: Double, longitude: Double): Location = mockk<Location>().also { location ->
        every { location.latitude } returns latitude
        every { location.longitude } returns longitude
        every { location.time } returns LOCATION_TIME
    }

    private companion object {
        const val LOCATION_TIME = 1_700_000_000_000L
    }
}
