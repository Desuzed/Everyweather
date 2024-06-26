package com.desuzed.everyweather.presentation.features.weather_next_days

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import com.desuzed.everyweather.presentation.features.weather_next_days.ui.NextDaysBottomSheetContent
import com.desuzed.everyweather.util.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class NextDaysBottomSheet : BottomSheetDialogFragment() {
    private val viewModel by viewModel<NextDaysViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle(NextDaysState())
            NextDaysBottomSheetContent(state = state)
        }
        isNestedScrollingEnabled = true
    }
}