package com.desuzed.everyweather.analytics.update

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import org.json.JSONException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidUpdateTelemetryStoreTest {
    private val directories = mutableListOf<File>()

    @After
    fun cleanup() {
        directories.forEach { it.deleteRecursively() }
    }

    @Test
    fun emptyRead_returnsDefaultState() {
        val (_, store) = isolatedStore()

        assertEquals(UpdateTelemetryState(), store.read())
    }

    @Test
    fun roundTrip_preservesNullInstallerAndTarget() {
        val (_, store) = isolatedStore()
        val expected = UpdateTelemetryState(
            observedVersion = 42L,
            observedInstaller = null,
            pendingInstall = PendingUpdateInstall(
                fromVersion = 41L,
                targetVersion = null,
                requestedAtMillis = 123L,
                origin = UpdateInstallSource(
                    store = "unknown",
                    installerPackage = null,
                    lookupStatus = "unknown",
                ),
            ),
        )

        store.write(expected)

        assertEquals(expected, store.read())
        assertNull(store.read().observedInstaller)
        assertNull(store.read().pendingInstall?.targetVersion)
        assertNull(store.read().pendingInstall?.origin?.installerPackage)
    }

    @Test
    fun pendingGalaxyOrigin_persistsAcrossNewAdapter() {
        val (context, store) = isolatedStore()
        val expected = UpdateTelemetryState(
            observedVersion = 10L,
            pendingInstall = PendingUpdateInstall(
                fromVersion = 10L,
                targetVersion = 11L,
                requestedAtMillis = 456L,
                origin = UpdateInstallSource(
                    store = "galaxy_store",
                    installerPackage = "com.sec.android.app.samsungapps",
                ),
            ),
        )

        store.write(expected)

        assertEquals(expected, AndroidUpdateTelemetryStore(context).read())
    }

    @Test
    fun clearingPending_retainsObservedVersion() {
        val (context, store) = isolatedStore()
        store.write(
            UpdateTelemetryState(
                observedVersion = 99L,
                pendingInstall = PendingUpdateInstall(
                    fromVersion = 98L,
                    targetVersion = 99L,
                    requestedAtMillis = 789L,
                    origin = UpdateInstallSource("google_play"),
                ),
            ),
        )

        store.write(store.read().copy(pendingInstall = null))

        assertEquals(UpdateTelemetryState(observedVersion = 99L), AndroidUpdateTelemetryStore(context).read())
    }

    @Test
    fun malformedJson_throws() {
        val (context, _) = isolatedStore()
        File(context.noBackupFilesDir, "update_telemetry.json").writeText("not json")

        assertThrows(JSONException::class.java) {
            AndroidUpdateTelemetryStore(context).read()
        }
    }

    @Test
    fun missingBaseFile_recoversAtomicFileBackup() {
        val (context, _) = isolatedStore()
        File(context.noBackupFilesDir, "update_telemetry.json.bak").writeText(
            "{\"observedVersion\":7,\"observedInstaller\":\"com.sec.android.app.samsungapps\"}",
        )

        assertEquals(
            UpdateTelemetryState(observedVersion = 7L, observedInstaller = "com.sec.android.app.samsungapps"),
            AndroidUpdateTelemetryStore(context).read(),
        )
    }

    private fun isolatedStore(): Pair<Context, AndroidUpdateTelemetryStore> {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(target.cacheDir, "update-telemetry-${UUID.randomUUID()}").apply { mkdirs() }
        directories += directory
        val context = object : ContextWrapper(target) {
            override fun getNoBackupFilesDir(): File = directory
        }
        return context to AndroidUpdateTelemetryStore(context)
    }
}
