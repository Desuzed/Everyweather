package com.desuzed.everyweather.analytics.update

import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.InstallerPackages
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.SourceStatus
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.Sources

/** Maps the package reported by Android to a coarse install source for telemetry. */
fun classifyInstallSource(
    installerPackage: String?,
    lookupFailed: Boolean = false,
): UpdateInstallSource {
    val store = when {
        lookupFailed -> Sources.UNKNOWN
        installerPackage == null -> Sources.UNKNOWN
        installerPackage == InstallerPackages.GOOGLE_PLAY -> Sources.GOOGLE_PLAY
        installerPackage == InstallerPackages.GALAXY_STORE -> Sources.GALAXY_STORE
        installerPackage == InstallerPackages.APP_GALLERY -> Sources.APP_GALLERY
        installerPackage == InstallerPackages.AMAZON -> Sources.AMAZON
        installerPackage == InstallerPackages.ANDROID ||
            installerPackage == InstallerPackages.GOOGLE ||
            installerPackage == InstallerPackages.MIUI -> Sources.PACKAGE_INSTALLER
        else -> Sources.OTHER
    }
    return UpdateInstallSource(
        store = store,
        installerPackage = installerPackage,
        lookupStatus = if (lookupFailed) SourceStatus.ERROR
        else if (installerPackage == null) SourceStatus.UNKNOWN
        else SourceStatus.KNOWN,
    )
}
