package com.desuzed.everyweather.analytics.update

import org.junit.Assert.assertEquals
import org.junit.Test

class InstallSourceClassifierTest {

    @Test
    fun `recognizes supported stores`() {
        assertEquals("google_play", classifyInstallSource("com.android.vending").store)
        assertEquals("galaxy_store", classifyInstallSource("com.sec.android.app.samsungapps").store)
        assertEquals("app_gallery", classifyInstallSource("com.huawei.appmarket").store)
        assertEquals("amazon", classifyInstallSource("com.amazon.venezia").store)
    }

    @Test
    fun `classifies package installer and unknown packages`() {
        assertEquals("package_installer", classifyInstallSource("com.android.packageinstaller").store)
        assertEquals("other", classifyInstallSource("com.example.enterprise-store").store)
        assertEquals("unknown", classifyInstallSource(null).store)
    }

    @Test
    fun `preserves lookup status for unknown and error lookups`() {
        assertEquals("unknown", classifyInstallSource(null).lookupStatus)
        assertEquals("error", classifyInstallSource("com.android.vending", lookupFailed = true).lookupStatus)
        assertEquals("unknown", classifyInstallSource("com.android.vending", lookupFailed = true).store)
    }
}
