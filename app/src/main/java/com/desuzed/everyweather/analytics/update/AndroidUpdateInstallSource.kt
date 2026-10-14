package com.desuzed.everyweather.analytics.update

import android.content.Context
import android.os.Build
import kotlinx.coroutines.CancellationException

object AndroidUpdateInstallSource {
    fun read(context: Context): UpdateInstallSource {
        return try {
            val packageManager = context.packageManager
            val installerPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(context.packageName)
            }
            classifyInstallSource(installerPackage)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            classifyInstallSource(null, lookupFailed = true)
        }
    }
}
