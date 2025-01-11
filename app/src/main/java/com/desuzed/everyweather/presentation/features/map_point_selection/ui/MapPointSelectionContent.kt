package com.desuzed.everyweather.presentation.features.map_point_selection.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.model.weather.Location
import com.desuzed.everyweather.presentation.features.map_point_selection.MapPointSelectionAction
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.elements.RegularText
import com.desuzed.everyweather.ui.extensions.bottomEdgeToEdgePadding
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.Constants.EMPTY_STRING
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private const val DEFAULT_ZOOM = 11f

@Composable
fun ColumnScope.MapPointSelectionContent(
    defaultSelectedLocation: Location?,
    newPickedLocation: UserLatLng?,
    loadNewLocationWeather: Boolean,
    isMapInitialized: Boolean,
    onAction: (MapPointSelectionAction) -> Unit,
) {
    val oldMarker = remember(key1 = defaultSelectedLocation) {
        if (defaultSelectedLocation != null) {
            LatLng(defaultSelectedLocation.lat, defaultSelectedLocation.lon)
        } else null
    }
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        if (oldMarker != null) {
            position = CameraPosition.fromLatLngZoom(oldMarker, DEFAULT_ZOOM)
        }
    }
    LaunchedEffect(isMapInitialized) {
        if (isMapInitialized && oldMarker != null) {
            cameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(oldMarker, DEFAULT_ZOOM)
                )
            )
        }
    }
    RegularText(
        text = stringResource(id = R.string.set_place_on_map),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = dimensionResource(id = R.dimen.dimen_10)),
        textAlign = TextAlign.Start,
    )
    Box(contentAlignment = Alignment.Center) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .bottomEdgeToEdgePadding()
                .padding(top = dimensionResource(id = R.dimen.dimen_10)),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                onAction(
                    MapPointSelectionAction.NewLocationPicked(
                        location = UserLatLng(
                            lat = it.latitude,
                            lon = it.longitude,
                            time = System.currentTimeMillis(),
                        )
                    )
                )
            },
            onMapLoaded = { onAction(MapPointSelectionAction.OnMapInitialized) },
        ) {
            if (oldMarker != null) {
                Marker(
                    state = rememberMarkerState(position = oldMarker),
                    title = defaultSelectedLocation?.name ?: EMPTY_STRING,
                )
            }
            if (newPickedLocation != null && loadNewLocationWeather) {
                Marker(
                    state = rememberMarkerState(
                        position = LatLng(
                            newPickedLocation.lat,
                            newPickedLocation.lon,
                        )
                    ),
                )
            }
        }
        if (!isMapInitialized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EveryweatherTheme.colors.primaryBackground.first()),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = EveryweatherTheme.colors.primary)
            }
        }
    }
}

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        Column(modifier = Modifier.background(EveryweatherTheme.colors.primaryBackground.first())) {
            MapPointSelectionContent(
                defaultSelectedLocation = null,
                newPickedLocation = null,
                loadNewLocationWeather = false,
                isMapInitialized = false,
                onAction = {},
            )
        }
    }
}