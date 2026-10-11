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
import com.desuzed.everyweather.ui.elements.AppDialog
import com.desuzed.everyweather.ui.elements.AppSheetDialog

//TODO: Переделать на боттомшиты
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
    when (showDialogType) {
        SettingsType.LANG -> AppDialog(onDismiss = onDismiss) {
            AppDialog(onDismiss = onDismiss) {
                SettingPickerContent(
                    selectedItem = language,
                    radioOptions = langDialogItems
                ) {
                    onAction(
                        SettingsAction.ChangeLanguage(lang = it)
                    )
                }
            }
        }

        SettingsType.TEMP -> AppDialog(onDismiss = onDismiss) {
            AppSheetDialog(
                onDismissRequest = onDismiss,
                content = {
                    SettingPickerContent(
                        selectedItem = temperature,
                        radioOptions = temperatureDialogItems
                    ) {
                        onAction(
                            SettingsAction.ChangeTemperatureDimension(tempDimen = it)
                        )
                    }
                },
            )
//            AppDialog(onDismiss = onDismiss) {
//                SettingPickerContent(
//                    selectedItem = temperature,
//                    radioOptions = temperatureDialogItems
//                ) {
//                    onAction(
//                        SettingsAction.ChangeTemperatureDimension(tempDimen = it)
//                    )
//                }
//            }
        }

        SettingsType.DISTANCE -> AppDialog(onDismiss = onDismiss) {
            AppDialog(onDismiss = onDismiss) {
                SettingPickerContent(
                    selectedItem = windSpeed,
                    radioOptions = distanceDialogItems
                ) {
                    onAction(
                        SettingsAction.ChangeDistanceDimension(distanceDimen = it)
                    )
                }
            }
        }

        SettingsType.DARK_MODE -> AppDialog(onDismiss = onDismiss) {
            AppDialog(onDismiss = onDismiss) {
                SettingPickerContent(
                    selectedItem = darkTheme,
                    radioOptions = darkModeDialogItems
                ) {
                    onAction(
                        SettingsAction.ChangeDarkMode(darkMode = it)
                    )
                }
            }
        }

        SettingsType.PRESSURE -> {
            AppDialog(onDismiss = onDismiss) {
                SettingPickerContent(
                    selectedItem = pressure,
                    radioOptions = pressureDialogItems
                ) {
                    onAction(
                        SettingsAction.ChangePressureDimension(
                            pressureDimen = it
                        )
                    )
                }
            }
        }

        null -> {}
    }
}