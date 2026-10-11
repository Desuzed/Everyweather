package com.desuzed.everyweather.presentation.features.in_app_update

import android.app.Activity
import com.desuzed.everyweather.analytics.InAppUpdateAnalytics
import com.desuzed.everyweather.data.repository.providers.app_update.AppUpdateProvider
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.presentation.base.BaseViewModel

class InAppUpdateViewModel(
    private val status: InAppUpdateStatus?,
    private val analytics: InAppUpdateAnalytics,
    private val appUpdateProvider: AppUpdateProvider,
) :
    BaseViewModel<InAppUpdateState, InAppUpdateEffect, InAppUpdateAction>(InAppUpdateState(status)) {

    private var shown = false

    fun onShown() {
        if (shown) return
        shown = true
        analytics.onShown(status)
    }

    override fun onAction(action: InAppUpdateAction) {
        analytics.onAction(action)
        when (action) {
            InAppUpdateAction.AgreedToInstallUpdate -> setSideEffect(InAppUpdateEffect.InstallUpdate)
            InAppUpdateAction.AgreedToUpdate -> setSideEffect(InAppUpdateEffect.UpdateApplication)
            InAppUpdateAction.Dismiss -> setSideEffect(InAppUpdateEffect.Dismiss)
        }
    }

    fun startUpdate(activity: Activity) {
        appUpdateProvider.startUpdate(activity)
    }

    fun completeUpdate() {
        appUpdateProvider.completeUpdate()
    }
}
