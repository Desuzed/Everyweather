package com.desuzed.everyweather.presentation.features.main_activity

import com.desuzed.everyweather.presentation.base.State
import com.desuzed.everyweather.util.Constants.EMPTY_STRING
import com.desuzed.everyweather.util.Constants.ZERO_LONG

data class MainActivityState(
    val lang: String = EMPTY_STRING,
    val isInternetUnavailable: Boolean = false,
    val isLookingForLocation: Boolean = false,
    val totalBytes: Long = ZERO_LONG,
    val bytesDownloaded: Long = ZERO_LONG,
    val isUpdateLoading: Boolean = false,
) : State