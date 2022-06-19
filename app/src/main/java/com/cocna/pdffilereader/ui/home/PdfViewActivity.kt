package com.cocna.pdffilereader.ui.home

import android.view.LayoutInflater
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.AppConfig
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity

/**
 * Created by Thuytv on 10/06/2022.
 */
class PdfViewActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding
        get() = ActivityBaseBinding::inflate

    override fun initData() {
        loadInterstAds(getString(R.string.id_interstitial_ad_splash))
        replaceFragment(PDFViewerFragment(), intent.extras, R.id.layout_container)
        logEventFirebase(AppConfig.KEY_EVENT_FB_OPEN_PDF, AppConfig.KEY_EVENT_FB_OPEN_PDF)
    }

    override fun initEvents() {
    }
}