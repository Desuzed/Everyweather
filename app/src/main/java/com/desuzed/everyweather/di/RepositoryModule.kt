package com.desuzed.everyweather.di

import com.desuzed.everyweather.data.repository.local.WeatherDataRepositoryImpl
import com.desuzed.everyweather.data.repository.providers.UserLocationProvider
import com.desuzed.everyweather.data.repository.providers.app_update.AppUpdateProvider
import com.desuzed.everyweather.data.repository.remote.RemoteDataRepositoryImpl
import com.desuzed.everyweather.data.repository.settings.SystemSettingsRepositoryImpl
import com.desuzed.everyweather.data.repository.settings.WeatherSettingRepositoryImpl
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.domain.repository.remote.RemoteDataRepository
import com.desuzed.everyweather.domain.repository.settings.SystemSettingsRepository
import com.desuzed.everyweather.domain.repository.settings.WeatherSettingsRepository
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val repositoryModule = module {

    single {
        UserLocationProvider(context = androidApplication(), userLatLngMapper = get())
    }

    single {
        AppUpdateProvider(androidApplication(), telemetry = get())
    }

    single<RemoteDataRepository> {
        RemoteDataRepositoryImpl(
            weatherApi = get(),
            locationIqApi = get(),
            weatherResponseMapper = get(),
            apiErrorMapper = get(),
            locationResponseMapper = get(),
            dispatcher = Dispatchers.IO,
        )
    }

    single<WeatherSettingsRepository> {
        WeatherSettingRepositoryImpl(datastoreProvider = get())
    }

    single<SystemSettingsRepository> {
        SystemSettingsRepositoryImpl(datastoreProvider = get())
    }

    single<WeatherDataRepository> {
        WeatherDataRepositoryImpl(
            weatherDataStore = get(),
            sharedPrefsProvider = get(),
        )
    }
}
