package com.desuzed.everyweather.presentation.features.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.presentation.features.settings.SettingsAction
import com.desuzed.everyweather.presentation.ui.settings.SettingsMapper
import com.desuzed.everyweather.presentation.ui.settings.SettingsUiParams
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.elements.BoldText
import com.desuzed.everyweather.ui.elements.GradientBox
import com.desuzed.everyweather.ui.elements.LargeBoldText
import com.desuzed.everyweather.ui.extensions.topEdgeToEdgePadding
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.Constants.EMPTY_STRING

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        SettingsScreenBody(
            settingsParams = SettingsMapper.getSettingsUiParams(
                distanceDimenList = DistanceDimen.entries,
                tempList = TempDimen.entries,
                pressureList = PressureDimen.entries,
                langList = Lang.entries,
                darkModeList = DarkMode.entries,
                selectedMode = DarkMode.SYSTEM,
                selectedLang = Lang.EN,
                selectedDistanceDimen = DistanceDimen.METRIC_MS,
                selectedTempDimen = TempDimen.CELCIUS,
                selectedPressureDimen = PressureDimen.INCHES,
            ),
            updateStatus = null,
            onAction = {},
        )
    }
}

@Composable
fun SettingsScreenBody(
    settingsParams: SettingsUiParams,
    updateStatus: InAppUpdateStatus?,
    onAction: (SettingsAction) -> Unit,
) {
    GradientBox(
        modifier = Modifier.fillMaxSize(),
        colors = EveryweatherTheme.colors.primaryBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .topEdgeToEdgePadding()
                .padding(dimensionResource(id = R.dimen.dimen_10))
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dimen_10))
            ) {
                LargeBoldText(
                    text = stringResource(id = R.string.settings),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                IconButton(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.dimen_34))
                        .padding(start = dimensionResource(id = R.dimen.dimen_10)),
                    onClick = { onAction(SettingsAction.OnBackClick) },
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_arrow_back),
                            contentDescription = EMPTY_STRING,
                            tint = EveryweatherTheme.colors.onBackgroundPrimary
                        )
                    },
                )
            }
            BoldText(
                text = stringResource(id = R.string.app_settings),
                modifier = Modifier.padding(
                    top = dimensionResource(id = R.dimen.dimen_10),
                    start = dimensionResource(id = R.dimen.dimen_10)
                ),
            )
            SettingsMenuGroupContent(
                onAction = onAction,
                items = listOf(
                    settingsParams.selectedLang,
                    settingsParams.selectedMode,
                ),
            )
            BoldText(
                text = stringResource(id = R.string.dimension_settings),
                modifier = Modifier.padding(
                    top = dimensionResource(id = R.dimen.dimen_10),
                    start = dimensionResource(id = R.dimen.dimen_10)
                ),
            )
            SettingsMenuGroupContent(
                onAction = onAction,
                items = listOf(
                    settingsParams.selectedTemp,
                    settingsParams.selectedDistance,
                    settingsParams.selectedPressure,
                ),
            )
            SettingsAppUpdateContent(updateStatus, onAction)
        }
    }
}
