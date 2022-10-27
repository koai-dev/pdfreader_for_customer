package com.cocna.pdffilereader.ui.drive.adapter

import android.net.Uri
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class DriveModel(
    @field:SerializedName("name")
    var name: String? = null,
    @field:SerializedName("id")
    var id: String? = null,
    @field:SerializedName("size")
    var size: Long? = null,
    @field:SerializedName("createdTime")
    val createdTime: Long? = null,
    @field:SerializedName("modifiedTime")
    val modifiedTime: Long? = null,
    @field:SerializedName("extensionName")
    val extensionName: String? = "pdf",
    @field:SerializedName("folderName")
    val folderName: String? = "pdf"
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        return ((this.id == (other as DriveModel).id))
    }
}