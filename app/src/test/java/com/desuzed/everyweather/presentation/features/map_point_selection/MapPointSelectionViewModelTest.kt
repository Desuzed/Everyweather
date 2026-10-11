package com.desuzed.everyweather.presentation.features.map_point_selection

import com.desuzed.everyweather.analytics.MapPointSelectionAnalytics
import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MapPointSelectionViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: WeatherDataRepository
    private lateinit var analytics: MapPointSelectionAnalytics

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        analytics = mockk(relaxed = true)
        every { repository.getWeatherContentFlow() } returns flowOf(null)
    }

    @Test
    fun `confirm saves full precision map point and navigates to weather`() = runTest {
        val viewModel = MapPointSelectionViewModel(repository, analytics)
        val point = UserLatLng(55.1234567890123, 37.9876543210987, 0L)
        viewModel.onAction(MapPointSelectionAction.NewLocationPicked(point))
        assertTrue(viewModel.state.value.isConfirmDialogShown)

        viewModel.onAction(MapPointSelectionAction.NewLocationConfirm)
        advanceUntilIdle()

        val query = slot<String>()
        val savedPoint = slot<UserLatLng>()
        coVerify {
            repository.saveQuery(capture(query), true, capture(savedPoint))
        }
        assertEquals("55.1234567890123, 37.9876543210987", query.captured)
        assertEquals(point.lat, savedPoint.captured.lat, 0.0)
        assertEquals(point.lon, savedPoint.captured.lon, 0.0)
        assertTrue(savedPoint.captured.time > 0L)
        assertFalse(viewModel.state.value.isConfirmDialogShown)
    }

    @Test
    fun `dismiss clears selected point without saving`() = runTest {
        val viewModel = MapPointSelectionViewModel(repository, analytics)
        viewModel.onAction(MapPointSelectionAction.NewLocationPicked(UserLatLng(1.0, 2.0, 0L)))
        viewModel.onAction(MapPointSelectionAction.DismissConfirmPinDialog)
        advanceUntilIdle()

        assertNull(viewModel.state.value.newSelectedLocation)
        assertFalse(viewModel.state.value.isConfirmDialogShown)
        coVerify(exactly = 0) { repository.saveQuery(any(), any(), any()) }
    }
}
