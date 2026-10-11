package com.desuzed.everyweather.domain.model.location

import android.os.Parcelable
//import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

//@Parcelize
@Serializable
data class UserLatLng(val lat: Double, val lon: Double, val time: Long) /*: Parcelable*/ {

    override fun toString(): String {
        return "$lat, $lon"
    }

}