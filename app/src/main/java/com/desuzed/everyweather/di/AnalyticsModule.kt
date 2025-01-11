package com.desuzed.everyweather.di

import com.desuzed.everyweather.analytics.InAppUpdateAnalytics
import com.desuzed.everyweather.analytics.LocationMainAnalytics
import com.desuzed.everyweather.analytics.MapPointSelectionAnalytics
import com.desuzed.everyweather.analytics.SettingsAnalytics
import com.desuzed.everyweather.analytics.WeatherMainAnalytics
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val analyticsModule = module {
    single {
        SettingsAnalytics(androidApplication())
    }
    single {
        WeatherMainAnalytics(androidApplication())
    }
    single {
        LocationMainAnalytics(androidApplication())
    }
    single {
        InAppUpdateAnalytics(androidApplication())
    }
    single {
        MapPointSelectionAnalytics(androidApplication())
    }
}