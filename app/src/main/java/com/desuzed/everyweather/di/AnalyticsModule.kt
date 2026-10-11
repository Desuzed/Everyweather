package com.desuzed.everyweather.di

import com.desuzed.everyweather.analytics.InAppUpdateAnalytics
import com.desuzed.everyweather.analytics.LocationMainAnalytics
import com.desuzed.everyweather.analytics.MapPointSelectionAnalytics
import com.desuzed.everyweather.analytics.SettingsAnalytics
import com.desuzed.everyweather.analytics.WeatherMainAnalytics
import com.desuzed.everyweather.BuildConfig
import com.desuzed.everyweather.analytics.update.AndroidUpdateInstallSource
import com.desuzed.everyweather.analytics.update.AndroidUpdateTelemetryStore
import com.desuzed.everyweather.analytics.update.FirebaseUpdateEventSink
import com.desuzed.everyweather.analytics.update.UpdateTelemetry
import com.desuzed.everyweather.analytics.update.UpdateTelemetryRecorder
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val analyticsModule = module {
    single<UpdateTelemetry> {
        val context = androidApplication()
        UpdateTelemetryRecorder(
            sink = FirebaseUpdateEventSink(context),
            store = AndroidUpdateTelemetryStore(context),
            source = AndroidUpdateInstallSource.read(context),
            currentVersion = BuildConfig.VERSION_CODE.toLong(),
        )
    }
    single {
        SettingsAnalytics(androidApplication(), get())
    }
    single {
        WeatherMainAnalytics(androidApplication())
    }
    single {
        LocationMainAnalytics(androidApplication())
    }
    single {
        InAppUpdateAnalytics(get())
    }
    single {
        MapPointSelectionAnalytics(androidApplication())
    }
}
