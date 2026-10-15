package com.desuzed.everyweather.presentation.features.main_activity.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desuzed.everyweather.R
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoadingWidgetsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun bottomContentWidget_locationLoading_showsProgress_andHidesAfterCompletion() {
        var isLookingForLocation by mutableStateOf(true)

        composeRule.setContent {
            EveryweatherTheme {
                BottomContentWidget(isLookingForLocation = isLookingForLocation) {}
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.looking_for_your_location)
        ).assertIsDisplayed()
        progressNodes().assertCountEquals(1)
        assertEquals(ProgressBarRangeInfo.Indeterminate, progressInfo().single())

        composeRule.runOnIdle { isLookingForLocation = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.looking_for_your_location)
        ).assertDoesNotExist()
        progressNodes().assertCountEquals(0)
    }

    @Test
    fun downloadingUpdateWidget_reportsFraction_andHidesAfterCompletion() {
        var isDownloading by mutableStateOf(true)

        composeRule.setContent {
            EveryweatherTheme {
                androidx.compose.foundation.layout.Column {
                    DownloadingInAppUpdateWidget(
                        isDownloadingInProgress = isDownloading,
                        totalBytes = 1_000L,
                        bytesDownloaded = 250L,
                    )
                }
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.downloading_update)
        ).assertIsDisplayed()
        progressNodes().assertCountEquals(1)
        assertEquals(ProgressBarRangeInfo(0.25f, 0f..1f), progressInfo().single())

        composeRule.runOnIdle { isDownloading = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.downloading_update)
        ).assertDoesNotExist()
        progressNodes().assertCountEquals(0)
    }

    @Test
    fun bottomContentWidget_withDownloadingContent_exposesBothProgressIndicators() {
        composeRule.setContent {
            EveryweatherTheme {
                BottomContentWidget(isLookingForLocation = true) {
                    DownloadingInAppUpdateWidget(
                        isDownloadingInProgress = true,
                        totalBytes = 1_000L,
                        bytesDownloaded = 250L,
                    )
                }
            }
        }

        progressNodes().assertCountEquals(2)
        val infos = progressInfo()
        assertTrue(infos.contains(ProgressBarRangeInfo.Indeterminate))
        assertTrue(infos.contains(ProgressBarRangeInfo(0.25f, 0f..1f)))
    }

    private fun progressNodes() = composeRule.onAllNodes(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
    )

    private fun progressInfo(): List<ProgressBarRangeInfo> = progressNodes()
        .fetchSemanticsNodes()
        .map { it.config[SemanticsProperties.ProgressBarRangeInfo] }
}
