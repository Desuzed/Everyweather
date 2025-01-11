package com.desuzed.everyweather.analytics

import android.content.Context
import com.desuzed.everyweather.presentation.features.location_main.LocationAction

class LocationMainAnalytics(context: Context) : Analytics(context = context) {

    fun onAction(action: LocationAction) {
        when (action) {
            is LocationAction.ConfirmFoundLocation -> logEvent(CONFIRM_FOUND_LOCATION)
            is LocationAction.DeleteFavoriteLocation -> logEvent(DELETE_LOCATION)
            is LocationAction.UpdateFavoriteLocation -> logEvent(UPDATE_LOCATION)
            is LocationAction.SetDefaultLocationName -> logEvent(SET_DEFAULT_LOCATION)
            is LocationAction.FavoriteLocationClick -> logEvent(FAVORITE_LOCATION)
            LocationAction.FindByQuery -> logEvent(FIND_BY_QUERY)
            is LocationAction.NavigateToMapSelection -> logEvent(NAVIGATE_TO_MAP)
            LocationAction.MyLocation -> logEvent(MY_LOCATION)
            LocationAction.Settings -> logEvent(NAVIGATE_TO_SETTINGS)
            else -> {}
        }
    }

    companion object {
        private const val FIND_BY_QUERY = "loc_main_find_by_query"
        private const val CONFIRM_FOUND_LOCATION = "loc_main_confirm_found_location"
        private const val MY_LOCATION = "loc_main_my_location"
        private const val DELETE_LOCATION = "loc_main_delete_location"
        private const val UPDATE_LOCATION = "loc_main_update_location"
        private const val SET_DEFAULT_LOCATION = "loc_main_set_default_location"
        private const val FAVORITE_LOCATION = "loc_main_favorite"
        private const val NAVIGATE_TO_MAP = "loc_main_nav_to_map"
        private const val NAVIGATE_TO_SETTINGS = "loc_main_nav_to_settings"
    }
}