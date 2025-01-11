package com.desuzed.everyweather.presentation.features.map_point_selection.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.desuzed.everyweather.R
import com.desuzed.everyweather.presentation.features.map_point_selection.MapPointSelectionAction
import com.desuzed.everyweather.presentation.features.map_point_selection.MapPointSelectionState
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.elements.AppAlertDialog
import com.desuzed.everyweather.ui.elements.AppToolbar
import com.desuzed.everyweather.ui.elements.GradientBox
import com.desuzed.everyweather.ui.extensions.topEdgeToEdgePadding
import com.desuzed.everyweather.ui.theming.EveryweatherTheme

@Composable
fun MapPointSelectionScreenContent(
    state: MapPointSelectionState,
    onAction: (MapPointSelectionAction) -> Unit,
) {
    GradientBox(
        modifier = Modifier.fillMaxSize(),
        colors = EveryweatherTheme.colors.primaryBackground,
    ) {
        Column(modifier = Modifier.topEdgeToEdgePadding()) {
            AppToolbar(
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.dimen_10)),
                title = stringResource(id = R.string.map_screen_title),
                onStartIconClick = { onAction(MapPointSelectionAction.OnBackClick) },
            )
            MapPointSelectionContent(
                defaultSelectedLocation = state.defaultMapPinLocation,
                newPickedLocation = state.newSelectedLocation,
                loadNewLocationWeather = state.shouldShowSelectedPin,
                isMapInitialized = state.isMapInitialized,
                onAction = onAction,
            )
        }
    }

    if (state.isConfirmDialogShown) {
        AppAlertDialog(
            title = stringResource(id = R.string.load_weather_of_this_location),
            onPositiveButtonClick = {
                onAction(MapPointSelectionAction.NewLocationConfirm)
            },
            onDismiss = {
                onAction(MapPointSelectionAction.DismissConfirmPinDialog)
            },
        )
    }
}

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        MapPointSelectionScreenContent(state = MapPointSelectionState(), onAction = {})
    }
}
