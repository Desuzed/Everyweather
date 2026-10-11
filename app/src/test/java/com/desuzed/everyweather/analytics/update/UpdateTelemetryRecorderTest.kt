package com.desuzed.everyweather.analytics.update

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateTelemetryRecorderTest {

    @Test
    fun `launch is recorded once and install source is emitted once per version`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        val recorder = recorder(sink, store, currentVersion = 10)

        recorder.observeAppLaunch()
        recorder.observeAppLaunch()
        recorder.observeAppLaunch()

        assertEquals(listOf("app_install_source"), sink.events.map { it.first })
        assertEquals("10", sink.events[0].second["current_version"])
        assertEquals("google_play", sink.events[0].second["install_source"])
        assertEquals("com.android.vending", sink.events[0].second["installer_package"])
        assertEquals(10L, store.value.observedVersion)
    }

    @Test
    fun `launch does not duplicate source after recreating recorder`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        recorder(sink, store, currentVersion = 10).observeAppLaunch()
        recorder(sink, store, currentVersion = 10).observeAppLaunch()

        assertEquals(listOf("app_install_source"), sink.events.map { it.first })
    }

    @Test
    fun `same version with changed installer records a new source`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        recorder(sink, store, currentVersion = 10).observeAppLaunch()
        recorder(
            sink,
            store,
            source = UpdateInstallSource("galaxy_store", "com.sec.android.app.samsungapps"),
            currentVersion = 10,
        ).observeAppLaunch()

        assertEquals(2, sink.events.count { it.first == "app_install_source" })
    }

    @Test
    fun `unknown installer is deduplicated across recreated recorders`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        recorder(sink, store, source = UpdateInstallSource("unknown"), currentVersion = 10).observeAppLaunch()
        recorder(sink, store, source = UpdateInstallSource("unknown"), currentVersion = 10).observeAppLaunch()

        assertEquals(1, sink.events.count { it.first == "app_install_source" })
    }

    @Test
    fun `higher version after request is observed as matching in app update`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        val origin = UpdateInstallSource("galaxy_store", "com.sec.android.app.samsungapps")
        val old = recorder(sink, store, source = origin, currentVersion = 10, now = 1000L)
        old.setTargetVersion(11)
        old.installRequested()

        val after = recorder(
            sink,
            store,
            source = UpdateInstallSource("google_play", "com.android.vending"),
            currentVersion = 11,
            now = 2000L,
        )
        after.observeAppLaunch()

        val observed = sink.events.single { it.first == "in_app_update_version_observed" }.second
        assertEquals("true", observed["matches_target"])
        assertEquals("after_install_request", observed["evidence"])
        assertEquals("galaxy_store", observed["origin_install_source"])
        assertEquals("google_play", observed["install_source"])
        assertEquals(11L, store.value.observedVersion)
        assertNull(store.value.pendingInstall)
    }

    @Test
    fun `same version remains pending and is not reported as failure`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        val recorder = recorder(sink, store, currentVersion = 10, now = 1000L)
        recorder.observeAppLaunch()
        recorder.setTargetVersion(11)
        recorder.installRequested()

        recorder(sink, store, currentVersion = 10, now = 2000L).observeAppLaunch()

        assertEquals(1, sink.events.count { it.first == "in_app_update_install_pending" })
        assertNotNull(store.value.pendingInstall)
        assertFalse(sink.events.any { it.first == "in_app_update_install_unconfirmed" })
    }

    @Test
    fun `higher version below requested target is observed without claiming a match`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        val recorder = recorder(sink, store, currentVersion = 10)
        recorder.observeAppLaunch()
        recorder.setTargetVersion(12)
        recorder.installRequested()

        recorder(sink, store, currentVersion = 11).observeAppLaunch()

        val event = sink.events.single { it.first == "in_app_update_version_observed" }
        assertEquals("false", event.second["matches_target"])
    }

    @Test
    fun `higher version with unknown target records unknown match`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        val recorder = recorder(sink, store, currentVersion = 10)
        recorder.observeAppLaunch()
        recorder.installRequested()

        recorder(sink, store, currentVersion = 11).observeAppLaunch()

        val event = sink.events.single { it.first == "in_app_update_version_observed" }
        assertEquals("unknown", event.second["matches_target"])
    }

    @Test
    fun `expired pending install is unconfirmed and marker is cleared`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        val recorder = recorder(sink, store, currentVersion = 10, now = 0L)
        recorder.observeAppLaunch()
        recorder.installRequested()

        recorder(sink, store, currentVersion = 10, now = 7 * DAY + 1).observeAppLaunch()

        val event = sink.events.single { it.first == "in_app_update_install_unconfirmed" }
        assertEquals("in_app_update_install_unconfirmed", event.first)
        assertEquals("expired", event.second["reason"])
        assertNull(store.value.pendingInstall)
    }

    @Test
    fun `pending install at exact seven day boundary remains pending`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        val recorder = recorder(sink, store, currentVersion = 10, now = 0L)
        recorder.observeAppLaunch()
        recorder.installRequested()

        recorder(sink, store, currentVersion = 10, now = DAY * 7).observeAppLaunch()

        assertEquals(1, sink.events.count { it.first == "in_app_update_install_pending" })
        assertNotNull(store.value.pendingInstall)
    }

    @Test
    fun `repeated request for same target preserves original request time`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        val recorder = recorder(sink, store, currentVersion = 10, now = 100L)
        recorder.observeAppLaunch()
        recorder.setTargetVersion(11)
        recorder.installRequested()

        recorder(sink, store, currentVersion = 10, now = 200L).also {
            it.setTargetVersion(11)
            it.installRequested()
        }

        assertEquals(100L, store.value.pendingInstall?.requestedAtMillis)
    }

    @Test
    fun `version rollback is reported as unconfirmed and clears marker`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        val recorder = recorder(sink, store, currentVersion = 10)
        recorder.observeAppLaunch()
        recorder.installRequested()

        recorder(sink, store, currentVersion = 9).observeAppLaunch()

        val event = sink.events.single { it.first == "in_app_update_install_unconfirmed" }
        assertEquals("version_rollback", event.second["reason"])
        assertNull(store.value.pendingInstall)
    }

    @Test
    fun `clear request removes only pending marker`() {
        val sink = CollectingSink()
        val store = MemoryStore()
        val recorder = recorder(sink, store, currentVersion = 10)
        recorder.observeAppLaunch()
        recorder.installRequested()
        recorder.clearInstallRequest()

        assertNull(store.value.pendingInstall)
        assertEquals(10L, store.value.observedVersion)
    }

    @Test
    fun `common metadata wins over caller parameters`() {
        val sink = CollectingSink()
        val recorder = recorder(sink, MemoryStore(), currentVersion = 42)

        recorder.record(
            "custom_event",
            mapOf("current_version" to "wrong", "install_source" to "wrong", "stage" to "download"),
        )

        val params = sink.events.single().second
        assertEquals("42", params["current_version"])
        assertEquals("google_play", params["install_source"])
        assertEquals("download", params["stage"])
        assertEquals("google_play", params["update_channel"])
    }

    @Test
    fun `sink and store failures are contained`() {
        val sink = CollectingSink(fail = true)
        val store = MemoryStore(failReads = true, failWrites = true)
        val recorder = recorder(sink, store, currentVersion = 10)

        recorder.observeAppLaunch()
        recorder.installRequested()
        recorder.clearInstallRequest()

        assertEquals(0, sink.events.size)
    }

    @Test(expected = CancellationException::class)
    fun `cancellation from store read is propagated`() {
        UpdateTelemetryRecorder(
            sink = CollectingSink(),
            store = MemoryStore(readError = CancellationException("cancel")),
            source = UpdateInstallSource("google_play"),
            currentVersion = 10,
        )
    }

    @Test(expected = CancellationException::class)
    fun `cancellation from sink is propagated`() {
        val recorder = recorder(CancellationSink(), MemoryStore(), currentVersion = 10)
        recorder.record("event")
    }

    @Test(expected = CancellationException::class)
    fun `cancellation from store write is propagated`() {
        val recorder = recorder(
            CollectingSink(),
            MemoryStore(writeError = CancellationException("cancel")),
            currentVersion = 10,
        )
        recorder.observeAppLaunch()
    }

    @Test
    fun `storage error telemetry contains no raw exception message`() {
        val sink = CollectingSink()
        val recorder = recorder(sink, MemoryStore(readError = IllegalStateException("private detail")), currentVersion = 10)

        recorder.observeAppLaunch()

        val error = sink.events.single { it.first == "in_app_update_error" }.second
        assertEquals("read_failed", error["error_name"])
        assertFalse(error.values.any { it.contains("private detail") })
    }

    private fun recorder(
        sink: UpdateEventSink,
        store: UpdateTelemetryStore,
        source: UpdateInstallSource = UpdateInstallSource("google_play", "com.android.vending"),
        currentVersion: Long = 10,
        now: Long = 0L,
    ) = UpdateTelemetryRecorder(
        sink = sink,
        store = store,
        source = source,
        currentVersion = currentVersion,
        nowMillis = { now },
    )

    private class CollectingSink(private val fail: Boolean = false) : UpdateEventSink {
        val events = CopyOnWriteArrayList<Pair<String, Map<String, String>>>()

        override fun record(event: String, parameters: Map<String, String>) {
            if (fail) throw IllegalStateException("sink")
            events += event to parameters
        }
    }

    private class MemoryStore(
        private val failReads: Boolean = false,
        private val failWrites: Boolean = false,
        private val readError: Exception? = null,
        private val writeError: Exception? = null,
    ) : UpdateTelemetryStore {
        var value = UpdateTelemetryState()

        override fun read(): UpdateTelemetryState {
            readError?.let { throw it }
            if (failReads) throw IllegalStateException("read")
            return value
        }

        override fun write(state: UpdateTelemetryState) {
            writeError?.let { throw it }
            if (failWrites) throw IllegalStateException("write")
            value = state
        }
    }

    private class CancellationSink : UpdateEventSink {
        override fun record(event: String, parameters: Map<String, String>): Unit =
            throw CancellationException("cancel")
    }

    private companion object {
        const val DAY = 24 * 60 * 60 * 1000L
    }
}
