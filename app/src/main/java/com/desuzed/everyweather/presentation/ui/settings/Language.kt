package com.desuzed.everyweather.presentation.ui.settings

import com.desuzed.everyweather.domain.model.settings.SettingsType

data class Language(
    override val id: String,
    override val categoryStringId: Int,
    override val valueStringId: Int,
    override val type: SettingsType = SettingsType.LANG,
) : BaseSettingItem()