package com.desuzed.clocknweather.retrofit.pojo

import com.google.gson.annotations.SerializedName

class Astro {
    @SerializedName("sunrise")
    var sunrise: String? = null

    @SerializedName("sunset")
    var sunset: String? = null

    @SerializedName("moonrise")
    var moonrise: String? = null

    @SerializedName("moonset")
    var moonset: String? = null

    @SerializedName("moon_phase")
    var moonPhase: String? = null

    @SerializedName("moon_illumination")
    var moonIllumination: String? = null
}