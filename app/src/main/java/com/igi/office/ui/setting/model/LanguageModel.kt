package com.igi.office.ui.setting.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Thuytv on 13/06/2022.
 */
class LanguageModel(
    @field:SerializedName("id_language")
    var idLanguage: String? = null,
    @field:SerializedName("is_selected")
    var isSelected: Boolean? = null,
    @field:SerializedName("language_name")
    var languageName: String? = null,
    @field:SerializedName("language_icon")
    var languageIcon: Int? = null
)