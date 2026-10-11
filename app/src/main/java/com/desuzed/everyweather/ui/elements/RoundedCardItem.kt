package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.dimensionResource
import com.desuzed.everyweather.R
import com.desuzed.everyweather.ui.theming.EveryweatherTheme

@Composable
fun RoundedCardItem(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16))
    val cardModifier = modifier
        .fillMaxWidth()
        .shadow(
            elevation = dimensionResource(id = R.dimen.dimen_4),
            shape = cardShape,
        )
        .clip(cardShape)
        .background(EveryweatherTheme.colors.surfacePrimary)
    if (onClick != null) {
        Box(
            modifier = cardModifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
        ) {
            content()
        }
    } else {
        Box(modifier = cardModifier) {
            content()
        }
    }
}
