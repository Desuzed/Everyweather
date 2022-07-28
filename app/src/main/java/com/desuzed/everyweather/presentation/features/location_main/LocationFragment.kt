package com.desuzed.everyweather.presentation.features.location_main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.desuzed.everyweather.App
import com.desuzed.everyweather.R
import com.desuzed.everyweather.presentation.base.AppViewModelFactory
import com.desuzed.everyweather.presentation.features.main_activity.MainActivity
import com.desuzed.everyweather.util.addOnBackPressedCallback
import com.desuzed.everyweather.util.collect
import com.desuzed.everyweather.util.navigate
import com.desuzed.everyweather.util.toast
import com.desuzed.everyweather.presentation.features.weather_main.WeatherMainFragment


class LocationFragment : Fragment() {
    private val viewModel: LocationViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            AppViewModelFactory(App.instance.getRepo())
        )
            .get(LocationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsState()
                LocationMainContent(
                    state = state,
                    onUserInteraction = viewModel::onUserInteraction,
                    onGeoTextChanged = viewModel::onNewGeoText,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addOnBackPressedCallback()
        collect(viewModel.action, ::onNewAction)
    }

    private fun onNewAction(action: LocationMainAction) {
        when (action) {
            is LocationMainAction.ShowToast -> toast(action.message)//todo избавиться от тостов
            is LocationMainAction.NavigateToWeather -> navigateToWeatherFragment(bundleOf(action.key to action.query))
            LocationMainAction.MyLocation -> onMyLocationClick()
            LocationMainAction.ShowMapFragment -> showMapBotSheet()
        }
    }

    private fun onMyLocationClick() {
        (activity as MainActivity).locationHandler.findUserLocation()
        val bundle = bundleOf(WeatherMainFragment.USER_LOCATION to true)
        navigateToWeatherFragment(bundle)
    }

    private fun showMapBotSheet() {
        navigate(R.id.action_locationFragment_to_mapBottomSheetFragment)
    }

    private fun navigateToWeatherFragment(bundle: Bundle) {
        navigate(R.id.action_locationFragment_to_weatherFragment, bundle)
    }

}