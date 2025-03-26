package com.desuzed.everyweather.presentation.features.in_app_update.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.desuzed.everyweather.R
import com.desuzed.everyweather.presentation.features.in_app_update.InAppUpdateAction
import com.desuzed.everyweather.presentation.features.in_app_update.InAppUpdateState
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.elements.BoldText
import com.desuzed.everyweather.ui.elements.RegularText
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.Constants.EMPTY_STRING
import com.desuzed.everyweather.util.Constants.ONE_FLOAT

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        InAppUpdateContent(InAppUpdateState()) {}
    }
}

@Composable
fun InAppUpdateContent(
    state: InAppUpdateState,
    onAction: (InAppUpdateAction) -> Unit,
) {
    Surface(
        modifier = Modifier.height(dimensionResource(id = R.dimen.dimen_350)),
        shape = RoundedCornerShape(
            topStart = dimensionResource(id = R.dimen.corner_radius_30),
            topEnd = dimensionResource(id = R.dimen.corner_radius_30)
        ),
        color = EveryweatherTheme.colors.tertiaryBackground,
    ) {
        val inAppUpdateUiParams = InAppUpdateUiParams.fromInAppUpdateStatus(
            status = state.updateStatus!!, //todo
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    vertical = dimensionResource(id = R.dimen.dimen_20),
                    horizontal = dimensionResource(id = R.dimen.dimen_10),
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BoldText(text = stringResource(id = inAppUpdateUiParams.titleId))
                RegularText(
                    text = stringResource(id = inAppUpdateUiParams.descriptionId),
                    modifier = Modifier.padding(
                        top = dimensionResource(id = R.dimen.dimen_20)
                    )
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_weather_splash),
                contentDescription = EMPTY_STRING,
                modifier = Modifier
                    .weight(ONE_FLOAT)
                    .padding(
                        vertical = dimensionResource(id = R.dimen.dimen_10)
                    )
            )
            InAppUpdateButtonsRow(inAppUpdateUiParams, onAction)
        }
    }
}