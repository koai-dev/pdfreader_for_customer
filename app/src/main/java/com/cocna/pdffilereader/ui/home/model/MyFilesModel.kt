package com.cocna.pdffilereader.ui.home.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MyFilesModel(

    @field:SerializedName("name")
    var name: String? = null,
    @field:SerializedName("uri_path")
    var uriPath: String? = null,
    @field:SerializedName("uri_old_path")
    var uriOldPath: String? = null,
    @field:SerializedName("lastModified")
    val lastModified: Long? = null,
    @field:SerializedName("extension_name")
    val extensionName: String? = null,
    @field:SerializedName("length")
    val length: Long? = null,
    @field:SerializedName("is_rename")
    var isRename: Boolean? = null,
    @field:SerializedName("is_delete")
    var isDelete: Boolean? = null,
    @field:SerializedName("folder_name")
    var folderName: String? = null,
    @field:SerializedName("list_child_file")
    var lstChildFile: ArrayList<MyFilesModel>? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        return ((this.uriPath == (other as MyFilesModel).uriPath) || (this.uriOldPath == (other as MyFilesModel).uriPath))
    }
}