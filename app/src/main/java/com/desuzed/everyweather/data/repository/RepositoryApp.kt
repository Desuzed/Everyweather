package com.desuzed.everyweather.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import com.desuzed.everyweather.data.network.ActionResultProvider
import com.desuzed.everyweather.data.network.dto.weatherApi.ApiErrorMapper
import com.desuzed.everyweather.data.network.dto.weatherApi.ErrorDtoWeatherApi
import com.desuzed.everyweather.data.network.dto.weatherApi.WeatherResponseDto
import com.desuzed.everyweather.data.network.dto.weatherApi.WeatherResponseMapper
import com.desuzed.everyweather.data.network.retrofit.NetworkResponse
import com.desuzed.everyweather.data.repository.local.ContextProvider
import com.desuzed.everyweather.data.repository.local.LocalDataSource
import com.desuzed.everyweather.data.repository.local.RoomProvider
import com.desuzed.everyweather.data.room.FavoriteLocationDto
import com.desuzed.everyweather.model.NetworkLiveData
import com.desuzed.everyweather.model.entity.WeatherResponse
import com.desuzed.everyweather.view.ui.next_days.NextDaysUi
import com.desuzed.everyweather.view.ui.main.WeatherMainUi
import kotlinx.coroutines.flow.Flow

class RepositoryAppImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : RepositoryApp {
    //RoomProvider
    override suspend fun insert(favoriteLocationDto: FavoriteLocationDto): Boolean =
        localDataSource.provideRoom().insert(favoriteLocationDto)

    override suspend fun deleteItem(favoriteLocationDto: FavoriteLocationDto): Boolean =
        localDataSource.provideRoom().deleteItem(favoriteLocationDto)


    override suspend fun containsPrimaryKey(latLon: String): Boolean =
        localDataSource.provideRoom().containsPrimaryKey(latLon)

    override fun getAllLocations(): LiveData<List<FavoriteLocationDto>> =
        localDataSource.provideRoom().getAllLocations()

    //ContextProvider
    override fun saveForecastToCache(weatherResponse: WeatherResponse) =
        localDataSource.provideSPref().saveForecastToCache(weatherResponse)

    override fun loadForecastFromCache(): WeatherResponse? =
        localDataSource.provideSPref().loadForecastFromCache()

    override fun saveQuery(query: String) = localDataSource.provideSPref().saveQuery(query)

    override fun loadQuery(): String = localDataSource.provideSPref().loadQuery()

    override fun parseCode(errorCode: Int): String =
        localDataSource.provideSPref().parseCode(errorCode)

    override suspend fun mapToNextDaysUi(response: WeatherResponse): List<NextDaysUi> {
        return localDataSource.provideSPref().mapToNextDaysUi(response)
    }

    override suspend fun mapToMainWeatherUi(response: WeatherResponse): WeatherMainUi =
        localDataSource.provideSPref().mapToMainWeatherUi(response)

    override suspend fun getForecast(query: String): NetworkResponse<WeatherResponseDto, ErrorDtoWeatherApi> {
        saveQuery(query)
        return remoteDataSource.getForecast(query)
    }

    override fun getNetworkConnection(): Flow<Boolean> =
        localDataSource.getNetworkLiveData().asFlow()

    //todo вынести в какой нибудь юзкейс и избавиться от репозиторий слоя
    override suspend fun fetchForecastOrErrorMessage(query: String): ResultForecast {
        if (query.isEmpty()) {
            return ResultForecast(null, parseCode(ActionResultProvider.NO_DATA))
        }
        return when (val response = getForecast(query)) {
            is NetworkResponse.Success -> {
                val weatherResponse = WeatherResponseMapper.mapFromEntity(response.body)
                saveForecastToCache(weatherResponse)
                ResultForecast(weatherResponse, null)
            }
            is NetworkResponse.ApiError -> {
                val apiError = ApiErrorMapper().mapFromEntity(response.body)
                ResultForecast(loadForecastFromCache(), parseCode(apiError.error.code))
            }
            is NetworkResponse.NetworkError -> ResultForecast(
                loadForecastFromCache(),
                parseCode(ActionResultProvider.NO_INTERNET)
            )
            is NetworkResponse.UnknownError -> ResultForecast(
                loadForecastFromCache(),
                parseCode(ActionResultProvider.UNKNOWN)
            )
        }
    }

}

interface RepositoryApp : RoomProvider, ContextProvider, RemoteDataSource {
    fun getNetworkConnection(): Flow<Boolean>
    suspend fun fetchForecastOrErrorMessage(query: String): ResultForecast
}