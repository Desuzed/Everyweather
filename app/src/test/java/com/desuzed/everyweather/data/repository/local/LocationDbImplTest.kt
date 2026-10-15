package com.desuzed.everyweather.data.repository.local

import com.desuzed.everyweather.data.mapper.location.FavoriteLocationMapper
import com.desuzed.everyweather.data.room.FavoriteLocationDAO
import com.desuzed.everyweather.data.room.FavoriteLocationDto
import com.desuzed.everyweather.domain.model.weather.Location
import com.desuzed.everyweather.util.LatLngKeyGeneratorImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationDbImplTest {

    @Test
    fun `insert maps a location to a reusable latitude longitude weather query`() = runBlocking {
        val dao = InMemoryFavoriteLocationDao()
        val subject = LocationDbImpl(
            favoriteLocationDAO = dao,
            favoriteLocationMapper = FavoriteLocationMapper(),
            latLngKeyGenerator = LatLngKeyGeneratorImpl(),
            dispatcher = Dispatchers.Unconfined,
        )
        val location = Location(
            name = "Moscow",
            region = "Moscow",
            country = "Russia",
            lat = 55.7558,
            lon = 37.6173,
            timezone = "Europe/Moscow",
            localtimeEpoch = 0,
        )

        assertTrue(subject.insert(location))

        val saved = subject.getAllLocations().first().single()
        assertEquals("55.7558, 37.6173", saved.toQuery())
        assertEquals("55.8,37.7", saved.latLon)
    }

    private class InMemoryFavoriteLocationDao : FavoriteLocationDAO {
        private val locations = MutableStateFlow<List<FavoriteLocationDto>>(emptyList())

        override fun getAlphabetizedLocations(): Flow<List<FavoriteLocationDto>> = locations

        override suspend fun insert(favoriteLocationDto: FavoriteLocationDto): Long {
            if (locations.value.any { it.latLon == favoriteLocationDto.latLon }) return -1
            locations.value = (locations.value + favoriteLocationDto).sortedBy { it.cityName }
            return 1
        }

        override suspend fun deleteItem(favoriteLocationDto: FavoriteLocationDto): Int {
            val updated = locations.value.filterNot { it.latLon == favoriteLocationDto.latLon }
            val count = locations.value.size - updated.size
            locations.value = updated
            return count
        }

        override suspend fun updateLocation(favoriteLocationDto: FavoriteLocationDto): Int {
            if (locations.value.none { it.latLon == favoriteLocationDto.latLon }) return 0
            locations.value = locations.value.map {
                if (it.latLon == favoriteLocationDto.latLon) favoriteLocationDto else it
            }
            return 1
        }

        override suspend fun deleteAll() {
            locations.value = emptyList()
        }

        override suspend fun containsPrimaryKey(latLon: String): Boolean =
            locations.value.any { it.latLon == latLon }
    }
}
