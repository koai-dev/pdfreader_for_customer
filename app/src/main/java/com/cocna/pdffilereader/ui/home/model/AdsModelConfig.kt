package com.cocna.pdffilereader.ui.home.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AdsModelConfig(
    @field:SerializedName("ads_banner_main")
    var ads_banner_main: Boolean = true,
    @field:SerializedName("ads_banner_reader")
    var ads_banner_reader: Boolean = true,
    @field:SerializedName("ads_inter_spalsh")
    var ads_inter_spalsh: Boolean = true,
    @field:SerializedName("ads_inter_file")
    var ads_inter_file: Boolean = true,
    @field:SerializedName("ads_inter_result_img")
    var ads_inter_result_img: Boolean = true,
    @field:SerializedName("ads_inter_back")
    var ads_inter_back: Boolean = true,
    @field:SerializedName("ads_native_file")
    var ads_native_file: Boolean = true,
    @field:SerializedName("ads_native_language")
    var ads_native_language: Boolean = true,
    @field:SerializedName("ads_native_theme")
    var ads_native_theme: Boolean = true,
    @field:SerializedName("ads_native_result_img")
    var ads_native_result_img: Boolean = true,
    @field:SerializedName("ads_native_top_bar")
    var ads_native_top_bar: Boolean = false,
    @field:SerializedName("ads_open_resume")
    var ads_open_resume: Boolean = true,
    @field:SerializedName("ads_inter_back_file")
    var ads_inter_back_file: Boolean = true
)