package com.desuzed.everyweather.presentation.features.location_main.ui

import androidx.compose.runtime.Composable
import com.desuzed.everyweather.presentation.features.location_main.LocationAction
import com.desuzed.everyweather.presentation.features.location_main.LocationMainState

@Composable
fun LocationMain(
    state: LocationMainState,
    onAction: (LocationAction) -> Unit,
) {
    LocationMainBody(
        locations = state.locations,
        isLoading = state.isLoading,
        geoText = state.geoText,
        onAction = onAction,
    )
    LocationDialogContent(
        dialog = state.locationDialog,
        geoData = state.geoData,
        editLocationText = state.editLocationText,
        onAction = onAction,
    )
}
