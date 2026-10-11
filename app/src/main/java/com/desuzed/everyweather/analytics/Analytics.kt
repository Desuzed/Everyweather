package com.desuzed.everyweather.analytics

import android.content.Context
import android.os.Bundle
import com.desuzed.everyweather.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics

abstract class Analytics(context: Context) {
    private val analyticsInstance = FirebaseAnalytics.getInstance(context)

    init {
        analyticsInstance.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    fun logEvent(event: String, parameters: Map<String, String> = emptyMap()) {
        val bundle = Bundle().apply {
            parameters.forEach { (key, value) -> putString(key, value) }
        }
        analyticsInstance.logEvent(event, bundle)
    }
}
