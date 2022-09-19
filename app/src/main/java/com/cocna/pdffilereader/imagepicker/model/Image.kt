package com.cocna.pdffilereader.imagepicker.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Image(
    @field:SerializedName("uri")
    var uri: Uri? = null,
    @field:SerializedName("name")
    var name: String? = null,
    @field:SerializedName("bucket_id")
    var bucketId: Long? = 0,
    @field:SerializedName("bucket_name")
    var bucketName: String? = "",
    @field:SerializedName("date_modified")
    var dateModified: String? = "",
    @field:SerializedName("header")
    var header: String? = null,
    @field:SerializedName("number_files")
    var numberFiles: Int? = null,
    @field:SerializedName("rotate")
    var rotate: Int = 0
) : Parcelable