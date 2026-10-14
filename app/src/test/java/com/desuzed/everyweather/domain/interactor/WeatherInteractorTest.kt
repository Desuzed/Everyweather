package com.desuzed.everyweather.domain.interactor

import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.model.result.FetchResult
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.weather.Error
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.domain.repository.remote.RemoteDataRepository
import com.desuzed.everyweather.domain.repository.settings.SystemSettingsRepository
import com.desuzed.everyweather.util.MockWeatherObject
import io.mockk.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class WeatherInteractorTest {
    private val remote = mockk<RemoteDataRepository>()
    private val settings = mockk<SystemSettingsRepository>()
    private val cache = mockk<WeatherDataRepository>(relaxed = true)
    private val interactor = WeatherInteractor(remote, settings, cache)
    private val weather = MockWeatherObject.weather

    init {
        every { settings.lang } returns flowOf(Lang.RU)
        every { cache.getWeatherContentFlow() } returns flowOf(weather)
        coEvery { remote.getForecast(any(), any()) } returns FetchResult.Success(weather)
    }

    @Test fun `blank query does not call API`() = runTest {
        assertNotNull(interactor.fetchForecastOrErrorMessage(" ").queryResult)
        coVerify(exactly = 0) { remote.getForecast(any(), any()) }
    }

    @Test fun `forecast uses saved language and exact map coordinates`() = runTest {
        val point = UserLatLng(55.751244, 37.618423, 1)
        val result = interactor.fetchForecastOrErrorMessage("55.751244,37.618423", point)
        assertEquals(point.lat, result.weatherContent!!.location.lat, 0.0)
        assertEquals(point.lon, result.weatherContent!!.location.lon, 0.0)
        coVerify { remote.getForecast("55.751244,37.618423", "ru") }
    }

    @Test fun `API error preserves offline weather and retry query`() = runTest {
        coEvery { remote.getForecast(any(), any()) } returns FetchResult.Failure(Error(500, "error"))
        val result = interactor.fetchForecastOrErrorMessage("Moscow")
        assertEquals(weather, result.weatherContent)
        assertEquals("Moscow", result.queryResult!!.query)
        coVerify(exactly = 0) { cache.saveForecastToCache(any()) }
    }

    @Test fun `storage failure does not discard successful network weather`() = runTest {
        coEvery { cache.saveForecastToCache(any()) } throws IOException("full disk")
        val result = interactor.fetchForecastOrErrorMessage("Moscow")
        assertEquals(weather, result.weatherContent)
        assertNull(result.queryResult)
    }

    @Test fun `cancellation is propagated from storage`() = runTest {
        coEvery { cache.saveForecastToCache(any()) } throws CancellationException("cancelled")
        try {
            interactor.fetchForecastOrErrorMessage("Moscow")
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }
}
