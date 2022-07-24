package com.cocna.pdffilereader.ui.home

import android.view.LayoutInflater
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.AppConfig
import com.cocna.pdffilereader.common.Common
import com.cocna.pdffilereader.common.InterstitialUtils
import com.cocna.pdffilereader.common.visible
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.base.OnCallbackLoadAds

/**
 * Created by Thuytv on 10/06/2022.
 */
class PdfViewActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding
        get() = ActivityBaseBinding::inflate

    override fun initData() {
        binding.prbLoadingMain.visible()
        Common.countShowAdsPdf++
        if (Common.countShowAdsPdf == 1 || (Common.countShowAdsPdf % 2 == 1)) {
            loadInterstAds(AppConfig.ID_ADS_INTERSTITIAL_FILE, object : OnCallbackLoadAds {
                override fun onCallbackActionLoadAds(isSuccess: Boolean) {
                    gotoPdfViewFragment()
                }
            })
        } else {
            gotoPdfViewFragment()
        }
//        InterstitialUtils.sharedInstance?.showInterstitial(AppConfig.ID_ADS_INTERSTITIAL_FILE, this, object : OnCallbackLoadAds {
//            override fun onCallbackActionLoadAds(isSuccess: Boolean) {
//                replaceFragment(PDFViewerFragment(), intent.extras, R.id.layout_container)
//                logEventFirebase(AppConfig.KEY_EVENT_FB_OPEN_PDF, AppConfig.KEY_EVENT_FB_OPEN_PDF)
//            }
//        })
    }

    private fun gotoPdfViewFragment() {
        replaceFragment(PDFViewerFragment(), intent.extras, R.id.layout_container)
        logEventFirebase(AppConfig.KEY_EVENT_FB_OPEN_PDF, AppConfig.KEY_EVENT_FB_OPEN_PDF)
    }

    override fun initEvents() {
    }
}