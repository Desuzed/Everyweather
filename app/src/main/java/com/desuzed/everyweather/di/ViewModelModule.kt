package com.desuzed.everyweather.di

import com.desuzed.everyweather.presentation.features.in_app_update.InAppUpdateViewModel
import com.desuzed.everyweather.presentation.features.location_main.LocationViewModel
import com.desuzed.everyweather.presentation.features.main_activity.MainActivityViewModel
import com.desuzed.everyweather.presentation.features.map_point_selection.MapPointSelectionViewModel
import com.desuzed.everyweather.presentation.features.settings.SettingsViewModel
import com.desuzed.everyweather.presentation.features.shared.SharedViewModel
import com.desuzed.everyweather.presentation.features.weather_main.WeatherMainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainActivityViewModel(
            systemSettingsInteractor = get(),
            systemInteractor = get(),
            weatherDataRepository = get(),
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

    viewModel {
        InAppUpdateViewModel(
            analytics = get(),
        )
    }

    viewModel {
        MapPointSelectionViewModel(
            weatherDataRepository = get(),
            analytics = get(),
        )
    }

    viewModel {
        SharedViewModel(
            appUpdateProvider = get(),
        )
    }
}