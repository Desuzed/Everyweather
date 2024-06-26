package com.desuzed.everyweather.presentation.features.weather_main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.desuzed.everyweather.R
import com.desuzed.everyweather.data.repository.providers.action_result.WeatherActionResultProvider
import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.model.result.ActionType
import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.presentation.features.main_activity.MainActivity
import com.desuzed.everyweather.presentation.features.weather_main.ui.WeatherMainContent
import com.desuzed.everyweather.util.collect
import com.desuzed.everyweather.util.collectAsStateWithLifecycle
import com.desuzed.everyweather.util.navigate
import com.desuzed.everyweather.util.setArgumentObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

class WeatherMainFragment : Fragment() {
    private val viewModel by viewModel<WeatherMainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsStateWithLifecycle(WeatherState())
                WeatherMainContent(
                    state = state,
                    onUserInteraction = viewModel::onUserInteraction,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resolveArguments()
        collect((activity as MainActivity).getUserLatLngFlow(), ::onNewLocation)
        collect(viewModel.action, ::onNewAction)
    }

    private fun resolveArguments() {
        setArgumentObserver<String>(QUERY_KEY) {
            if (it.isNotBlank()) {
                getQueryForecast(it)
            }
        }
        setArgumentObserver<UserLatLng>(LAT_LNG_KEY) {
            viewModel.getForecast(it.toString(), it)
        }
    }

    private fun getQueryForecast(query: String) {
        viewModel.getForecast(query)
    }

    private fun onNewAction(action: WeatherMainAction) {
        when (action) {
            is WeatherMainAction.ShowSnackbar -> showSnackbar(action.queryResult)
            WeatherMainAction.NavigateToLocation -> navigate(R.id.action_weatherFragment_to_locationFragment)
            WeatherMainAction.NavigateToNextDaysWeather -> navigate(R.id.action_weatherFragment_to_nextDaysBottomSheet)
        }
    }

    private fun onNewLocation(location: UserLatLng?) {
        if (location != null) {
            getQueryForecast(location.toString())
        }
    }

    private fun showSnackbar(queryResult: QueryResult) {
        val provider = WeatherActionResultProvider(resources)
        val message = provider.parseCode(queryResult.code, queryResult.query)
        val onClick: () -> Unit
        val buttonTextId: Int
        when (queryResult.actionType) {
            ActionType.OK -> {
                onClick = {}
                buttonTextId = R.string.ok
            }

            ActionType.RETRY -> {
                buttonTextId = R.string.retry
                onClick = {
                    viewModel.onUserInteraction(WeatherUserInteraction.Refresh)
                }
            }
        }
        (activity as MainActivity).showSnackbar(
            message = message,
            actionStringId = buttonTextId,
            onActionClick = onClick
        )
    }

    companion object {
        const val QUERY_KEY = "QUERY"
        const val LAT_LNG_KEY = "LAT_LNG"
    }

}