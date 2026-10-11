package com.desuzed.everyweather.presentation.features.in_app_update

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import com.desuzed.everyweather.presentation.features.in_app_update.ui.InAppUpdateContent
import com.desuzed.everyweather.ui.extensions.collectAsStateWithLifecycle
import com.desuzed.everyweather.util.collect
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class InAppUpdateBottomSheet : BottomSheetDialogFragment() {
    private val viewModel by viewModel<InAppUpdateViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle(InAppUpdateState())
            InAppUpdateContent(
                state = state,
                onAction = viewModel::onAction,
            )
        }
        isNestedScrollingEnabled = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onShown()
        collect(viewModel.sideEffect, ::onNewAction)
    }

    private fun onNewAction(action: InAppUpdateEffect) {
        when (action) {
            InAppUpdateEffect.Dismiss -> dismiss()
            InAppUpdateEffect.InstallUpdate -> {
                viewModel.completeUpdate()
                dismiss()
            }
            InAppUpdateEffect.UpdateApplication -> {
                viewModel.startUpdate(requireActivity())
                dismiss()
            }
        }
    }

}
