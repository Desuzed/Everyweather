package com.desuzed.everyweather.data.repository.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.desuzed.everyweather.domain.repository.local.BaseDatastoreApiProvider
import com.desuzed.everyweather.util.Constants
import kotlinx.coroutines.flow.Flow

class WeatherDataStore(private val context: Context) : BaseDatastoreApiProvider() {
    private val Context.dataStore by preferencesDataStore(name = WEATHER_DATA)

    override val dataStore: DataStore<Preferences>
        get() = context.dataStore

    suspend fun setQuery(query: String) =
        edit { preferences ->
            preferences[KEY_QUERY] = query
        }

    suspend fun updateQuery(transform: (String) -> String) =
        edit { preferences ->
            preferences[KEY_QUERY]?.let { query ->
                preferences[KEY_QUERY] = transform(query)
            }
        }

    fun getQuery(): Flow<String> = getFlowOf(KEY_QUERY, Constants.EMPTY_STRING)

    suspend fun setForecastData(weatherData: String) =
        edit { preferences ->
            preferences[KEY_WEATHER_DATA] = weatherData
        }

    fun getForecastData(): Flow<String> = getFlowOf(KEY_WEATHER_DATA, Constants.EMPTY_STRING)

    companion object {
        private const val WEATHER_DATA = "weather_data"
        private val KEY_QUERY = stringPreferencesKey("query")
        private val KEY_WEATHER_DATA = stringPreferencesKey("weather_data")
    }
}
