package com.desuzed.everyweather.presentation.features.location_main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.location.FavoriteLocation
import com.desuzed.everyweather.presentation.features.location_main.LocationAction
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.elements.RoundedButton
import com.desuzed.everyweather.ui.extensions.bottomEdgeToEdgeImePadding
import com.desuzed.everyweather.ui.extensions.bottomEdgeToEdgePadding
import com.desuzed.everyweather.ui.extensions.topEdgeToEdgePadding
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.Constants.EMPTY_STRING
import com.desuzed.everyweather.util.Constants.ONE_FLOAT
import com.desuzed.everyweather.util.MockWeatherObject

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        LocationMainPageContent(
            locations = MockWeatherObject.locations,
            isLoading = false,
            geoText = EMPTY_STRING,
            onAction = {},
            onGeoTextChanged = {},
            onShowDeleteDialog = {},
        )
    }
}

@Composable
fun LocationMainPageContent(
    locations: List<FavoriteLocation>,
    isLoading: Boolean,
    geoText: String,
    onGeoTextChanged: (text: String) -> Unit,
    onAction: (LocationAction) -> Unit,
    onShowDeleteDialog: (FavoriteLocation) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.dimen_10))
    ) {
        LocationToolbar(
            modifier = Modifier.topEdgeToEdgePadding(),
            isLoading = isLoading,
            geoText = geoText,
            onGeoTextChanged = onGeoTextChanged,
            onAction = onAction,
        )
        LazyColumn(
            modifier = Modifier
                .weight(ONE_FLOAT)
                .padding(
                    horizontal = dimensionResource(id = R.dimen.dimen_10),
                    vertical = dimensionResource(id = R.dimen.dimen_10)
                )
        ) {
            items(items = locations, key = { it.latLon }) { locationItem ->
                LocationItemContent(
                    item = locationItem,
                    onClick = {
                        onAction(
                            LocationAction.FavoriteLocationClick(
                                locationItem
                            )
                        )
                    },
                    onLongClick = { onShowDeleteDialog(it) },
                    onEditClick = {
                        onAction(
                            LocationAction.ToggleEditFavoriteLocationDialog(it)
                        )
                    }
                )
            }
        }
        RoundedButton(
            modifier = Modifier
                .fillMaxWidth()
                .bottomEdgeToEdgeImePadding()
                .bottomEdgeToEdgePadding()
                .padding(horizontal = dimensionResource(id = R.dimen.dimen_10)),
            onClick = { onAction(LocationAction.NavigateToMapSelection) },
            text = stringResource(id = R.string.find_on_map)
        )
    }
}