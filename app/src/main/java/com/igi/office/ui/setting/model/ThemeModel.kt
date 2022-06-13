package com.igi.office.ui.setting.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Thuytv on 13/06/2022.
 */
class ThemeModel(
    @field:SerializedName("id_theme")
    var idTheme: Int? = null,
    @field:SerializedName("is_selected")
    var isSelected: Boolean? = null,
    @field:SerializedName("str_theme")
    var strTheme: String? = null
)