package com.cocna.pdffilereader.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.base.OnCallbackLoadAds
import com.cocna.pdffilereader.ui.home.dialog.LoadingAdsDialog
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.*

/**
 * Created by Thuytv on 10/06/2022.
 */
class CreateImageToPdfActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding
        get() = ActivityBaseBinding::inflate

    override fun initData() {
        replaceFragment(CreateImageToPdfFragment(), intent.extras, R.id.layout_container)
    }

    override fun initEvents() {
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Logger.showLog("Thuytv---------onNewIntent")
    }
}