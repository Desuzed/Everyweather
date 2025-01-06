package com.desuzed.everyweather.presentation.features.location_main

import com.desuzed.everyweather.analytics.LocationMainAnalytics
import com.desuzed.everyweather.data.repository.providers.action_result.GeoActionResultProvider
import com.desuzed.everyweather.domain.interactor.LocationInteractor
import com.desuzed.everyweather.domain.interactor.SystemInteractor
import com.desuzed.everyweather.domain.model.location.FavoriteLocation
import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.model.location.geo.GeoData
import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.domain.repository.local.SharedPrefsProvider
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider
import com.desuzed.everyweather.presentation.base.BaseViewModel
import com.desuzed.everyweather.util.Constants.EMPTY_STRING
import kotlinx.coroutines.delay

class LocationViewModel(
    private val locationInteractor: LocationInteractor,
    private val analytics: LocationMainAnalytics,
    private val sharedPrefsProvider: SharedPrefsProvider,
    private val weatherDataRepository: WeatherDataRepository,
    private val systemInteractor: SystemInteractor,
) : BaseViewModel<LocationMainState, LocationMainEffect, LocationAction>(LocationMainState()) {

    init {
        collect(locationInteractor.getAllLocations(), ::onNewLocations)
        initMapPin()
    }

    override fun onAction(action: LocationAction) {
        analytics.onAction(action)
        when (action) {
            is LocationAction.DeleteFavoriteLocation -> deleteFavoriteLocation(
                action.favoriteLocationDto
            )

            is LocationAction.ConfirmFoundLocation -> onConfirmLocation(action.geo)
            is LocationAction.FavoriteLocationClick -> onFavoriteLocation(action.favoriteLocationDto)
            is LocationAction.NavigateToWeather -> navigateToWeatherWithDelay(action.latLng) //todo delete?
            LocationAction.Redirection -> redirectToLocationApiPage()
            LocationAction.FindByQuery -> findTypedLocation()
            is LocationAction.ToggleMap -> toggleMap(action.isVisible)
            LocationAction.MyLocation -> onMyLocationClick()
            LocationAction.Settings -> setSideEffect(LocationMainEffect.NavigateToSettings)
            LocationAction.OnBackClick -> navigateBack()
            LocationAction.RequestLocationPermissions -> onRequestPermissions()
            is LocationAction.UpdateFavoriteLocation -> updateFavoriteLocation(action.favoriteLocationDto)
            is LocationAction.ToggleEditFavoriteLocationDialog -> onToggle(action.item)
            is LocationAction.SetDefaultLocationName -> setDefaultLocationName(action.item)
            LocationAction.DismissConfirmPinDialog -> onDismissConfirmPinDialog()
            LocationAction.NewLocationConfirm -> onNewLocationConfirm()
            is LocationAction.NewLocationPicked -> onNewLocationPicked(action.location)
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

    private fun navigateToWeatherWithDelay(latLng: UserLatLng) {
        launch {
            delay(200)
            saveQueryAndNavigateBackToWeather(latLng.toString(), latLng)
        }
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

    private fun onNewLocationPicked(newLocation: UserLatLng) {
        setState {
            copy(
                newPickedLocation = newLocation,
                locationDialog = LocationDialog.ConfirmPickedLocation,
            )
        }
    }

    private fun onDismissConfirmPinDialog() {
        setState { copy(newPickedLocation = null) }
        onDismissDialog()
    }

    private fun onShowDeleteFavoriteLocation(item: FavoriteLocation) {
        setState { copy(locationDialog = LocationDialog.DeleteLocation(item)) }
    }

    //todo доработать чтобы маркер перемещался
    private fun onNewLocationConfirm() {
        launch {
            onDismissDialog()
            delay(ONE_SEC)
            val latLng = state.value.newPickedLocation
            setState { copy(newPickedLocation = null, loadNewLocationWeather = false) }
            if (latLng != null) {
                val userLatLng = latLng.copy(time = System.currentTimeMillis())
                toggleMap(false)
                saveQueryAndNavigateBackToWeather(
                    query = userLatLng.toString(),
                    userLatLng = userLatLng,
                )
            }
        }
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

    private fun initMapPin() {
        setState { copy(mapPinLocation = sharedPrefsProvider.loadForecastFromCache()?.location) }
    }

    private fun toggleMap(isShown: Boolean) {
        setSideEffect(LocationMainEffect.ToggleMap(isShown))
    }

    private fun onNewGeoText(text: String) {
        setState { copy(geoText = text) }
    }

    private fun onNewEditLocationText(text: String) {
        setState { copy(editLocationText = text) }
    }

    companion object {
        private const val ONE_SEC = 1000L
    }

}