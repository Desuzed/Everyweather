package com.desuzed.everyweather.presentation.features.in_app_update

import com.desuzed.everyweather.analytics.InAppUpdateAnalytics
import com.desuzed.everyweather.data.repository.providers.app_update.AppUpdateProvider
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.presentation.base.BaseViewModel

class InAppUpdateViewModel(
    status: InAppUpdateStatus?,
    private val analytics: InAppUpdateAnalytics,
    private val appUpdateProvider: AppUpdateProvider,
) :
    BaseViewModel<InAppUpdateState, InAppUpdateEffect, InAppUpdateAction>(InAppUpdateState(status)) {

    override fun onAction(action: InAppUpdateAction) {
        analytics.onAction(action)
        when (action) {
            InAppUpdateAction.AgreedToInstallUpdate -> {
                //todo appUpdateProvider.completeUpdate()
                dismiss()
            }
            InAppUpdateAction.AgreedToUpdate -> {
                // todo appUpdateProvider.startUpdate(activity)
                dismiss()
            }
            InAppUpdateAction.Dismiss -> dismiss()
        }
        setSideEffect(InAppUpdateEffect.Dismiss)
    }

    private fun dismiss() {
        setSideEffect(InAppUpdateEffect.Dismiss)
    }

//    fun resolveStatus(status: InAppUpdateStatus) {
//        setState { copy(updateStatus = status) }
//    }
}