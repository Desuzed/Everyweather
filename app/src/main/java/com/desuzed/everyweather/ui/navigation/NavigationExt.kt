package com.desuzed.everyweather.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.desuzed.everyweather.presentation.features.main_activity.MainActivity

@Composable
@ReadOnlyComposable
fun getMainActivity(): MainActivity {
    return requireNotNull(LocalActivity.current as? MainActivity) {
        "Everyweather navigation must be hosted by MainActivity"
    }
}

fun <T> NavController.setResult(key: String, value: T) {
    currentBackStackEntry
        ?.savedStateHandle
        ?.set(key, value)
}

fun <T> NavBackStackEntry.getResult(key: String): T? {
    return savedStateHandle.remove(key)
}
