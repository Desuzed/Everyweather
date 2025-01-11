package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import com.desuzed.everyweather.R
import com.desuzed.everyweather.ui.theming.EveryweatherTheme

@Composable
fun RoundedCardItem(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = dimensionResource(id = R.dimen.dimen_4),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)),
                )//todo shapes
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)))
                .clickable(
                    interactionSource = interactionSource,
                    onClick = onClick,
                    indication = null, //rememberRipple(),//TODO ripple for dark mode
                    role = Role.Button,
                ),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)),
            backgroundColor = EveryweatherTheme.colors.surfacePrimary,
            elevation = dimensionResource(id = R.dimen.dimen_4),
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)),
            backgroundColor = EveryweatherTheme.colors.surfacePrimary,
            elevation = dimensionResource(id = R.dimen.dimen_4),
        ) {
            content()
        }
    }
}