package com.desuzed.everyweather.presentation.features.location_main

import com.desuzed.everyweather.domain.model.location.FavoriteLocation

sealed interface LocationDialog {
    data object GeoPickerData : LocationDialog
    data object RequireLocationPermissions : LocationDialog
    class DeleteLocation(val favoriteLocation: FavoriteLocation) : LocationDialog
    class EditLocation(val favoriteLocation: FavoriteLocation) : LocationDialog
}