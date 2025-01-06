package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.desuzed.everyweather.R
import com.desuzed.everyweather.presentation.ui.base.DetailCard
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.detailCardMain

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
private fun TextPair(header: String, text: String) {
    SmallText(text = header)
    BoldText(text = text)
}

@Composable
private fun LeftColumn(detailCard: DetailCard, modifier: Modifier = Modifier) {
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
private fun RightColumn(detailCard: DetailCard, modifier: Modifier = Modifier) {
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
            header = stringResource(id = R.string.moonrise_moonset),
            text = detailCard.moon
        )
    }
}

@AppPreview
@Composable
private fun Preview() {
    EveryweatherTheme {
        CardDetailDayItem(detailCard = detailCardMain)
    }
}