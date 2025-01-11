package com.desuzed.everyweather.presentation.features.location_main.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.location.geo.GeoData
import com.desuzed.everyweather.presentation.features.location_main.LocationAction
import com.desuzed.everyweather.presentation.features.location_main.LocationDialog
import com.desuzed.everyweather.ui.elements.AppAlertDialog
import com.desuzed.everyweather.ui.elements.AppDialog

@Composable
fun LocationDialogContent(
    dialog: LocationDialog?,
    geoData: List<GeoData>?,
    editLocationText: String,
    onAction: (LocationAction) -> Unit,
) {
    dialog?.let {
        when (dialog) {
            is LocationDialog.EditLocation -> {
                EditLocationDialogContent(
                    editLocationText = editLocationText,
                    onNewEditLocationText = {
                        onAction(LocationAction.EditLocationText(it))
                    },
                    location = dialog.favoriteLocation,
                    onAction = onAction,
                )
            }

            LocationDialog.GeoPickerData -> {
                geoData?.let {
                    AppDialog(
                        modifier = Modifier.fillMaxHeight(fraction = 0.9f),
                        onDismiss = {
                            onAction(LocationAction.DismissDialog)
                        }
                    ) {
                        GeoLocationsPickerContent(geoData, onAction)
                    }
                }
            }

            LocationDialog.RequireLocationPermissions -> {
                RequirePermissionsDialogContent(onAction)
            }

            is LocationDialog.DeleteLocation -> {
                AppAlertDialog(title = stringResource(id = R.string.delete),
                    onPositiveButtonClick = {
                        onAction(
                            LocationAction.DeleteFavoriteLocation(dialog.favoriteLocation)
                        )
                    }, onDismiss = {
                        onAction(LocationAction.DismissDialog)
                    }
                )
            }
        }
    }
}