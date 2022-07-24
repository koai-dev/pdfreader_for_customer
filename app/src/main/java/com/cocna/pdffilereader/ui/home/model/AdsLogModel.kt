package com.cocna.pdffilereader.ui.home.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AdsLogModel(
    @field:SerializedName("ads_id")
    var adsId: String? = null,
    @field:SerializedName("ads_name")
    var adsName: String? = null,
    @field:SerializedName("message")
    var message: String? = null,
    @field:SerializedName("device_name")
    var deviceName: String? = null
)