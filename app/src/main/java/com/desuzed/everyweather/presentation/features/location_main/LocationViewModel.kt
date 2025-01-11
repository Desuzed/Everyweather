package com.desuzed.everyweather.presentation.features.location_main

import com.desuzed.everyweather.analytics.LocationMainAnalytics
import com.desuzed.everyweather.data.repository.providers.action_result.GeoActionResultProvider
import com.desuzed.everyweather.domain.interactor.LocationInteractor
import com.desuzed.everyweather.domain.interactor.SystemInteractor
import com.desuzed.everyweather.domain.model.location.FavoriteLocation
import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.model.location.geo.GeoData
import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider
import com.desuzed.everyweather.presentation.base.BaseViewModel
import com.desuzed.everyweather.util.Constants.EMPTY_STRING

class LocationViewModel(
    private val locationInteractor: LocationInteractor,
    private val analytics: LocationMainAnalytics,
    private val weatherDataRepository: WeatherDataRepository,
    private val systemInteractor: SystemInteractor,
) : BaseViewModel<LocationMainState, LocationMainEffect, LocationAction>(LocationMainState()) {

    init {
        collect(locationInteractor.getAllLocations(), ::onNewLocations)
    }

    override fun onAction(action: LocationAction) {
        analytics.onAction(action)
        when (action) {
            is LocationAction.DeleteFavoriteLocation -> deleteFavoriteLocation(
                action.favoriteLocationDto
            )

            is LocationAction.ConfirmFoundLocation -> onConfirmLocation(action.geo)
            is LocationAction.FavoriteLocationClick -> onFavoriteLocation(action.favoriteLocationDto)
            LocationAction.Redirection -> redirectToLocationApiPage()
            LocationAction.FindByQuery -> findTypedLocation()
            is LocationAction.NavigateToMapSelection -> navigateToMapSelectionScreen()
            LocationAction.MyLocation -> onMyLocationClick()
            LocationAction.Settings -> setSideEffect(LocationMainEffect.NavigateToSettings)
            LocationAction.OnBackClick -> navigateBack()
            LocationAction.RequestLocationPermissions -> onRequestPermissions()
            is LocationAction.UpdateFavoriteLocation -> updateFavoriteLocation(action.favoriteLocationDto)
            is LocationAction.ToggleEditFavoriteLocationDialog -> onToggle(action.item)
            is LocationAction.SetDefaultLocationName -> setDefaultLocationName(action.item)
            LocationAction.DismissDialog -> onDismissDialog()
            is LocationAction.EditLocationText -> onNewEditLocationText(action.input)
            is LocationAction.GeoInputQuery -> onNewGeoText(action.input)
            is LocationAction.ShowDeleteFavoriteLocation -> onShowDeleteFavoriteLocation(
                action.item
            )
        }
    }

    private fun launchRequireLocationPermissionsDialog() {
        setState { copy(locationDialog = LocationDialog.RequireLocationPermissions) }
    }

    private fun onMyLocationClick() {
        if (systemInteractor.arePermissionsGranted()) {
            setSideEffect(LocationMainEffect.FindUserLocation)
            navigateBack()
        } else {
            launchRequireLocationPermissionsDialog()
        }
    }

    private fun findTypedLocation() {
        launch {
            setState { copy(isLoading = true) }
            val resultGeo = locationInteractor.fetchGeocodingResultOrError(
                query = state.value.geoText,
            )
            setState {
                copy(
                    geoData = resultGeo.geoData,
                    isLoading = false,
                    locationDialog = LocationDialog.GeoPickerData,
                )
            }
            val resultAction = resultGeo.queryResult
            if (resultAction != null) {
                handleGeoError(resultAction)
            }
        }

    }

    private fun handleGeoError(queryResult: QueryResult) {
        if (shouldIgnoreError(queryResult.code)) {
            saveQueryAndNavigateBackToWeather(queryResult.query)
        } else {
            setSideEffect(LocationMainEffect.ShowSnackbar(queryResult))
        }
    }

    private fun onDismissDialog() {
        setState { copy(locationDialog = null) }
    }

    private fun onToggle(item: FavoriteLocation) {
        setState {
            copy(
                locationDialog = LocationDialog.EditLocation(item),
                editLocationText = item.customName.ifEmpty { item.cityName },
            )
        }
    }

    private fun navigateBack() {
        setSideEffect(LocationMainEffect.NavigateBack)
    }

    private fun onRequestPermissions() {
        setSideEffect(LocationMainEffect.RequestLocationPermissions)
        onDismissDialog()
    }

    private fun onConfirmLocation(geoData: GeoData) {
        setState { copy(geoText = EMPTY_STRING, geoData = null) }
        saveQueryAndNavigateBackToWeather("${geoData.lat},${geoData.lon}")
    }

    private fun deleteFavoriteLocation(favoriteLocationDto: FavoriteLocation) =
        launch {
            onDismissDialog()
            val deleted = locationInteractor.deleteFavoriteLocation(favoriteLocationDto)
            if (deleted) {
                onSuccess(ActionResultProvider.DELETED)
            } else {
                onError(ActionResultProvider.FAIL)
            }
        }

    private fun updateFavoriteLocation(favoriteLocationDto: FavoriteLocation) {
        launch {
            onDismissDialog()
            val inputText = state.value.editLocationText
            val locationToSave = favoriteLocationDto.copy(customName = inputText)
            val updated = locationInteractor.updateLocation(locationToSave)
            if (updated) {
                onSuccess(ActionResultProvider.UPDATED)
            } else {
                onError(ActionResultProvider.FAIL)
            }
        }
    }

    private fun setDefaultLocationName(favoriteLocationDto: FavoriteLocation) {
        launch {
            onDismissDialog()
            val locationToSave = favoriteLocationDto.copy(customName = favoriteLocationDto.cityName)
            val updated = locationInteractor.updateLocation(locationToSave)
            if (updated) {
                onSuccess(ActionResultProvider.UPDATED)
            } else {
                onError(ActionResultProvider.FAIL)
            }
        }
    }

    private fun onSuccess(code: Int) {
        setSideEffect(LocationMainEffect.ShowSnackbar(QueryResult(code)))
    }

    private fun onError(code: Int) {
        setSideEffect(LocationMainEffect.ShowSnackbar(QueryResult(code)))
    }

    private fun onNewLocations(locationsList: List<FavoriteLocation>) {
        setState { copy(locations = locationsList) }
    }

    private fun onFavoriteLocation(location: FavoriteLocation) {
        saveQueryAndNavigateBackToWeather(location.toQuery())
    }

    private fun redirectToLocationApiPage() {
        setSideEffect(
            LocationMainEffect.ShowSnackbar(
                QueryResult(ActionResultProvider.REDIRECTION)
            )
        )
    }

    /**
     * Нужен для того, чтобы игнорить ошибки Geo API и подсовывать query в Weather Api
     * */
    private fun shouldIgnoreError(code: Int): Boolean =
        code == GeoActionResultProvider.RATE_LIMIT
                || code == GeoActionResultProvider.ACCESS_RESTRICTED
                || code == GeoActionResultProvider.INVALID_TOKEN


    private fun onShowDeleteFavoriteLocation(item: FavoriteLocation) {
        setState { copy(locationDialog = LocationDialog.DeleteLocation(item)) }
    }

    private fun saveQueryAndNavigateBackToWeather(query: String, userLatLng: UserLatLng? = null) {
        launch {
            weatherDataRepository.saveQuery(
                query = query,
                shouldTriggerWeatherRequest = true,
                userLatLng = userLatLng,
            )
            navigateBack()
        }
    }


    private fun navigateToMapSelectionScreen() {
        setSideEffect(LocationMainEffect.NavigateToMapSelection)
    }

    private fun onNewGeoText(text: String) {
        setState { copy(geoText = text) }
    }

    private fun onNewEditLocationText(text: String) {
        setState { copy(editLocationText = text) }
    }

}