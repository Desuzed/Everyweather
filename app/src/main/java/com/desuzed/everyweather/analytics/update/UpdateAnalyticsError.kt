package com.desuzed.everyweather.analytics.update

import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.ErrorNames
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.ErrorCodes
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.MAX_PARAMETER_VALUE_LENGTH
import com.desuzed.everyweather.analytics.update.UpdateAnalyticsContract.Params
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.model.InstallErrorCode

/** Only stable error metadata is exported; exception messages and stacks stay out of Analytics. */
data class UpdateAnalyticsError(
    val stage: UpdateStage,
    val name: String,
    val code: Int? = null,
    val exceptionType: String? = null,
    val resultCode: Int? = null,
) {
    fun toParameters(): Map<String, String> = buildMap {
        put(Params.STAGE, stage.parameterValue)
        put(Params.ERROR_NAME, name)
        code?.let { put(Params.ERROR_CODE, it.toString()) }
        exceptionType?.let { put(Params.EXCEPTION_TYPE, it.take(MAX_PARAMETER_VALUE_LENGTH)) }
        resultCode?.let { put(Params.RESULT_CODE, it.toString()) }
    }

    companion object {
        fun fromException(stage: UpdateStage, error: Exception): UpdateAnalyticsError {
            val result = if (error is InstallException) fromInstallCode(stage, error.errorCode)
                else UpdateAnalyticsError(stage, ErrorNames.EXCEPTION)
            return result.copy(exceptionType = error.javaClass.simpleName)
        }

        fun fromInstallCode(stage: UpdateStage, code: Int) = UpdateAnalyticsError(
            stage = stage,
            name = when (code) {
                InstallErrorCode.NO_ERROR -> ErrorNames.NO_ERROR
                InstallErrorCode.ERROR_UNKNOWN -> ErrorNames.UNKNOWN
                InstallErrorCode.ERROR_API_NOT_AVAILABLE -> ErrorNames.API_NOT_AVAILABLE
                InstallErrorCode.ERROR_INVALID_REQUEST -> ErrorNames.INVALID_REQUEST
                InstallErrorCode.ERROR_INSTALL_UNAVAILABLE -> ErrorNames.INSTALL_UNAVAILABLE
                InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED -> ErrorNames.INSTALL_NOT_ALLOWED
                InstallErrorCode.ERROR_DOWNLOAD_NOT_PRESENT -> ErrorNames.DOWNLOAD_NOT_PRESENT
                ErrorCodes.INSTALL_IN_PROGRESS -> ErrorNames.INSTALL_IN_PROGRESS
                InstallErrorCode.ERROR_PLAY_STORE_NOT_FOUND -> ErrorNames.PLAY_STORE_NOT_FOUND
                InstallErrorCode.ERROR_APP_NOT_OWNED -> ErrorNames.APP_NOT_OWNED
                InstallErrorCode.ERROR_INTERNAL_ERROR -> ErrorNames.INTERNAL_ERROR
                else -> ErrorNames.UNRECOGNIZED
            },
            code = code,
        )
    }
}
