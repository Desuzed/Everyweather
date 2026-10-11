package com.desuzed.everyweather.presentation.features.settings.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.desuzed.everyweather.domain.model.settings.DarkMode
import com.desuzed.everyweather.domain.model.settings.DistanceDimen
import com.desuzed.everyweather.domain.model.settings.Lang
import com.desuzed.everyweather.domain.model.settings.PressureDimen
import com.desuzed.everyweather.domain.model.settings.SettingsType
import com.desuzed.everyweather.domain.model.settings.TempDimen
import com.desuzed.everyweather.presentation.features.settings.SettingsAction
import com.desuzed.everyweather.presentation.ui.settings.DarkTheme
import com.desuzed.everyweather.presentation.ui.settings.Language
import com.desuzed.everyweather.presentation.ui.settings.Pressure
import com.desuzed.everyweather.presentation.ui.settings.SettingUiItem
import com.desuzed.everyweather.presentation.ui.settings.Temperature
import com.desuzed.everyweather.presentation.ui.settings.WindSpeed
import com.desuzed.everyweather.ui.elements.AppSheetDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDialog(
    showDialogType: SettingsType?,
    language: Language,
    windSpeed: WindSpeed,
    temperature: Temperature,
    darkTheme: DarkTheme,
    pressure: Pressure,
    langDialogItems: List<SettingUiItem<Lang>>,
    darkModeDialogItems: List<SettingUiItem<DarkMode>>,
    temperatureDialogItems: List<SettingUiItem<TempDimen>>,
    distanceDialogItems: List<SettingUiItem<DistanceDimen>>,
    pressureDialogItems: List<SettingUiItem<PressureDimen>>,
    onAction: (SettingsAction) -> Unit,
    onDismiss: () -> Unit,
) {
    if (showDialogType != null) {
        AppSheetDialog(onDismissRequest = onDismiss) {
            when (showDialogType) {
                SettingsType.LANG -> SettingPickerContent(
                    selectedItem = language,
                    radioOptions = langDialogItems,
                    onPicked = { onAction(SettingsAction.ChangeLanguage(it)) },
                )

                SettingsType.TEMP -> SettingPickerContent(
                    selectedItem = temperature,
                    radioOptions = temperatureDialogItems,
                    onPicked = { onAction(SettingsAction.ChangeTemperatureDimension(it)) },
                )

                SettingsType.DISTANCE -> SettingPickerContent(
                    selectedItem = windSpeed,
                    radioOptions = distanceDialogItems,
                    onPicked = { onAction(SettingsAction.ChangeDistanceDimension(it)) },
                )

                SettingsType.DARK_MODE -> SettingPickerContent(
                    selectedItem = darkTheme,
                    radioOptions = darkModeDialogItems,
                    onPicked = { onAction(SettingsAction.ChangeDarkMode(it)) },
                )

                SettingsType.PRESSURE -> SettingPickerContent(
                    selectedItem = pressure,
                    radioOptions = pressureDialogItems,
                    onPicked = { onAction(SettingsAction.ChangePressureDimension(it)) },
                )
            }
        }
    }
}
