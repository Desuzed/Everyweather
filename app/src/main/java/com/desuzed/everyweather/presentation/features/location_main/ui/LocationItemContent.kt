package com.desuzed.everyweather.presentation.features.location_main.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.location.FavoriteLocation
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.elements.LargeBoldText
import com.desuzed.everyweather.ui.elements.MediumText
import com.desuzed.everyweather.ui.theming.EveryweatherTheme

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        LocationItemContent(
            item = FavoriteLocation(
                latLon = "",
                cityName = "London",
                region = "RegionName",
                country = "England",
                lat = "",
                lon = "",
                customName = "",
            ),
            onClick = {},
            onLongClick = {},
            onEditClick = {},
        )
    }
}

@Composable
fun LocationItemContent(
    item: FavoriteLocation,
    onClick: (FavoriteLocation) -> Unit,
    onLongClick: (FavoriteLocation) -> Unit,
    onEditClick: (FavoriteLocation) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.dimen_4)),
        shape = RoundedCornerShape(
            dimensionResource(id = R.dimen.corner_radius_16),
        ),
        backgroundColor = EveryweatherTheme.colors.surfacePrimary,
        elevation = dimensionResource(id = R.dimen.dimen_4)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(dimensionResource(id = R.dimen.dimen_10))
                    .testTag("favorite_location_${item.latLon}")
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onLongClick(item) },
                            onTap = { onClick(item) }
                        )
                    }
            ) {
                LargeBoldText(
                    text = item.customName.ifBlank { item.cityName },
                    overflow = TextOverflow.Ellipsis,
                )
                MediumText(text = item.fullName())
            }
            IconButton(
                modifier = Modifier.testTag("edit_favorite_location_${item.latLon}"),
                onClick = { onEditClick(item) },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_location),
                    tint = EveryweatherTheme.colors.onBackgroundPrimary,
                    contentDescription = null,
                )
            }
        }
    }
}
