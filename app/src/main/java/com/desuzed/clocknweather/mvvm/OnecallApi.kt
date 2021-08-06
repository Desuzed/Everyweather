package com.desuzed.clocknweather.mvvm

import com.google.gson.annotations.SerializedName
import java.util.*

class OnecallApi {
    @SerializedName("lat")
    var lat = 0f

    @SerializedName("lon")
    var lon = 0f

    @SerializedName("timezone")
    var timezone: String? = null

    @SerializedName("id")
    var id = 0

    @SerializedName("timezone_offset")
    var timezone_offset: Int = 0

    @SerializedName("current")
    var current: Current? = null

    @SerializedName("hourly")
    var hourly: ArrayList<Hourly>? = null

    @SerializedName("daily")
    var daily: ArrayList<Daily>? = null

    class Weather {
        @SerializedName("id")
        var id = 0

        @SerializedName("main")
        var main: String? = null

        @SerializedName("description")
        var description: String? = null

        @SerializedName("icon")
        var icon: String? = null
    }


    class Current {
        @SerializedName("dt")
        var date: Long = 0

        @SerializedName("sunrise")
        var sunrise: Long = 0

        @SerializedName("sunset")
        var sunset: Long = 0

        @SerializedName("temp")
        var temp = 0f

        @SerializedName("feels_like")
        var feels_like = 0f

        @SerializedName("pressure")
        var pressure: Int = 0

        @SerializedName("humidity")
        var humidity: Int = 0

        @SerializedName("dew_point")
        var dew_point = 0f

        @SerializedName("uvi")
        var uvi = 0f

        @SerializedName("clouds")
        var clouds: Int = 0

        @SerializedName("visibility")
        var visibility: Int = 0

        @SerializedName("wind_speed")
        var wind_speed = 0f

        @SerializedName("wind_deg")
        var wind_deg: Int = 0

        @SerializedName("wind_gust")
        var wind_gust = 0f

        @SerializedName("weather")
        var weather: ArrayList<Weather>? = null
    }

    class Hourly {
        @SerializedName("dt")
        var date: Long = 0

        @SerializedName("temp")
        var temp = 0f

        @SerializedName("feels_like")
        var feels_like = 0f

        @SerializedName("pressure")
        var pressure: Int = 0

        @SerializedName("humidity")
        var humidity: Int = 0

        @SerializedName("dew_point")
        var dew_point = 0f

        @SerializedName("uvi")
        var uvi = 0f

        @SerializedName("clouds")
        var clouds: Int = 0

        @SerializedName("visibility")
        var visibility: Int = 0

        @SerializedName("wind_speed")
        var wind_speed = 0f

        @SerializedName("wind_deg")
        var wind_deg: Int = 0

        @SerializedName("wind_gust")
        var wind_gust = 0f

        @SerializedName("weather")
        var weather: ArrayList<Weather>? = null
    }

    class Daily {
        @SerializedName("dt")
        var date: Long = 0

        @SerializedName("sunrise")
        var sunrise: Long = 0

        @SerializedName("sunset")
        var sunset: Long = 0

        @SerializedName("moonrise")
        var moonrise: Long = 0

        @SerializedName("moonset")
        var moonset: Long = 0

        @SerializedName("temp")
        var temp: Temp? = null

        @SerializedName("feels_like")
        var feelsLike: FeelsLike? = null

        @SerializedName("pressure")
        var pressure: Int = 0

        @SerializedName("humidity")
        var humidity: Int = 0

        @SerializedName("dew_point")
        var dew_point = 0f

        @SerializedName("uvi")
        var uvi = 0f

        @SerializedName("clouds")
        var clouds: Int = 0

        @SerializedName("wind_speed")
        var wind_speed = 0f

        @SerializedName("wind_deg")
        var wind_deg: Int = 0

        @SerializedName("wind_gust")
        var wind_gust = 0f

        @SerializedName("weather")
        var weather: ArrayList<Weather>? = null

        @SerializedName("pop")
        var pop = 0f
    }

    class Temp {
        @SerializedName("day")
        var day = 0f

        @SerializedName("min")
        var min = 0f

        @SerializedName("max")
        var max = 0f

        @SerializedName("night")
        var night = 0f

        @SerializedName("eve")
        var eve = 0f

        @SerializedName("morn")
        var morn = 0f
    }

    class FeelsLike {
        @SerializedName("day")
        var day = 0f

        @SerializedName("night")
        var night = 0f

        @SerializedName("eve")
        var eve = 0f

        @SerializedName("morn")
        var morn = 0f
    }
}

