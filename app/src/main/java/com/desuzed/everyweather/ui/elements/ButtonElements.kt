package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.desuzed.everyweather.R
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.Constants

@Composable
fun FloatingButton(modifier: Modifier = Modifier, id: Int, onClick: () -> Unit) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = EveryweatherTheme.colors.primary,
    ) {
        Image(
            painter = painterResource(id),
            colorFilter = ColorFilter.tint(EveryweatherTheme.colors.onPrimary),
            contentDescription = Constants.EMPTY_STRING,
        )
    }
}

@Composable
fun RoundedButton(modifier: Modifier = Modifier, onClick: () -> Unit, text: String) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = EveryweatherTheme.colors.primary
        )
    )
    {
        MediumText(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dimen_4)),
            text = text,
            color = EveryweatherTheme.colors.onPrimary
        )
    }
}

@Composable
fun AppRadioButton(modifier: Modifier = Modifier, isSelected: Boolean, onClick: () -> Unit) {
    RadioButton(
        modifier = modifier,
        colors = RadioButtonDefaults.colors(
            selectedColor = EveryweatherTheme.colors.primary,
            unselectedColor = EveryweatherTheme.colors.primary,
            disabledColor = EveryweatherTheme.colors.onPrimary,
        ),
        selected = isSelected,
        onClick = onClick
    )
}

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FloatingButton(
                id = R.drawable.ic_my_location,
                onClick = {},
            )
            RoundedButton(modifier = Modifier.fillMaxWidth(), text = "text", onClick = {})
            AppRadioButton(isSelected = false, onClick = {})
            AppRadioButton(isSelected = true, onClick = {})
        }
    }
}