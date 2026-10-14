package com.desuzed.everyweather.analytics.update

data class UpdateInstallSource(
    val store: String,
    val installerPackage: String? = null,
    val lookupStatus: String = UpdateAnalyticsContract.SourceStatus.KNOWN,
)

data class PendingUpdateInstall(
    val fromVersion: Long,
    val targetVersion: Long?,
    val requestedAtMillis: Long,
    val origin: UpdateInstallSource,
)

data class UpdateTelemetryState(
    val observedVersion: Long? = null,
    val observedInstaller: String? = null,
    val pendingInstall: PendingUpdateInstall? = null,
)

interface UpdateTelemetryStore {
    fun read(): UpdateTelemetryState
    fun write(state: UpdateTelemetryState)
}

fun interface UpdateEventSink {
    fun record(event: String, parameters: Map<String, String>)
}

interface UpdateTelemetry {
    fun record(event: String, parameters: Map<String, String> = emptyMap())
    fun observeAppLaunch()
    fun setTargetVersion(version: Long?)
    fun installRequested()
    fun clearInstallRequest()
}

object NoOpUpdateTelemetry : UpdateTelemetry {
    override fun record(event: String, parameters: Map<String, String>) = Unit
    override fun observeAppLaunch() = Unit
    override fun setTargetVersion(version: Long?) = Unit
    override fun installRequested() = Unit
    override fun clearInstallRequest() = Unit
}
