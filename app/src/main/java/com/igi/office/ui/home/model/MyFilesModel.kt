package com.igi.office.ui.home.model

import android.net.Uri
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MyFilesModel(

    @field:SerializedName("name")
    var name: String? = null,
    @field:SerializedName("uri")
    val uri: Uri? = null,
    @field:SerializedName("lastModified")
    val lastModified: Long? = null,
    @field:SerializedName("extension_name")
    val extensionName: String? = null,
    @field:SerializedName("length")
    val length: Long? = null
) : Parcelable