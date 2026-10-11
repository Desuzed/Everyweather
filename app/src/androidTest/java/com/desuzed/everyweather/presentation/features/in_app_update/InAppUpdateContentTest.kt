package com.desuzed.everyweather.presentation.features.in_app_update

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.presentation.features.in_app_update.ui.InAppUpdateContent
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InAppUpdateContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun launchUpdate_rendersPromptAndUpdateAction() {
        val actions = mutableListOf<InAppUpdateAction>()
        compose(InAppUpdateStatus.READY_TO_LAUNCH_UPDATE, actions)

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.update_available_title)).assertExists()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.update_available_update_button)).performClick()

        assertEquals(listOf(InAppUpdateAction.AgreedToUpdate), actions)
    }

    @Test
    fun launchUpdate_laterDismissesPrompt() {
        val actions = mutableListOf<InAppUpdateAction>()
        compose(InAppUpdateStatus.READY_TO_LAUNCH_UPDATE, actions)

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.update_available_later_button)).performClick()

        assertEquals(listOf(InAppUpdateAction.Dismiss), actions)
    }

    @Test
    fun readyToInstall_rendersInstallPromptAndAction() {
        val actions = mutableListOf<InAppUpdateAction>()
        compose(InAppUpdateStatus.READY_TO_INSTALL, actions)

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.update_ready_to_install_title)).assertExists()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.update_install_button)).performClick()

        assertEquals(listOf(InAppUpdateAction.AgreedToInstallUpdate), actions)
    }

    @Test
    fun readyToInstall_dismissesFromNegativeButton() {
        val actions = mutableListOf<InAppUpdateAction>()
        compose(InAppUpdateStatus.READY_TO_INSTALL, actions)

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.update_dismiss_button)).performClick()

        assertEquals(listOf(InAppUpdateAction.Dismiss), actions)
    }

    @Test
    fun missingStatus_usesLaunchUpdatePrompt() {
        val actions = mutableListOf<InAppUpdateAction>()
        compose(null, actions)

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.update_available_title)).assertExists()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.update_available_later_button)).performClick()

        assertEquals(listOf(InAppUpdateAction.Dismiss), actions)
    }

    private fun compose(
        status: InAppUpdateStatus?,
        actions: MutableList<InAppUpdateAction>,
    ) {
        composeRule.setContent {
            EveryweatherTheme {
                InAppUpdateContent(
                    state = InAppUpdateState(updateStatus = status),
                    onAction = actions::add,
                )
            }
        }
    }
}
