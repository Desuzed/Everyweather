package com.desuzed.everyweather.analytics.update

/** Stable Firebase wire names. Changing these splits existing reports. */
object UpdateAnalyticsContract {
    const val MAX_PARAMETER_VALUE_LENGTH = 100

    object Events {
        const val INSTALL_SOURCE = "app_install_source"
        const val CHECK = "in_app_update_check"
        const val DIALOG_SHOWN = "in_app_update_dialog_shown"
        const val DIALOG_DISMISSED = "in_app_update_dialog_dismissed"
        const val UPDATE_CLICKED = "in_app_update_start"
        const val INSTALL_CLICKED = "in_app_update_install"
        const val SETTINGS_UPDATE_OPENED = "settings_launch_update"
        const val SETTINGS_INSTALL_OPENED = "settings_install_update"
        const val FLOW_LAUNCHED = "in_app_update_flow_launched"
        const val CONSENT = "in_app_update_consent"
        const val DOWNLOAD_STARTED = "in_app_update_download_started"
        const val DOWNLOADED = "in_app_update_downloaded"
        const val DOWNLOAD_CANCELED = "in_app_update_download_canceled"
        const val INSTALL_CANCELED = "in_app_update_install_canceled"
        const val INSTALL_REQUESTED = "in_app_update_install_requested"
        const val INSTALL_ACCEPTED = "in_app_update_install_accepted"
        const val INSTALL_STATUS_INSTALLED = "in_app_update_install_status_installed"
        const val VERSION_OBSERVED = "in_app_update_version_observed"
        const val INSTALL_PENDING = "in_app_update_install_pending"
        const val INSTALL_UNCONFIRMED = "in_app_update_install_unconfirmed"
        const val ERROR = "in_app_update_error"
    }

    object Params {
        const val INSTALL_SOURCE = "install_source"
        const val INSTALLER_PACKAGE = "installer_package"
        const val INSTALL_SOURCE_STATUS = "install_source_status"
        const val UPDATE_CHANNEL = "update_channel"
        const val CURRENT_VERSION = "current_version"
        const val TARGET_VERSION = "target_version"
        const val FROM_VERSION = "from_version"
        const val REQUESTED_VERSION = "requested_version"
        const val ORIGIN_INSTALL_SOURCE = "origin_install_source"
        const val ORIGIN_INSTALLER_PACKAGE = "origin_installer_package"
        const val EVIDENCE = "evidence"
        const val REASON = "reason"
        const val MATCHES_TARGET = "matches_target"
        const val STAGE = "stage"
        const val ERROR_NAME = "error_name"
        const val ERROR_CODE = "error_code"
        const val EXCEPTION_TYPE = "exception_type"
        const val RESULT_CODE = "result_code"
        const val OUTCOME = "outcome"
        const val RESUMED = "resumed"
        const val DIALOG_STATUS = "dialog_status"
    }

    object Values {
        const val UNKNOWN = "unknown"
        const val TRUE = "true"
        const val GOOGLE_PLAY_CHANNEL = "google_play"
        const val AFTER_INSTALL_REQUEST = "after_install_request"
        const val EXPIRED = "expired"
        const val VERSION_ROLLBACK = "version_rollback"
        const val READY_TO_LAUNCH_UPDATE = "ready_to_launch_update"
        const val READY_TO_INSTALL = "ready_to_install"
    }

    object Sources {
        const val GOOGLE_PLAY = "google_play"
        const val GALAXY_STORE = "galaxy_store"
        const val APP_GALLERY = "app_gallery"
        const val AMAZON = "amazon"
        const val PACKAGE_INSTALLER = "package_installer"
        const val OTHER = "other"
        const val UNKNOWN = Values.UNKNOWN
    }

    object SourceStatus {
        const val KNOWN = "known"
        const val UNKNOWN = Values.UNKNOWN
        const val ERROR = "error"
    }

    object InstallerPackages {
        const val GOOGLE_PLAY = "com.android.vending"
        const val GALAXY_STORE = "com.sec.android.app.samsungapps"
        const val APP_GALLERY = "com.huawei.appmarket"
        const val AMAZON = "com.amazon.venezia"
        const val ANDROID = "com.android.packageinstaller"
        const val GOOGLE = "com.google.android.packageinstaller"
        const val MIUI = "com.miui.packageinstaller"
    }

    object ErrorNames {
        const val NO_ERROR = "no_error"
        const val UNKNOWN = Values.UNKNOWN
        const val API_NOT_AVAILABLE = "api_not_available"
        const val INVALID_REQUEST = "invalid_request"
        const val INSTALL_UNAVAILABLE = "install_unavailable"
        const val INSTALL_NOT_ALLOWED = "install_not_allowed"
        const val DOWNLOAD_NOT_PRESENT = "download_not_present"
        const val INSTALL_IN_PROGRESS = "install_in_progress"
        const val PLAY_STORE_NOT_FOUND = "play_store_not_found"
        const val APP_NOT_OWNED = "app_not_owned"
        const val INTERNAL_ERROR = "internal_error"
        const val UNRECOGNIZED = "unrecognized"
        const val EXCEPTION = "exception"
        const val MISSING_UPDATE_INFO = "missing_update_info"
        const val FLOW_NOT_STARTED = "flow_not_started"
        const val FLOW_FAILED = "flow_failed"
        const val READ_FAILED = "read_failed"
        const val WRITE_FAILED = "write_failed"
    }

    object ErrorCodes {
        // Documented Play code; app-update 2.1.0 does not expose a named constant for it.
        const val INSTALL_IN_PROGRESS = -8
    }
}

enum class UpdateStage(val parameterValue: String) {
    CHECK("check"), LAUNCH("launch"), CONSENT("consent"),
    DOWNLOAD("download"), INSTALL("install"), TELEMETRY_STORAGE("telemetry_storage"),
}

enum class UpdateCheckOutcome(val parameterValue: String) {
    STARTED("started"), AVAILABLE("available"), UNAVAILABLE("unavailable"),
    NOT_ALLOWED("not_allowed"), DOWNLOADING("downloading"), DOWNLOADED("downloaded"),
}

enum class UpdateConsentOutcome(val parameterValue: String) {
    ACCEPTED("accepted"), CANCELED("canceled"), FAILED("failed"),
}
