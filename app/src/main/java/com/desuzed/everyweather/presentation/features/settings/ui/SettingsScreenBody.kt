package com.desuzed.everyweather.presentation.features.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
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
import com.desuzed.everyweather.ui.elements.AppToolbar
import com.desuzed.everyweather.ui.elements.BoldText
import com.desuzed.everyweather.ui.elements.GradientBox
import com.desuzed.everyweather.ui.extensions.topEdgeToEdgePadding
import com.desuzed.everyweather.ui.theming.EveryweatherTheme

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
            AppToolbar(
                title = stringResource(id = R.string.settings),
                onStartIconClick = { onAction(SettingsAction.OnBackClick) },
            )
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
