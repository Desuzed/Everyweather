package com.desuzed.everyweather.di

import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.presentation.features.in_app_update.InAppUpdateViewModel
import com.desuzed.everyweather.presentation.features.location_main.LocationViewModel
import com.desuzed.everyweather.presentation.features.main_activity.MainActivityViewModel
import com.desuzed.everyweather.presentation.features.map_point_selection.MapPointSelectionViewModel
import com.desuzed.everyweather.presentation.features.settings.SettingsViewModel
import com.desuzed.everyweather.presentation.features.weather_main.WeatherMainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainActivityViewModel(
            systemSettingsInteractor = get(),
            systemInteractor = get(),
            weatherDataRepository = get(),
            appUpdateProvider = get(),
        )
    }
    viewModel {
        LocationViewModel(
            locationInteractor = get(),
            analytics = get(),
            weatherDataRepository = get(),
            systemInteractor = get(),
        )
    }
    viewModel {
        WeatherMainViewModel(
            weatherInteractor = get(),
            weatherSettingsInteractor = get(),
            systemSettingsRepository = get(),
            locationInteractor = get(),
            analytics = get(),
            weatherDataRepository = get(),
        )
    }

    viewModel {
        SettingsViewModel(
            weatherSerringsInteractor = get(),
            systemSettingsInteractor = get(),
            analytics = get(),
            appUpdateProvider = get(),
        )
    }

    viewModel { (status: InAppUpdateStatus) ->
        InAppUpdateViewModel(
            status = status,
            analytics = get(),
            appUpdateProvider = get(),
        )
    }

    viewModel {
        MapPointSelectionViewModel(
            weatherDataRepository = get(),
            analytics = get(),
        )
    }
}