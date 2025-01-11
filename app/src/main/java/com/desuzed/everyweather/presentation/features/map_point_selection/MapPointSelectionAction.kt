package com.desuzed.everyweather.presentation.features.map_point_selection

import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.presentation.base.Action

sealed interface MapPointSelectionAction : Action {
    data object NewLocationConfirm : MapPointSelectionAction
    data object DismissConfirmPinDialog : MapPointSelectionAction
    data object OnBackClick : MapPointSelectionAction
    data object OnMapInitialized : MapPointSelectionAction
    class NewLocationPicked(val location: UserLatLng) : MapPointSelectionAction
}