package com.desuzed.everyweather.analytics.update

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import org.json.JSONObject

class AndroidUpdateTelemetryStore(context: Context) : UpdateTelemetryStore {
    private val file = AtomicFile(File(context.noBackupFilesDir, FILE_NAME))

    override fun read(): UpdateTelemetryState {
        val input = try {
            // AtomicFile.openRead() restores a valid .bak when the base file is absent.
            file.openRead()
        } catch (error: FileNotFoundException) {
            val backup = File(file.baseFile.path + ".bak")
            if (!file.baseFile.exists() && !backup.exists()) return UpdateTelemetryState()
            throw error
        }
        return input.use {
            val json = JSONObject(String(it.readBytes(), StandardCharsets.UTF_8))
            UpdateTelemetryState(
                observedVersion = json.optionalLong("observedVersion"),
                observedInstaller = json.optionalString("observedInstaller"),
                pendingInstall = json.optJSONObject("pending")?.let(::readPending),
            )
        }
    }

    override fun write(state: UpdateTelemetryState) {
        val output = file.startWrite()
        try {
            state.toJson().toString().toByteArray(StandardCharsets.UTF_8).let(output::write)
            file.finishWrite(output)
        } catch (error: Throwable) {
            file.failWrite(output)
            throw error
        }
    }

    private fun readPending(json: JSONObject): PendingUpdateInstall {
        val originJson = json.getJSONObject("origin")
        return PendingUpdateInstall(
            fromVersion = json.getLong("fromVersion"),
            targetVersion = json.optionalLong("targetVersion"),
            requestedAtMillis = json.getLong("requestedAtMillis"),
            origin = UpdateInstallSource(
                store = originJson.getString("store"),
                installerPackage = originJson.optionalString("installerPackage"),
                lookupStatus = originJson.optString(
                    "lookupStatus",
                    UpdateAnalyticsContract.SourceStatus.KNOWN,
                ),
            ),
        )
    }

    private fun UpdateTelemetryState.toJson(): JSONObject = JSONObject().apply {
        observedVersion?.let { put("observedVersion", it) }
        observedInstaller?.let { put("observedInstaller", it) }
        pendingInstall?.let { put("pending", it.toJson()) }
    }

    private fun PendingUpdateInstall.toJson(): JSONObject = JSONObject().apply {
        put("fromVersion", fromVersion)
        targetVersion?.let { put("targetVersion", it) }
        put("requestedAtMillis", requestedAtMillis)
        put("origin", JSONObject().apply {
            put("store", origin.store)
            origin.installerPackage?.let { put("installerPackage", it) }
            put("lookupStatus", origin.lookupStatus)
        })
    }

    private fun JSONObject.optionalLong(name: String): Long? =
        if (has(name) && !isNull(name)) getLong(name) else null

    private fun JSONObject.optionalString(name: String): String? =
        if (has(name) && !isNull(name)) getString(name) else null

    private companion object {
        const val FILE_NAME = "update_telemetry.json"
    }
}
