package com.desuzed.everyweather.analytics

import android.content.Context
import com.desuzed.everyweather.presentation.features.map_point_selection.MapPointSelectionAction

class MapPointSelectionAnalytics(context: Context) : Analytics(context = context) {

    fun onAction(action: MapPointSelectionAction) {
        when (action) {
            MapPointSelectionAction.NewLocationConfirm -> logEvent(CONFIRM_MAP_LOCATION)
            else -> {}
        }
    }

    companion object {
        private const val CONFIRM_MAP_LOCATION = "map_loc_confirm"

    }
}