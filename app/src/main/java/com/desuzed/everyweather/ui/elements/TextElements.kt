package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.desuzed.everyweather.R
import com.desuzed.everyweather.presentation.features.weather_main.WeatherAction
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.Constants.EMPTY_STRING

@Composable
fun BoldText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        modifier = modifier.clickable(
            onClick = onClick,
            indication = null,
            interactionSource = interactionSource,
        ),
        text = text,
        style = EveryweatherTheme.typography.h3,
        color = EveryweatherTheme.colors.onBackgroundPrimary,
        textAlign = textAlign,
    )
}

@Composable
fun RegularText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = EveryweatherTheme.colors.onBackgroundPrimary,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    maxLines: Int = Int.MAX_VALUE,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        modifier = modifier.clickable(
            onClick = onClick,
            indication = null,
            interactionSource = interactionSource,
        ),
        text = text,
        style = EveryweatherTheme.typography.text,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun MediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = EveryweatherTheme.colors.onBackgroundPrimary,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    maxLines: Int = 1,
    textAlign: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text,
        style = EveryweatherTheme.typography.textMedium,
        color = color,
        overflow = overflow,
        maxLines = maxLines,
        textAlign = textAlign,
    )
}

@Composable
fun MediumBoldText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    Text(
        modifier = modifier,
        text = text,
        style = EveryweatherTheme.typography.textMediumBold,
        color = color,
        overflow = overflow,
    )
}

@Composable
fun SmallText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        style = EveryweatherTheme.typography.textSmall,
        maxLines = 1,
        color = EveryweatherTheme.colors.onBackgroundPrimary
    )
}

@Composable
fun UltraLargeBoldText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = EveryweatherTheme.colors.onBackgroundPrimary
) {
    Text(
        modifier = modifier,
        text = text,
        style = EveryweatherTheme.typography.textUltraLarge,
        maxLines = 1,
        color = color,
    )
}

@Composable
fun LargeBoldText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = EveryweatherTheme.colors.onBackgroundPrimary,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text,
        style = EveryweatherTheme.typography.textBoldLarge,
        maxLines = 1,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
    )
}

@Composable
fun HintEditText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = EveryweatherTheme.colors.neutral,
) {
    Text(
        modifier = modifier,
        text = text,
        style = EveryweatherTheme.typography.textMedium,
        color = color,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
    )
}

@Composable
fun DelimiterText() {
    Text(
        text = stringResource(id = R.string.delimiter),
        style = EveryweatherTheme.typography.textLarge,
        color = EveryweatherTheme.colors.onBackgroundSecondary
    )
}

@Composable
fun LocationText(text: String, onAction: (WeatherAction) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.dimen_4)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)),
        backgroundColor = EveryweatherTheme.colors.surfaceOnSecondaryBg,
        elevation = dimensionResource(id = R.dimen.dimen_0),
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = dimensionResource(id = R.dimen.dimen_8),
                    vertical = dimensionResource(id = R.dimen.dimen_4)
                )
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onAction(WeatherAction.Location) },
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MediumText(
                modifier = Modifier.weight(1f),
                text = text,
                overflow = TextOverflow.Ellipsis,
                color = EveryweatherTheme.colors.onBackgroundPrimary
            )
            Image(
                modifier = Modifier,
                painter = painterResource(id = R.drawable.ic_edit_location),
                colorFilter = ColorFilter.tint(EveryweatherTheme.colors.onBackgroundPrimary),
                contentDescription = EMPTY_STRING,
            )
        }

    }
}

@Composable
fun LinkText(
    modifier: Modifier,
    inputText: String,
    url: String,
    startIndex: Int,
    endIndex: Int,
    style: TextStyle = EveryweatherTheme.typography.textMediumAnnotated,
    spannableStringColor: Color = EveryweatherTheme.colors.primary,
    onClick: () -> Unit
) {
    val mAnnotatedLinkString = buildAnnotatedString {
        append(inputText)
        addStyle(
            style = SpanStyle(
                color = spannableStringColor,
                textDecoration = TextDecoration.Underline
            ), start = startIndex, end = endIndex
        )
        addStringAnnotation(
            tag = "URL",
            annotation = url,
            start = startIndex,
            end = endIndex
        )
    }
    val uriHandler = LocalUriHandler.current
    ClickableText(
        text = mAnnotatedLinkString,
        modifier = modifier,
        style = style,
        onClick = {
            mAnnotatedLinkString
                .getStringAnnotations("URL", it, it)
                .firstOrNull()?.let { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                    onClick.invoke()
                }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OutlinedIconEditText(
    text: String,
    hint: String,
    isLoading: Boolean,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    iconResId: Int,
    maxLines: Int = 1,
    accentColor: Color = EveryweatherTheme.colors.primary,
    textColor: Color = EveryweatherTheme.colors.neutral,
    backgroundColor: Color = Color.Transparent,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = EveryweatherTheme.colors.onBackgroundPrimary,
        focusedBorderColor = EveryweatherTheme.colors.neutral,
        unfocusedBorderColor = EveryweatherTheme.colors.neutral,
        cursorColor = EveryweatherTheme.colors.primary,
        backgroundColor = backgroundColor
    ),
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
    onIconClick: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isIconButtonEnabled by remember { mutableStateOf(text.trim().isNotEmpty()) }

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = text,
        onValueChange = {
            isIconButtonEnabled = it.trim().isNotEmpty()
            onTextChanged(it)
        },
        maxLines = maxLines,
        textStyle = EveryweatherTheme.typography.text,
        placeholder = {
            HintEditText(text = hint, color = textColor)
        },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_16)),
        colors = colors,
        trailingIcon = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensionResource(id = R.dimen.dimen_26)),
                    strokeWidth = dimensionResource(id = R.dimen.dimen_3),
                    color = EveryweatherTheme.colors.primary
                )
            } else {
                IconButton(
                    onClick = onIconClick,
                    enabled = isIconButtonEnabled,
                    content = {
                        Icon(
                            painter = painterResource(id = iconResId),
                            contentDescription = null,
                            tint = if (isIconButtonEnabled) {
                                accentColor
                            } else {
                                EveryweatherTheme.colors.neutral
                            }
                        )
                    }
                )
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onSearch = {
            keyboard?.hide()
            focusManager.clearFocus()
            if (text.trim().isNotEmpty()) onIconClick()
        })
    )
}

//todo previews