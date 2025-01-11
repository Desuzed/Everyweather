package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.desuzed.everyweather.R
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.Constants.EMPTY_STRING

@Composable
fun AppToolbar(
    modifier: Modifier = Modifier,
    title: String,
    startIconId: Int = R.drawable.ic_round_arrow_back,
    onStartIconClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.padding(vertical = dimensionResource(id = R.dimen.dimen_10))
    ) {
        LargeBoldText(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        IconButton(
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.dimen_34))
                .padding(start = dimensionResource(id = R.dimen.dimen_10)),
            onClick = onStartIconClick,
            content = {
                Icon(
                    painter = painterResource(id = startIconId),
                    contentDescription = EMPTY_STRING,
                    tint = EveryweatherTheme.colors.onBackgroundPrimary
                )
            },
        )
    }
}

@Composable
@AppPreview
private fun Preview() {
    EveryweatherTheme {
        Box(modifier = Modifier.background(EveryweatherTheme.colors.primaryBackground.first())) {
            AppToolbar(title = "Title")
        }
    }
}