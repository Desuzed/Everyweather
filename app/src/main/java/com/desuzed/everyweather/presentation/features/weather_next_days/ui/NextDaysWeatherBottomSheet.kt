package com.desuzed.everyweather.presentation.features.weather_next_days.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.desuzed.everyweather.presentation.ui.next_days.NextDaysUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextDaysWeatherBottomSheet(
    sheetState: SheetState,
    dayItem: NextDaysUi?,
    onBackClick: () -> Unit,
) {
    if (sheetState.isVisible && dayItem != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = null,
            onDismissRequest = onBackClick,
            content = {
                NextDayBottomSheetContent(dayItem = dayItem)
            }
        )
    }
}