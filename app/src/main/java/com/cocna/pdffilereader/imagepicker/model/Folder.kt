package com.cocna.pdffilereader.imagepicker.model

import java.util.*

data class Folder(
    var bucketId: Long?,
    var name: String?,
    var images: ArrayList<Image> = arrayListOf()
)
