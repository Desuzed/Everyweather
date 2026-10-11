package com.desuzed.everyweather.presentation.features.location_main.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.location.FavoriteLocation
import com.desuzed.everyweather.domain.model.location.geo.GeoData
import com.desuzed.everyweather.presentation.features.location_main.LocationAction
import com.desuzed.everyweather.presentation.features.location_main.LocationDialog
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun favoriteItem_routesTapLongPressAndEditToItsCallbacks() {
        val location = favoriteLocation()
        val tapped = mutableListOf<FavoriteLocation>()
        val longPressed = mutableListOf<FavoriteLocation>()
        val edited = mutableListOf<FavoriteLocation>()

        composeRule.setContent {
            EveryweatherTheme {
                LocationItemContent(
                    item = location,
                    onClick = tapped::add,
                    onLongClick = longPressed::add,
                    onEditClick = edited::add,
                )
            }
        }

        composeRule.onNodeWithTag("favorite_location_${location.latLon}").performTouchInput { click() }
        composeRule.onNodeWithTag("favorite_location_${location.latLon}").performTouchInput { longClick() }
        composeRule.onNodeWithTag("edit_favorite_location_${location.latLon}").performClick()

        assertSame(location, tapped.single())
        assertSame(location, longPressed.single())
        assertSame(location, edited.single())
    }

    @Test
    fun geocodeResultAndDeleteConfirmation_dispatchTheirDomainActions() {
        val geo = GeoData(lat = "55.75", lon = "37.62", name = "Moscow", importance = 1f)
        val location = favoriteLocation()
        val actions = mutableListOf<LocationAction>()

        composeRule.setContent {
            EveryweatherTheme {
                GeoDataItem(geoData = geo, onAction = actions::add)
                LocationDialogContent(
                    dialog = LocationDialog.DeleteLocation(location),
                    geoData = null,
                    editLocationText = "",
                    onAction = actions::add,
                )
            }
        }

        composeRule.onNodeWithText(geo.name).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.ok)).performClick()

        assertSame(geo, (actions[0] as LocationAction.ConfirmFoundLocation).geo)
        assertSame(location, (actions[1] as LocationAction.DeleteFavoriteLocation).favoriteLocationDto)
    }

    @Test
    fun locationSearch_reportsTypedQueryAndFindAction() {
        val actions = mutableListOf<LocationAction>()
        var query by mutableStateOf("")

        composeRule.setContent {
            EveryweatherTheme {
                LocationMainPageContent(
                    locations = emptyList(),
                    isLoading = false,
                    geoText = query,
                    onGeoTextChanged = {
                        query = it
                        actions += LocationAction.GeoInputQuery(it)
                    },
                    onAction = actions::add,
                    onShowDeleteDialog = {},
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("Moscow")
        composeRule.onNode(hasSetTextAction()).assertTextContains("Moscow")
        composeRule.onNode(hasSetTextAction()).performImeAction()

        assertEquals("Moscow", (actions[0] as LocationAction.GeoInputQuery).input)
        assertEquals(LocationAction.FindByQuery, actions[1])
    }

    @Test
    fun locationPage_emptyState_isVisible() {
        composeRule.setContent {
            EveryweatherTheme {
                LocationMainBody(
                    locations = emptyList(),
                    isLoading = false,
                    geoText = "",
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.your_saved_cities_list_is_empty)
        ).assertExists()
    }

    @Test
    fun locationSearch_loadingState_showsProgressIndicator() {
        composeRule.setContent {
            EveryweatherTheme {
                LocationMainPageContent(
                    locations = emptyList(),
                    isLoading = true,
                    geoText = "Moscow",
                    onGeoTextChanged = {},
                    onAction = {},
                    onShowDeleteDialog = {},
                )
            }
        }

        composeRule.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
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
