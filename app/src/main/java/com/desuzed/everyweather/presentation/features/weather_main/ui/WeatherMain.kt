package com.desuzed.everyweather.presentation.features.weather_main.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.desuzed.everyweather.presentation.features.weather_main.WeatherAction
import com.desuzed.everyweather.presentation.features.weather_main.WeatherState
import com.desuzed.everyweather.presentation.features.weather_next_days.ui.NextDaysWeatherBottomSheet
import com.desuzed.everyweather.presentation.ui.UiMapper
import com.desuzed.everyweather.presentation.ui.main.WeatherMainUi
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherMain(
    // navBackStackEntry: NavBackStackEntry,
    state: WeatherState,
    onAction: (WeatherAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mappedWeatherUi = remember { mutableStateOf<WeatherMainUi?>(null) }
    var selectedDayItem by remember {
        mutableStateOf<NextDaysUi?>(null)
    }

    val nextDaysUiList = remember { mutableStateOf<List<NextDaysUi>?>(null) }
    LaunchedEffect(key1 = state.weatherData) {
        coroutineScope.launch {
            nextDaysUiList.value = state.weatherData?.let {
                UiMapper(
                    context = context,
                    selectedDistanceDimen = state.windSpeed,
                    selectedTemperature = state.temperature,
                    selectedLanguage = state.selectedLang,
                    selectedPressure = state.pressure,
                ).mapToNextDaysUi(it)
            }
        }
    }

    LaunchedEffect(key1 = state.weatherData) {
        coroutineScope.launch {
            mappedWeatherUi.value = withContext(Dispatchers.Default) {
                state.weatherData?.let {
                    UiMapper(
                        context = context,
                        selectedLanguage = state.selectedLang,
                        selectedDistanceDimen = state.windSpeed,
                        selectedTemperature = state.temperature,
                        selectedPressure = state.pressure,
                    ).mapToMainWeatherUi(it)
                }
            }
        }
    }

    WeatherMainBody(
        weatherData = mappedWeatherUi,
        nextDaysUiList = nextDaysUiList,
        isLoading = state.isLoading,
        isAddButtonEnabled = state.isAddButtonEnabled,
        onAction = onAction,
        onNextDayClick = {
            //todo переместить в эффект
            coroutineScope.launch {
                selectedDayItem = it
                sheetState.show()
            }
        },
    )
    NextDaysWeatherBottomSheet(
        sheetState = sheetState,
        dayItem = selectedDayItem,
        onBackClick = {
            //todo переместить в эффект
            coroutineScope.launch {
                sheetState.hide()
                selectedDayItem = null
            }
        }
    )
}