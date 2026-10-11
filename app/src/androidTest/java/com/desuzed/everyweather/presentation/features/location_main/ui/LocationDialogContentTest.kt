package com.desuzed.everyweather.presentation.features.location_main.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.location.FavoriteLocation
import com.desuzed.everyweather.presentation.features.location_main.LocationAction
import com.desuzed.everyweather.presentation.features.location_main.LocationDialog
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationDialogContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun editDialog_inputAndSave_dispatchUpdatedLocation() {
        val location = favoriteLocation()
        val actions = mutableListOf<LocationAction>()
        var input by mutableStateOf("")

        composeRule.setContent {
            EveryweatherTheme {
                LocationDialogContent(
                    dialog = LocationDialog.EditLocation(location),
                    geoData = null,
                    editLocationText = input,
                    onAction = {
                        actions += it
                        if (it is LocationAction.EditLocationText) input = it.input
                    },
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("Office")
        composeRule.onNodeWithText("Office").assertExists()
        composeRule.onNode(
            hasClickAction().and(
                SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
            ),
            useUnmergedTree = true,
        ).performClick()
        composeRule.runOnIdle { }

        assertEquals("Office", (actions.first() as LocationAction.EditLocationText).input)
        assertSame(location, (actions.last() as LocationAction.UpdateFavoriteLocation).favoriteLocationDto)
    }

    @Test
    fun editDialog_backDismiss_dispatchesDismissDialog() {
        val actions = mutableListOf<LocationAction>()
        var dialog by mutableStateOf<LocationDialog?>(LocationDialog.EditLocation(favoriteLocation()))
        composeRule.setContent {
            EveryweatherTheme {
                LocationDialogContent(
                    dialog = dialog,
                    geoData = null,
                    editLocationText = "Office",
                    onAction = {
                        actions += it
                        if (it == LocationAction.DismissDialog) dialog = null
                    },
                )
            }
        }

        pressBack()
        composeRule.runOnIdle { }

        assertEquals(LocationAction.DismissDialog, actions.single())
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.change_location_name))
            .assertDoesNotExist()
    }

    @Test
    fun deleteDialog_cancelAndConfirm_dispatchExpectedActions() {
        val location = favoriteLocation()
        val actions = mutableListOf<LocationAction>()
        var dialog by mutableStateOf<LocationDialog?>(LocationDialog.DeleteLocation(location))
        composeRule.setContent {
            EveryweatherTheme {
                LocationDialogContent(
                    dialog = dialog,
                    geoData = null,
                    editLocationText = "",
                    onAction = {
                        actions += it
                        if (it == LocationAction.DismissDialog || it is LocationAction.DeleteFavoriteLocation) {
                            dialog = null
                        }
                    },
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cancel)).performClick()
        assertEquals(LocationAction.DismissDialog, actions.single())
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.delete)).assertDoesNotExist()

        composeRule.runOnIdle { dialog = LocationDialog.DeleteLocation(location) }
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.ok)).performClick()

        assertSame(location, (actions.last() as LocationAction.DeleteFavoriteLocation).favoriteLocationDto)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.delete)).assertDoesNotExist()
    }

    @Test
    fun permissionDialog_allowAndCancel_dispatchExpectedActions() {
        val actions = mutableListOf<LocationAction>()
        var dialog by mutableStateOf<LocationDialog?>(LocationDialog.RequireLocationPermissions)
        composeRule.setContent {
            EveryweatherTheme {
                LocationDialogContent(
                    dialog = dialog,
                    geoData = null,
                    editLocationText = "",
                    onAction = {
                        actions += it
                        if (it == LocationAction.DismissDialog || it == LocationAction.RequestLocationPermissions) {
                            dialog = null
                        }
                    },
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.allow)).performClick()
        assertEquals(LocationAction.RequestLocationPermissions, actions.single())
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.require_location_permissions_title)
        ).assertDoesNotExist()

        composeRule.runOnIdle { dialog = LocationDialog.RequireLocationPermissions }
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cancel)).performClick()

        assertEquals(LocationAction.DismissDialog, actions.last())
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.require_location_permissions_title)
        ).assertDoesNotExist()
    }

    @Test
    fun geoPicker_emptyFixture_reportsZeroResults() {
        composeRule.setContent {
            EveryweatherTheme {
                LocationDialogContent(
                    dialog = LocationDialog.GeoPickerData,
                    geoData = emptyList(),
                    editLocationText = "",
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.results_were_found, 0)
        ).assertExists()
    }

    private fun favoriteLocation() = FavoriteLocation(
        latLon = "55.75,37.62",
        cityName = "Moscow",
        region = "Moscow",
        country = "Russia",
        lat = "55.75",
        lon = "37.62",
        customName = "Home",
    )
}
