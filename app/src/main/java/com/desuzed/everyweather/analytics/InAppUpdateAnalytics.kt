package com.desuzed.everyweather.analytics

import com.desuzed.everyweather.analytics.update.UpdateTelemetry
import com.desuzed.everyweather.analytics.update.UpdateAnalytics
import com.desuzed.everyweather.domain.model.app_update.InAppUpdateStatus
import com.desuzed.everyweather.presentation.features.in_app_update.InAppUpdateAction

class InAppUpdateAnalytics(private val telemetry: UpdateTelemetry) {
    private val analytics = UpdateAnalytics(telemetry)

    fun onShown(status: InAppUpdateStatus?) {
        analytics.dialogShown(status)
    }

    fun onAction(action: InAppUpdateAction) {
        when (action) {
            InAppUpdateAction.AgreedToInstallUpdate -> analytics.installClicked()
            InAppUpdateAction.AgreedToUpdate -> analytics.updateClicked()
            InAppUpdateAction.Dismiss -> analytics.dialogDismissed()
        }
    }
}
