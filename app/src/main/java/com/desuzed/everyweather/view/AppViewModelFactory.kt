package com.desuzed.everyweather.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.desuzed.everyweather.data.repository.RepositoryApp
import com.desuzed.everyweather.view.fragments.location.LocationViewModel
import com.desuzed.everyweather.view.fragments.weather.WeatherViewModel
import com.desuzed.everyweather.view.fragments.weather.main.WeatherMainViewModel
import com.desuzed.everyweather.view.fragments.weather.next_days.NextDaysViewModel

/**
 * Factory for creating ViewModels with parameters
 */
class AppViewModelFactory(private val repositoryApp: RepositoryApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainActivityViewModel::class.java) -> {
                MainActivityViewModel(repositoryApp) as T
            }
            modelClass.isAssignableFrom(WeatherViewModel::class.java) -> {
                WeatherViewModel(repositoryApp) as T
            }
            modelClass.isAssignableFrom(LocationViewModel::class.java) -> {
                LocationViewModel(repositoryApp) as T
            }
            modelClass.isAssignableFrom(WeatherMainViewModel::class.java) -> {
                WeatherMainViewModel(repositoryApp) as T
            }
            modelClass.isAssignableFrom(NextDaysViewModel::class.java) -> {
                NextDaysViewModel(repositoryApp) as T
            }
            else -> {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}
