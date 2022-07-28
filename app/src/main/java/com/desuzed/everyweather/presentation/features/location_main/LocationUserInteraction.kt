package com.desuzed.everyweather.presentation.features.location_main

import com.desuzed.everyweather.data.room.FavoriteLocationDto

sealed interface LocationUserInteraction {
    object MyLocation : LocationUserInteraction
    class FavoriteLocation(val favoriteLocationDto: FavoriteLocationDto) : LocationUserInteraction
    class DeleteFavoriteLocation(val favoriteLocationDto: FavoriteLocationDto) :
        LocationUserInteraction

    object FindOnMap : LocationUserInteraction
    object FindByQuery : LocationUserInteraction
}