package com.desuzed.everyweather.analytics.update

import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.ErrorNames
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.Events
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.MAX_PARAMETER_VALUE_LENGTH
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.Params
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.Values
import java.util.concurrent.CancellationException

/** Diagnostics never decide whether Play can update an installation from another store. */
class UpdateTelemetryRecorder(
    private val sink: UpdateEventSink,
    private val store: UpdateTelemetryStore,
    private val source: UpdateInstallSource,
    private val currentVersion: Long,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : UpdateTelemetry {
    private var targetVersion: Long? = null
    private var launchObserved = false
    private var state = readState()

    override fun record(event: String, parameters: Map<String, String>) {
        safely { sink.record(event, parameters + commonParameters()) }
    }

    override fun setTargetVersion(version: Long?) {
        targetVersion = version?.takeIf { it > 0 }
    }

    override fun observeAppLaunch() {
        if (launchObserved) return
        launchObserved = true
        if (state.observedVersion != currentVersion || state.observedInstaller != source.installerPackage) {
            record(Events.INSTALL_SOURCE)
        }
        var pending = state.pendingInstall
        if (pending != null) {
            val origin = pendingParameters(pending)
            when {
                nowMillis() - pending.requestedAtMillis > PENDING_LIFETIME_MILLIS -> {
                    record(Events.INSTALL_UNCONFIRMED, origin + (Params.REASON to Values.EXPIRED))
                    pending = null
                }
                currentVersion > pending.fromVersion -> {
                    // Another store/manual update may have supplied this version. This
                    // is observed version evidence, never proof of Play attribution.
                    record(Events.VERSION_OBSERVED, origin + (Params.MATCHES_TARGET to
                        (pending.targetVersion?.let { (currentVersion >= it).toString() } ?: Values.UNKNOWN)))
                    pending = null
                }
                currentVersion < pending.fromVersion -> {
                    record(Events.INSTALL_UNCONFIRMED, origin + (Params.REASON to Values.VERSION_ROLLBACK))
                    pending = null
                }
                else -> record(Events.INSTALL_PENDING, origin)
            }
        }
        state = state.copy(observedVersion = currentVersion,
            observedInstaller = source.installerPackage, pendingInstall = pending)
        writeState()
    }

    override fun installRequested() {
        val existing = state.pendingInstall
        val pending = if (existing?.fromVersion == currentVersion && existing.targetVersion == targetVersion) {
            existing
        } else PendingUpdateInstall(currentVersion, targetVersion, nowMillis(), source)
        state = state.copy(pendingInstall = pending)
        // Persist before completeUpdate(), which may terminate the process immediately.
        writeState()
        record(Events.INSTALL_REQUESTED)
    }

    override fun clearInstallRequest() {
        if (state.pendingInstall == null) return
        state = state.copy(pendingInstall = null)
        writeState()
    }

    private fun readState(): UpdateTelemetryState = try {
        store.read()
    } catch (error: Exception) {
        preserveCancellation(error)
        record(Events.ERROR, storageError(ErrorNames.READ_FAILED, error))
        UpdateTelemetryState()
    }

    private fun writeState() {
        try {
            store.write(state)
        } catch (error: Exception) {
            preserveCancellation(error)
            record(Events.ERROR, storageError(ErrorNames.WRITE_FAILED, error))
        }
    }

    private fun commonParameters(): Map<String, String> = buildMap {
        put(Params.INSTALL_SOURCE, source.store)
        put(Params.INSTALLER_PACKAGE, (source.installerPackage ?: Values.UNKNOWN).take(MAX_PARAMETER_VALUE_LENGTH))
        put(Params.INSTALL_SOURCE_STATUS, source.lookupStatus)
        put(Params.UPDATE_CHANNEL, Values.GOOGLE_PLAY_CHANNEL)
        put(Params.CURRENT_VERSION, currentVersion.toString())
        targetVersion?.let { put(Params.TARGET_VERSION, it.toString()) }
    }

    private fun pendingParameters(pending: PendingUpdateInstall): Map<String, String> = mapOf(
        Params.FROM_VERSION to pending.fromVersion.toString(),
        Params.REQUESTED_VERSION to (pending.targetVersion?.toString() ?: Values.UNKNOWN),
        Params.ORIGIN_INSTALL_SOURCE to pending.origin.store,
        Params.ORIGIN_INSTALLER_PACKAGE to
            (pending.origin.installerPackage ?: Values.UNKNOWN).take(MAX_PARAMETER_VALUE_LENGTH),
        Params.EVIDENCE to Values.AFTER_INSTALL_REQUEST,
    )

    private fun storageError(reason: String, error: Exception): Map<String, String> = UpdateAnalyticsError(
        stage = UpdateStage.TELEMETRY_STORAGE,
        name = reason,
        exceptionType = error.javaClass.simpleName,
    ).toParameters()

    private inline fun safely(block: () -> Unit) {
        try {
            block()
        } catch (error: Exception) {
            preserveCancellation(error)
        }
    }

    private fun preserveCancellation(error: Exception) {
        if (error is CancellationException) throw error
    }

    companion object {
        const val PENDING_LIFETIME_MILLIS = 7L * 24 * 60 * 60 * 1_000
    }
}
