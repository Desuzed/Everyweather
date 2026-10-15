package com.desuzed.everyweather.presentation.features.weather_main

import com.desuzed.everyweather.analytics.WeatherMainAnalytics
import com.desuzed.everyweather.domain.interactor.LocationInteractor
import com.desuzed.everyweather.domain.interactor.WeatherInteractor
import com.desuzed.everyweather.domain.interactor.WeatherSettingsInteractor
import com.desuzed.everyweather.domain.model.location.UserLatLng
import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.domain.model.weather.Astro
import com.desuzed.everyweather.domain.model.weather.Current
import com.desuzed.everyweather.domain.model.weather.Day
import com.desuzed.everyweather.domain.model.weather.ForecastDay
import com.desuzed.everyweather.domain.model.weather.Location
import com.desuzed.everyweather.domain.model.weather.ResultForecast
import com.desuzed.everyweather.domain.model.weather.WeatherContent
import com.desuzed.everyweather.domain.model.weather.WeatherQuery
import com.desuzed.everyweather.domain.repository.local.WeatherDataRepository
import com.desuzed.everyweather.domain.repository.settings.SystemSettingsRepository
import com.desuzed.everyweather.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WeatherMainViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var weatherInteractor: WeatherInteractor
    private lateinit var repository: WeatherDataRepository
    private lateinit var locationInteractor: LocationInteractor
    private lateinit var analytics: WeatherMainAnalytics
    private lateinit var weatherSettings: WeatherSettingsInteractor
    private lateinit var systemSettings: SystemSettingsRepository
    private lateinit var queryFlow: MutableStateFlow<WeatherQuery>
    private lateinit var contentFlow: MutableStateFlow<WeatherContent?>

    @Before
    fun setUp() {
        weatherInteractor = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        locationInteractor = mockk(relaxed = true)
        analytics = mockk(relaxed = true)
        weatherSettings = mockk(relaxed = true)
        systemSettings = mockk(relaxed = true)
        queryFlow = MutableStateFlow(WeatherQuery("initial", "Moscow"))
        contentFlow = MutableStateFlow(null)

        every { repository.getQueryFlow() } returns queryFlow
        every { repository.getWeatherContentFlow() } returns contentFlow
        every { weatherSettings.distanceDimen } returns flowOf(DistanceDimen.METRIC_KMH)
        every { weatherSettings.tempDimen } returns flowOf(TempDimen.CELCIUS)
        every { weatherSettings.pressureDimen } returns flowOf(PressureDimen.MILLIMETERS)
        every { systemSettings.lang } returns flowOf(Lang.EN)
        coEvery { locationInteractor.isLocationSaved(any()) } returns false
    }

    @Test
    fun `initial query triggers one forecast and does not issue blank request`() = runTest {
        val content = content("initial")
        coEvery { weatherInteractor.fetchForecastOrErrorMessage("Moscow", null) } returns
            ResultForecast(content, null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            weatherInteractor.fetchForecastOrErrorMessage("Moscow", null)
        }
        assertEquals(content, viewModel.state.value.weatherData)
        assertEquals("Moscow", viewModel.state.value.query)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `a slow request is cancelled when a newer query arrives`() = runTest {
        val contentA = content("A")
        val contentB = content("B")
        coEvery { weatherInteractor.fetchForecastOrErrorMessage(any(), any()) } coAnswers {
            when (args[0] as String) {
                "A" -> {
                    delay(1_000)
                    ResultForecast(contentA, null)
                }
                else -> ResultForecast(contentB, null)
            }
        }
        queryFlow.value = WeatherQuery("a", "A", shouldTriggerWeatherRequest = true)
        val viewModel = createViewModel()
        runCurrent()

        queryFlow.value = WeatherQuery("b", "B", shouldTriggerWeatherRequest = true)
        advanceUntilIdle()

        assertEquals(contentB, viewModel.state.value.weatherData)
        assertEquals("B", viewModel.state.value.query)
        coVerify(exactly = 1) { weatherInteractor.fetchForecastOrErrorMessage("A", null) }
        coVerify(exactly = 1) { weatherInteractor.fetchForecastOrErrorMessage("B", null) }
    }

    @Test
    fun `successful result is displayed even when repository flow stays unchanged`() = runTest {
        val result = content("42")
        coEvery { weatherInteractor.fetchForecastOrErrorMessage("42", null) } returns
            ResultForecast(result, null)
        queryFlow.value = WeatherQuery("result", "42", shouldTriggerWeatherRequest = true)
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(result, viewModel.state.value.weatherData)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `failed request preserves cached display and emits retry error`() = runTest {
        val cached = content("cached")
        contentFlow.value = cached
        coEvery { weatherInteractor.fetchForecastOrErrorMessage("Moscow", null) } returns
            ResultForecast(cached, QueryResult(503, "Moscow"))
        val viewModel = createViewModel()
        val effect = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.sideEffect.first()
        }
        advanceUntilIdle()

        assertEquals(cached, viewModel.state.value.weatherData)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(503, (effect.await() as WeatherMainEffect.ShowSnackBar).data.code)
    }

    @Test
    fun `save location reports success and disables add button`() = runTest {
        val result = content("save")
        coEvery { weatherInteractor.fetchForecastOrErrorMessage("save", null) } returns
            ResultForecast(result, null)
        coEvery { locationInteractor.saveLocationToDb(result) } returns true
        queryFlow.value = WeatherQuery("save", "save", shouldTriggerWeatherRequest = true)
        val viewModel = createViewModel()
        advanceUntilIdle()
        val effect = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.sideEffect.first()
        }

        viewModel.onAction(WeatherAction.SaveLocation)
        advanceUntilIdle()

        coVerify { locationInteractor.saveLocationToDb(result) }
        assertEquals(1, (effect.await() as WeatherMainEffect.ShowSnackBar).data.code)
        assertFalse(viewModel.state.value.isAddButtonEnabled)
    }

    @Test
    fun `unit and language flows are reflected in state`() = runTest {
        val distance = MutableStateFlow(DistanceDimen.METRIC_KMH)
        val temp = MutableStateFlow(TempDimen.CELCIUS)
        val pressure = MutableStateFlow(PressureDimen.MILLIMETERS)
        val lang = MutableStateFlow(Lang.EN)
        every { weatherSettings.distanceDimen } returns distance
        every { weatherSettings.tempDimen } returns temp
        every { weatherSettings.pressureDimen } returns pressure
        every { systemSettings.lang } returns lang
        coEvery { weatherInteractor.fetchForecastOrErrorMessage("Moscow", null) } returns
            ResultForecast(null, QueryResult(204, "Moscow"))

        val viewModel = createViewModel()
        advanceUntilIdle()
        distance.value = DistanceDimen.IMPERIAL
        temp.value = TempDimen.FAHRENHEIT
        pressure.value = PressureDimen.INCHES
        lang.value = Lang.RU
        advanceUntilIdle()

        assertEquals(DistanceDimen.IMPERIAL, viewModel.state.value.windSpeed)
        assertEquals(TempDimen.FAHRENHEIT, viewModel.state.value.temperature)
        assertEquals(PressureDimen.INCHES, viewModel.state.value.pressure)
        assertEquals(Lang.RU, viewModel.state.value.selectedLang)
    }

    private fun createViewModel() = WeatherMainViewModel(
        weatherInteractor,
        repository,
        locationInteractor,
        analytics,
        weatherSettings,
        systemSettings,
    )

    private fun content(name: String) = WeatherContent(
        location = Location(name, "region", "country", 55.0, 37.0, "UTC", 1L),
        current = Current(
            1f, 2f, "clear", "icon", 3f, 4f, 5f, 6f, 7f, 8f,
            9, 10f, 11f,
        ),
        forecastDay = listOf(
            ForecastDay(
                date = "2026-01-01",
                dateEpoch = 1L,
                day = Day(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10, "clear", "icon"),
                astro = Astro("06:00", "18:00", "", ""),
                hourForecast = emptyList(),
            )
        ),
    )
}
