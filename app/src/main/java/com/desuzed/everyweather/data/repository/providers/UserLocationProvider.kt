package com.desuzed.everyweather.data.repository.providers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.desuzed.everyweather.data.mapper.location.UserLatLngMapper
import com.desuzed.everyweather.data.repository.providers.action_result.GeoActionResultProvider.Companion.LOCATION_NOT_FOUND
import com.desuzed.everyweather.data.repository.providers.action_result.GeoActionResultProvider.Companion.NO_LOCATION_PERMISSIONS
import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.model.location.UserLocationResult
import com.desuzed.everyweather.domain.model.result.ActionResult
import com.desuzed.everyweather.domain.model.result.ActionType
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserLocationProvider(
    private val context: Context,
    private val userLatLngMapper: UserLatLngMapper,
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context),
    private val permissionChecker: () -> Boolean = { hasLocationPermission(context) },
) {
    private var lookingForLocation: Task<Location?>? = null
    private var activeRequestId: Long? = null
    private var nextRequestId = 0L

    private val _userLocationFlow = MutableStateFlow<UserLocationResult?>(null)
    val userLocationFlow: Flow<UserLocationResult?> = _userLocationFlow.asStateFlow()

    private var cancellationTokenSource: CancellationTokenSource? = null

    fun findUserLocation(): Boolean {
        val oldValue = _userLocationFlow.value?.userLatLng
        val shouldRefreshLocation = oldValue?.let { shouldRefreshUserLocation(it) }
        if (shouldRefreshLocation == false) {
            if (oldValue != null) {
                _userLocationFlow.value = UserLocationResult(userLatLng = oldValue)
            }
            return false
        }
        if (lookingForLocation != null) return true
        if (arePermissionsGranted()) {
            val newCancellationSource = CancellationTokenSource()
            val requestId = ++nextRequestId
            cancellationTokenSource = newCancellationSource
            activeRequestId = requestId
            val task = try {
                getCurrentLocation(newCancellationSource)
            } catch (_: SecurityException) {
                stopLookingForLocation(requestId)
                onNull()
                return false
            }
            lookingForLocation = task
            task
                .addOnCanceledListener {
                    if (!isActiveRequest(requestId)) return@addOnCanceledListener
                    onNull()
                    stopLookingForLocation(requestId)
                }
                .addOnFailureListener {
                    if (!isActiveRequest(requestId)) return@addOnFailureListener
                    onNull()
                    stopLookingForLocation(requestId)
                }
                .addOnSuccessListener { location ->
                    if (!isActiveRequest(requestId)) return@addOnSuccessListener
                    if (location != null) {
                        onSuccess(location)
                    } else {
                        onNull()
                    }
                    stopLookingForLocation(requestId)
                }
            return true
        } else {
            val actionError = ActionResult(
                code = NO_LOCATION_PERMISSIONS,
                actionType = ActionType.OK
            )
            _userLocationFlow.value = UserLocationResult(actionResult = actionError)
            stopLookingForLocation()
            return false
        }
    }

    fun arePermissionsGranted(): Boolean {
        return permissionChecker()
    }

    fun onCancel() {
        stopLookingForLocation(activeRequestId)
        onNull()
    }

    /**
     * [findUserLocation] calls this only after [arePermissionsGranted] succeeds. Permission can
     * still be revoked between that check and this API call, so the caller also handles
     * [SecurityException] and publishes a retryable result.
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(cancellationTokenSource: CancellationTokenSource): Task<Location?> =
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellationTokenSource.token,
        )

    private fun isActiveRequest(requestId: Long): Boolean = activeRequestId == requestId

    private fun stopLookingForLocation(requestId: Long? = null) {
        if (requestId != null && !isActiveRequest(requestId)) return
        val tokenSource = cancellationTokenSource
        cancellationTokenSource = null
        lookingForLocation = null
        activeRequestId = null
        tokenSource?.cancel()
    }

    private fun onSuccess(location: Location) {
        val result = userLatLngMapper.mapFromEntity(location)
        _userLocationFlow.value = UserLocationResult(userLatLng = result)
    }

    private fun onNull() {
        val actionError = ActionResult(
            code = LOCATION_NOT_FOUND,
            actionType = ActionType.RETRY
        )
        _userLocationFlow.value = UserLocationResult(actionResult = actionError)
    }

    private fun shouldRefreshUserLocation(userLatLng: UserLatLng): Boolean =
        System.currentTimeMillis() - userLatLng.time > SIXTY_SECONDS

    companion object {
        private const val SIXTY_SECONDS = 60_000
        private const val GRANTED = PackageManager.PERMISSION_GRANTED
        private const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

        private fun hasLocationPermission(context: Context): Boolean {
            val permissionFine = ContextCompat.checkSelfPermission(context, FINE_LOCATION)
            val permissionCoarse = ContextCompat.checkSelfPermission(context, COARSE_LOCATION)
            return permissionFine == GRANTED || permissionCoarse == GRANTED
        }
    }

}
