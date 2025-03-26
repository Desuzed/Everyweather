package com.desuzed.everyweather.presentation.features.in_app_update

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.presentation.base.BaseComposeScreen
import com.desuzed.everyweather.presentation.features.in_app_update.ui.InAppUpdateContent
import com.desuzed.everyweather.ui.navigation.Destination
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

object InAppUpdateScreen : BaseComposeScreen<
        InAppUpdateState,
        InAppUpdateEffect,
        InAppUpdateAction,
        InAppUpdateViewModel>(
    initialState = InAppUpdateState()
) {

    override val destination: Destination
        get() = Destination.InAppUpdateScreen

    @Composable
    override fun ProvideScreenForNavGraph(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        val inAppUpdateStatus = remember { getStatusArgument(navBackStackEntry)  }
        Log.e("TAG", "ProvideScreenForNavGraph: inAppUpdateStatus = $inAppUpdateStatus ${navBackStackEntry.arguments}", )
        InAppUpdateComposeScreen(
            navController = navController,
            inAppUpdateStatus = inAppUpdateStatus,
        )
    }

    @Composable
    private fun InAppUpdateComposeScreen(
        navController: NavHostController,
        inAppUpdateStatus: InAppUpdateStatus?,
        viewModel: InAppUpdateViewModel = koinViewModel(parameters = { parametersOf(inAppUpdateStatus) })
    ) {
        ComposeScreen(
            viewModel = viewModel,
            backAction = null, //todo
            onEffect = {
                when (it) {
                    InAppUpdateEffect.Dismiss -> navController.popBackStack()
                    InAppUpdateEffect.InstallUpdate -> TODO()
                    InAppUpdateEffect.UpdateApplication -> TODO()
                }
            },
            content = { state ->
                InAppUpdateContent(
                    state = state,
                    onAction = viewModel::onAction,
                )
            },
        )
    }

    private fun getStatusArgument(navBackStackEntry: NavBackStackEntry): InAppUpdateStatus? {
        val arg = navBackStackEntry.arguments?.getString("InAppUpdateStatus")?.also {
            Log.e("TAG", "getStatusArgument: $it", )
        } ?: return null
        return try {
            InAppUpdateStatus.valueOf(arg)
        } catch (e: Exception) {
            null
        }
    }
}