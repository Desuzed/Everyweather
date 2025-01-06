package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.desuzed.everyweather.R
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.theming.EveryweatherTheme

@Composable
fun AppAlertDialog(title: String, onPositiveButtonClick: () -> Unit, onDismiss: () -> Unit) {
    AppDialog(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.dimen_20))
                .fillMaxWidth()
        ) {
            RegularText(text = title)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(id = R.dimen.dimen_20)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RegularText(text = stringResource(id = R.string.cancel), onClick = onDismiss)
                RoundedButton(
                    text = stringResource(id = R.string.ok),
                    onClick = onPositiveButtonClick
                )
            }
        }
    }
}

@Composable
fun AppDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier,
            color = EveryweatherTheme.colors.surfacePrimary,
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)),
        ) {
            content()
        }
    }
}

@AppPreview
@Composable
private fun PreviewAppAlertDialog() {
    EveryweatherTheme {
        AppAlertDialog("Delete", onPositiveButtonClick = {}, {})
    }
}