package com.desuzed.everyweather.ui.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.desuzed.everyweather.R
import com.desuzed.everyweather.data.repository.providers.ActionResultProviderFactory
import com.desuzed.everyweather.domain.model.result.ActionType
import com.desuzed.everyweather.domain.model.result.QueryResult
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider
import com.desuzed.everyweather.ui.AppPreview
import com.desuzed.everyweather.ui.extensions.bottomEdgeToEdgeImePadding
import com.desuzed.everyweather.ui.theming.EveryweatherTheme
import com.desuzed.everyweather.util.Constants.EMPTY_STRING
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

//TODO попробовать отказатться от снекбаров вообще в пользу боттомшитов т.к очень сильно всё усложняется из за них и они имеют ограниченный функуционал
const val TOO_LONG_STRING_LENGTH = 60

@Composable
fun BoxScope.AppSnackbar(
    modifier: Modifier = Modifier,
    snackbarState: SnackbarHostState,
) {
    SnackbarHost(
        modifier = modifier
            .bottomEdgeToEdgeImePadding()
            .align(Alignment.BottomCenter),
        hostState = snackbarState,
    ) { snackbarData ->
        Snackbar(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.dimen_20)),
            containerColor = EveryweatherTheme.colors.onBackgroundPrimary,
            contentColor = EveryweatherTheme.colors.onBackgroundInvariant,
            actionOnNewLine = snackbarData.visuals.message.length > TOO_LONG_STRING_LENGTH,
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.dimen_16)),
            action = if (snackbarData.visuals.actionLabel != null) {
                {
                    AppTextButton(
                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dimen_10)),
                        text = snackbarData.visuals.actionLabel ?: EMPTY_STRING,
                        color = EveryweatherTheme.colors.primaryInvariant,
                        onClick = {
                            snackbarData.performAction()
                        },
                    )
                }
            } else {
                null
            },
        ) {
            RegularText(
                modifier = Modifier,
                text = snackbarData.visuals.message,
                textAlign = TextAlign.Start,
                color = EveryweatherTheme.colors.onBackgroundInvariant,
            )
        }
    }
}


@Composable
fun <P : ActionResultProvider> CollectSnackbar(
    queryResult: QueryResult?,
    snackbarState: SnackbarHostState,
    providerClass: KClass<P>,
    onRetryClick: () -> Unit,
) {
    val provider = getProvider(providerClass = providerClass)
    val onClick: () -> Unit
    val buttonTextId: Int

    when (queryResult?.actionType) {
        ActionType.RETRY -> {
            buttonTextId = R.string.retry
            onClick = onRetryClick
        }

        else -> {
            onClick = {}
            buttonTextId = R.string.ok
        }
    }
    val buttonText = stringResource(id = buttonTextId)
    LaunchedEffect(key1 = queryResult?.id) {
        if (queryResult != null) {
            launch {
                val message = provider.parseCode(queryResult.code, queryResult.query)

                val result = snackbarState.showSnackbar(
                    message = message,
                    actionLabel = buttonText,
                    duration = if (message.length > TOO_LONG_STRING_LENGTH) {
                        SnackbarDuration.Long
                    } else {
                        SnackbarDuration.Short
                    },
                )
                when (result) {
                    SnackbarResult.Dismissed -> {}
                    SnackbarResult.ActionPerformed -> onClick()
                }
            }
        }

    }
}

@Composable
private fun <P : ActionResultProvider> getProvider(providerClass: KClass<P>): ActionResultProvider {
    val resources = LocalContext.current.resources

    return ActionResultProviderFactory.provide(providerClass, resources)
}

/**
 * Чтобы увидеть превью нажми "start interactive mode"
 * */
@AppPreview
@Composable
private fun AppSnackbarPreview() {
    EveryweatherTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(key1 = Unit) {
            launch {
                snackbarHostState.showSnackbar(
                    message = "12345",
                    actionLabel = "77777",
                    duration = SnackbarDuration.Indefinite
                )
            }
        }
        Box {
            AppSnackbar(
                snackbarState = snackbarHostState,
            )
        }
    }
}
