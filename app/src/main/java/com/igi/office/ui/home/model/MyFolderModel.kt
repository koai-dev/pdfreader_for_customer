package com.igi.office.ui.home.model

import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class MyFolderModel(

    @field:SerializedName("name")
    var name: String? = null,
    @field:SerializedName("lst_folder")
    val lstFolder: List<DocumentFile>? = null
)