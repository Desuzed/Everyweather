package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.theming.EveryweatherTheme

@Composable
fun AppTextButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = EveryweatherTheme.colors.primary,
    textAlign: TextAlign? = null,
    onClick: () -> Unit,
) {
    Text(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, //rememberRipple(color = Color.Black),
            onClick = onClick,
        ),
        text = text,
        style = EveryweatherTheme.typography.h3,
        textAlign = textAlign,
        color = color,
    )
}

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppTextButton(text = "button") {}
        }
    }
}