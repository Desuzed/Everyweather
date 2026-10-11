package com.desuzed.everyweather.presentation.features.weather_main.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desuzed.everyweather.R
import com.desuzed.everyweather.data.repository.providers.action_result.WeatherActionResultProvider
import com.desuzed.everyweather.domain.model.result.ActionType
import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider
import com.desuzed.everyweather.ui.elements.AppSnackbar
import com.desuzed.everyweather.ui.elements.CollectSnackbar
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherFeedbackTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun weatherNotFound_retry_invokesCallbackOnceAndDismissesSnackbar() {
        val query = "Atlantis"
        var retryCount = 0
        val expectedMessage = composeRule.activity.getString(R.string.no_location_found, query)

        composeRule.setContent {
            EveryweatherTheme {
                val snackbarState = remember { androidx.compose.material3.SnackbarHostState() }
                Box {
                    CollectSnackbar(
                        queryResult = QueryResult(
                            code = 1006,
                            query = query,
                            actionType = ActionType.RETRY,
                            id = "weather-1006-retry",
                        ),
                        snackbarState = snackbarState,
                        providerClass = WeatherActionResultProvider::class,
                        onRetryClick = { retryCount++ },
                    )
                    AppSnackbar(snackbarState = snackbarState)
                }
            }
        }

        waitForMessage(expectedMessage)
        composeRule.onNodeWithText(expectedMessage).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.retry)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { retryCount == 1 }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(expectedMessage).fetchSemanticsNodes().isEmpty()
        }

        assertEquals(1, retryCount)
    }

    @Test
    fun savedResult_okAction_dismissesWithoutRetry() {
        var retryCount = 0
        val expectedMessage = composeRule.activity.getString(R.string.saved)

        composeRule.setContent {
            EveryweatherTheme {
                val snackbarState = remember { androidx.compose.material3.SnackbarHostState() }
                Box {
                    CollectSnackbar(
                        queryResult = QueryResult(
                            code = ActionResultProvider.SAVED,
                            actionType = ActionType.OK,
                            id = "weather-saved-ok",
                        ),
                        snackbarState = snackbarState,
                        providerClass = WeatherActionResultProvider::class,
                        onRetryClick = { retryCount++ },
                    )
                    AppSnackbar(snackbarState = snackbarState)
                }
            }
        }

        waitForMessage(expectedMessage)
        composeRule.onNodeWithText(expectedMessage).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.ok)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(expectedMessage).fetchSemanticsNodes().isEmpty()
        }

        assertEquals(0, retryCount)
    }

    @Test
    fun newQueryResultId_showsNextMessageAfterDismissal() {
        var queryResult by mutableStateOf(
            QueryResult(
                code = 1006,
                query = "First city",
                actionType = ActionType.OK,
                id = "weather-query-first",
            )
        )
        val firstMessage = composeRule.activity.getString(R.string.no_location_found, "First city")
        val secondMessage = composeRule.activity.getString(R.string.no_location_found, "Second city")

        composeRule.setContent {
            EveryweatherTheme {
                val snackbarState = remember { androidx.compose.material3.SnackbarHostState() }
                Box {
                    CollectSnackbar(
                        queryResult = queryResult,
                        snackbarState = snackbarState,
                        providerClass = WeatherActionResultProvider::class,
                        onRetryClick = {},
                    )
                    AppSnackbar(snackbarState = snackbarState)
                }
            }
        }

        waitForMessage(firstMessage)
        composeRule.onNodeWithText(firstMessage).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.ok)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(firstMessage).fetchSemanticsNodes().isEmpty()
        }
        composeRule.runOnIdle {
            queryResult = queryResult.copy(query = "Second city", id = "weather-query-second")
        }
        waitForMessage(secondMessage)
        composeRule.onNodeWithText(secondMessage).assertIsDisplayed()
        composeRule.onAllNodesWithText(firstMessage).assertCountEquals(0)
    }

    private fun waitForMessage(message: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
