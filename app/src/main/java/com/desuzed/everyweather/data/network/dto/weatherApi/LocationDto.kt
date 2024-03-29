package com.desuzed.everyweather.data.network.dto.weatherApi

import com.google.gson.annotations.SerializedName

data class LocationDto(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("region")
    val region: String = "",
    @SerializedName("country")
    val country: String = "",
    @SerializedName("lat")
    val lat: Double = 0.0,
    @SerializedName("lon")
    val lon: Double = 0.0,
    @SerializedName("tz_id")
    val timeZone: String = "",
    @SerializedName("localtime_epoch")
    val localtimeEpoch: Long = 0,
    @SerializedName("localtime")
    val localtime: String = ""
)