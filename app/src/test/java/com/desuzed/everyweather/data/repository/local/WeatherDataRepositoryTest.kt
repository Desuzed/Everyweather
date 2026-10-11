package com.desuzed.everyweather.data.repository.local

import com.desuzed.everyweather.domain.repository.local.SharedPrefsProvider
import com.desuzed.everyweather.domain.model.weather.WeatherQuery
import com.desuzed.everyweather.util.MockWeatherObject
import io.mockk.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class WeatherDataRepositoryTest {
    private val store = mockk<WeatherDataStore>(relaxed = true)
    private val prefs = mockk<SharedPrefsProvider>(relaxed = true)
    private val repository = WeatherDataRepositoryImpl(store, prefs)

    @Test fun `repeated legacy query emissions have stable identity`() = runTest {
        every { store.getQuery() } returns flowOf("", "", "")
        every { prefs.loadQuery() } returns "Moscow"
        val queries = repository.getQueryFlow().toList()
        assertEquals(1, queries.size)
        assertEquals("Moscow", queries.single().query)
    }

    @Test fun `corrupt new cache can recover legacy forecast`() = runTest {
        every { store.getForecastData() } returns flowOf("invalid json")
        every { prefs.loadForecastFromCache() } returns MockWeatherObject.weather
        assertEquals(MockWeatherObject.weather, repository.getWeatherContentFlow().toList().single())
    }

    @Test fun `cancellation while saving is not swallowed`() = runTest {
        coEvery { store.setForecastData(any()) } throws CancellationException("cancelled")
        try {
            repository.saveForecastToCache(MockWeatherObject.weather)
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }

    @Test fun `query acknowledgement transforms the value in one datastore update`() = runTest {
        val original = WeatherQuery(
            id = "query-id",
            query = "55.75, 37.62",
            shouldTriggerWeatherRequest = true,
        )
        val originalJson = Json.encodeToString(original)
        var updatedJson: String? = null
        coEvery { store.updateQuery(any()) } answers {
            updatedJson = firstArg<(String) -> String>()(originalJson)
        }

        repository.updateQueryShouldNotBeTriggered()

        assertEquals(
            original.copy(shouldTriggerWeatherRequest = false),
            Json.decodeFromString<WeatherQuery>(updatedJson!!),
        )
        coVerify(exactly = 1) { store.updateQuery(any()) }
        coVerify(exactly = 0) { store.setQuery(any()) }
    }

    @Test fun `query acknowledgement leaves malformed payload unchanged`() = runTest {
        var updatedJson: String? = null
        coEvery { store.updateQuery(any()) } answers {
            updatedJson = firstArg<(String) -> String>()("not-json")
        }

        repository.updateQueryShouldNotBeTriggered()

        assertEquals("not-json", updatedJson)
    }
}
