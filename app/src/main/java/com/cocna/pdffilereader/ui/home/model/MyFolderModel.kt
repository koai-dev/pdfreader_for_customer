package com.cocna.pdffilereader.ui.home.model

import androidx.documentfile.provider.DocumentFile
import com.google.gson.annotations.SerializedName

data class MyFolderModel(

    @field:SerializedName("name")
    var name: String? = null,
    @field:SerializedName("lst_folder")
    val lstFolder: List<DocumentFile>? = null
)