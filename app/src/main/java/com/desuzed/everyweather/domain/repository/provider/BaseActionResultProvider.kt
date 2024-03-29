package com.desuzed.everyweather.domain.repository.provider

import android.content.res.Resources
import com.desuzed.everyweather.R
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider.Companion.DELETED
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider.Companion.NO_DATA
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider.Companion.NO_INTERNET
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider.Companion.REDIRECTION
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider.Companion.SAVED
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider.Companion.UNKNOWN
import com.desuzed.everyweather.domain.repository.provider.ActionResultProvider.Companion.UPDATED

abstract class BaseActionResultProvider(private val resources: Resources) : ActionResultProvider {
    override fun getString(id: Int): String = resources.getString(id)
    override fun getString(id: Int, query: String): String = resources.getString(id, query)
    override fun parseCode(errorCode: Int, query: String): String {
        return when (errorCode) {
            SAVED -> resources.getString(R.string.saved)
            DELETED -> getString(R.string.deleted)
            UPDATED -> getString(R.string.name_updated)
            NO_DATA -> getString(R.string.no_data_to_load)
            NO_INTERNET -> getString(R.string.check_internet_connection)
            UNKNOWN -> getString(R.string.unknown_app_error)
            REDIRECTION -> getString(R.string.redirection)
            else -> getString(R.string.internal_app_error)
        }
    }
}

interface ActionResultProvider {
    fun parseCode(errorCode: Int, query: String = ""): String

    fun getString(id: Int): String

    fun getString(id: Int, query: String): String

    companion object {
        const val NO_DATA = 10
        const val NO_INTERNET = 11
        const val UNKNOWN = 12
        const val SAVED = 1
        const val DELETED = 2
        const val UPDATED = 4
        const val REDIRECTION = 3
        const val FAIL = -1
    }
}
