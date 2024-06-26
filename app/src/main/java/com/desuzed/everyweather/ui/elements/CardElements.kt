package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.desuzed.everyweather.R
import com.desuzed.everyweather.presentation.ui.base.DetailCard
import com.desuzed.everyweather.ui.theming.EveryweatherTheme

@Composable
fun CardDetailDayItem(detailCard: DetailCard, modifier: Modifier = Modifier) {
    RoundedCardItem(modifier = modifier) {
        Row(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.dimen_20)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LeftColumn(detailCard = detailCard)
            RightColumn(detailCard = detailCard)
        }
    }
}

@Composable
fun LeftColumn(detailCard: DetailCard, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_4)),
        horizontalAlignment = Alignment.Start
    ) {
        TextPair(
            header = stringResource(id = R.string.humidity),
            text = detailCard.humidity
        )
        TextPair(
            header = stringResource(id = R.string.pressure),
            text = detailCard.pressure
        )
        TextPair(
            header = stringResource(id = R.string.sunrise_sunset),
            text = detailCard.sun
        )
    }
}

@Composable
fun RightColumn(detailCard: DetailCard, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dimen_4)),
        horizontalAlignment = Alignment.End
    ) {
        TextPair(
            header = stringResource(id = R.string.probability_of_precipitation),
            text = detailCard.pop
        )
        TextPair(header = stringResource(id = R.string.wind), text = detailCard.wind)
        TextPair(
            modifier = modifier.align(Alignment.End),
            header = stringResource(id = R.string.moonrise_moonset),
            text = detailCard.moon
        )
    }
}

@Composable
fun RoundedCardItem(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)),
        backgroundColor = EveryweatherTheme.colors.surfacePrimary,
        elevation = dimensionResource(id = R.dimen.dimen_4),
    ) {
        content()
    }
}