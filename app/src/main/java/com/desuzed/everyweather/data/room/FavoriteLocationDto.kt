package com.desuzed.everyweather.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.desuzed.everyweather.domain.model.weather.Location

@Entity(tableName = "favorite_location_table")
data class FavoriteLocationDto(
    /**
     * latLon is made to be unique key by concatenating latitude and longitude values
     */
    @PrimaryKey @ColumnInfo(name = "lat_lon") val latLon: String,
    @ColumnInfo(name = "name") val cityName: String,
    @ColumnInfo(name = "region") val region: String,
    @ColumnInfo(name = "country") val country: String,
    @ColumnInfo(name = "lat") val lat: String,
    @ColumnInfo(name = "lon") val lon: String,
    @ColumnInfo(name = "custom_name", defaultValue = "") val customName: String = "",
) {
    override fun toString(): String {
        return if (region.isNotEmpty()) {
            "$region, $country"
        } else country
    }

    companion object {

        fun buildFavoriteLocationObj(
            location: Location,
            latLon: String,
        ): FavoriteLocationDto {
            return FavoriteLocationDto(
                latLon = latLon,
                cityName = location.name,
                region = location.region,
                country = location.country,
                lat = location.lat.toString(),
                lon = location.lon.toString(),
            )
        }
    }
}
