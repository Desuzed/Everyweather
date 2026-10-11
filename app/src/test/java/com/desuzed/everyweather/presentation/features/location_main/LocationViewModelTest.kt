package com.desuzed.everyweather.presentation.features.location_main

import com.desuzed.everyweather.analytics.LocationMainAnalytics
import com.desuzed.everyweather.data.repository.providers.action_result.GeoActionResultProvider
import com.desuzed.everyweather.domain.interactor.LocationInteractor
import com.desuzed.everyweather.domain.interactor.SystemInteractor
import com.desuzed.everyweather.domain.model.location.FavoriteLocation
import com.desuzed.everyweather.domain.model.location.geo.GeoData
import com.desuzed.everyweather.domain.model.location.geo.ResultGeo
import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider
import com.desuzed.everyweather.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocationViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var locationInteractor: LocationInteractor
    private lateinit var analytics: LocationMainAnalytics
    private lateinit var weatherDataRepository: WeatherDataRepository
    private lateinit var systemInteractor: SystemInteractor

    private val favorite = FavoriteLocation(
        latLon = "55.75,37.61",
        cityName = "Moscow",
        region = "Moscow",
        country = "RU",
        lat = "55.75",
        lon = "37.61",
        customName = "",
    )

    @Before
    fun setUp() {
        locationInteractor = mockk(relaxed = true)
        analytics = mockk(relaxed = true)
        weatherDataRepository = mockk(relaxed = true)
        systemInteractor = mockk(relaxed = true)
        every { locationInteractor.getAllLocations() } returns flowOf(emptyList())
        every { systemInteractor.arePermissionsGranted() } returns false
    }

    @Test
    fun `favorite click saves its query and navigates back`() = runTest {
        val viewModel = LocationViewModel(
            locationInteractor,
            analytics,
            weatherDataRepository,
            systemInteractor,
        )
        val effect = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.sideEffect.first()
        }

        viewModel.onAction(LocationAction.FavoriteLocationClick(favorite))
        advanceUntilIdle()

        coVerify {
            weatherDataRepository.saveQuery("55.75, 37.61", true, null)
        }
        assertEquals(LocationMainEffect.NavigateBack, effect.await())
    }

    @Test
    fun `delete success emits deleted result`() = runTest {
        coEvery { locationInteractor.deleteFavoriteLocation(favorite) } returns true
        val viewModel = createViewModel()
        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.sideEffect.first() }

        viewModel.onAction(LocationAction.DeleteFavoriteLocation(favorite))
        advanceUntilIdle()

        assertEquals(
            ActionResultProvider.DELETED,
            (effect.await() as LocationMainEffect.ShowSnackbar).data.code,
        )
    }

    @Test
    fun `delete failure emits fail result`() = runTest {
        coEvery { locationInteractor.deleteFavoriteLocation(favorite) } returns false
        val viewModel = createViewModel()
        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.sideEffect.first() }

        viewModel.onAction(LocationAction.DeleteFavoriteLocation(favorite))
        advanceUntilIdle()

        assertEquals(
            ActionResultProvider.FAIL,
            (effect.await() as LocationMainEffect.ShowSnackbar).data.code,
        )
    }

    @Test
    fun `update success saves edited name and emits updated result`() = runTest {
        coEvery { locationInteractor.updateLocation(any()) } returns true
        val viewModel = createViewModel()
        viewModel.onAction(LocationAction.ToggleEditFavoriteLocationDialog(favorite))
        viewModel.onAction(LocationAction.EditLocationText("Home"))
        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.sideEffect.first() }

        viewModel.onAction(LocationAction.UpdateFavoriteLocation(favorite))
        advanceUntilIdle()

        coVerify { locationInteractor.updateLocation(favorite.copy(customName = "Home")) }
        assertEquals(
            ActionResultProvider.UPDATED,
            (effect.await() as LocationMainEffect.ShowSnackbar).data.code,
        )
    }

    @Test
    fun `update failure emits fail result`() = runTest {
        coEvery { locationInteractor.updateLocation(any()) } returns false
        val viewModel = createViewModel()
        viewModel.onAction(LocationAction.ToggleEditFavoriteLocationDialog(favorite))
        viewModel.onAction(LocationAction.EditLocationText("Home"))
        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.sideEffect.first() }

        viewModel.onAction(LocationAction.UpdateFavoriteLocation(favorite))
        advanceUntilIdle()

        assertEquals(
            ActionResultProvider.FAIL,
            (effect.await() as LocationMainEffect.ShowSnackbar).data.code,
        )
    }

    @Test
    fun `geocoding rate limit falls back to weather query`() = runTest {
        val query = "Paris"
        coEvery { locationInteractor.fetchGeocodingResultOrError(query) } returns ResultGeo(
            geoData = null,
            queryResult = QueryResult(GeoActionResultProvider.RATE_LIMIT, query),
        )
        val viewModel = createViewModel()
        viewModel.onAction(LocationAction.GeoInputQuery(query))
        viewModel.onAction(LocationAction.FindByQuery)
        advanceUntilIdle()

        coVerify { weatherDataRepository.saveQuery(query, true, null) }
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `ordinary geocoding error shows snackbar and does not save query`() = runTest {
        val query = "unknown place"
        coEvery { locationInteractor.fetchGeocodingResultOrError(query) } returns ResultGeo(
            geoData = null,
            queryResult = QueryResult(400, query),
        )
        val viewModel = createViewModel()
        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.sideEffect.first() }
        viewModel.onAction(LocationAction.GeoInputQuery(query))
        viewModel.onAction(LocationAction.FindByQuery)
        advanceUntilIdle()

        val snackbar = effect.await() as LocationMainEffect.ShowSnackbar
        assertEquals(400, snackbar.data.code)
        coVerify(exactly = 0) { weatherDataRepository.saveQuery(any(), any(), any()) }
    }

    @Test
    fun `geocoding success exposes candidates for confirmation`() = runTest {
        val geo = GeoData("48.85", "2.35", "Paris", 0.8f)
        val query = "Paris"
        coEvery { locationInteractor.fetchGeocodingResultOrError(query) } returns ResultGeo(
            geoData = listOf(geo),
            queryResult = null,
        )
        val viewModel = createViewModel()
        viewModel.onAction(LocationAction.GeoInputQuery(query))
        viewModel.onAction(LocationAction.FindByQuery)
        advanceUntilIdle()

        assertEquals(listOf(geo), viewModel.state.value.geoData)
        assertEquals(LocationDialog.GeoPickerData, viewModel.state.value.locationDialog)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `confirming geocoding candidate saves coordinates`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(
            LocationAction.ConfirmFoundLocation(GeoData("48.85", "2.35", "Paris", 0.8f))
        )
        advanceUntilIdle()

        coVerify { weatherDataRepository.saveQuery("48.85,2.35", true, null) }
    }

    private fun createViewModel() = LocationViewModel(
        locationInteractor,
        analytics,
        weatherDataRepository,
        systemInteractor,
    )
}
