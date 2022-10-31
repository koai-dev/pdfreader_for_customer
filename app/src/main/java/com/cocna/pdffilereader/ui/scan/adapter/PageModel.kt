package com.cocna.pdffilereader.ui.scan.adapter

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PageModel(

    @field:SerializedName("name")
    var name: String? = null,
    @field:SerializedName("is_selected")
    var isSelected: Boolean? = null
) : Parcelable