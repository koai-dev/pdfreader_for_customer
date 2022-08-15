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
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.*

/**
 * Created by Thuytv on 10/06/2022.
 */
class PdfViewActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding
        get() = ActivityBaseBinding::inflate

    override fun initData() {
//        binding.prbLoadingMain.visible()
        val myModel = intent.extras?.getParcelable<MyFilesModel>(AppKeys.KEY_BUNDLE_DATA)
        val fileName = intent.getStringExtra(AppKeys.KEY_BUNDLE_SHORTCUT_NAME)
        val filePath = intent.getStringExtra(AppKeys.KEY_BUNDLE_SHORTCUT_PATH)
        if (fileName?.isNotEmpty() == true && filePath?.isNotEmpty() == true) {
            val myFilesModel = MyFilesModel(name = fileName, uriPath = filePath, uriOldPath = filePath, extensionName = "pdf")
            val bundle = Bundle()
            bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, myFilesModel)
            intent.putExtras(bundle)
        }
        if (myModel == null && filePath.isNullOrEmpty()) {
//            binding.prbLoadingMain.visible()
            loadInterstAds(AppConfig.ID_ADS_INTERSTITIAL_FILE, object : OnCallbackLoadAds {
                override fun onCallbackActionLoadAds(isSuccess: Boolean) {
                    Handler(Looper.myLooper()!!).postDelayed({
                        gotoPdfViewFragment()
                    }, 500)
                }
            })
        } else {
            Common.countShowAdsPdf++
            if (Common.countShowAdsPdf == 1 || (Common.countShowAdsPdf % 2 == 1)) {
//            loadInterstAds(AppConfig.ID_ADS_INTERSTITIAL_FILE, object : OnCallbackLoadAds {
//                override fun onCallbackActionLoadAds(isSuccess: Boolean) {
//                    gotoPdfViewFragment()
//                }
//            })
                InterstitialUtils.sharedInstance?.showInterstitial(AppConfig.ID_ADS_INTERSTITIAL_FILE, this, object : OnCallbackLoadAds {
                    override fun onCallbackActionLoadAds(isSuccess: Boolean) {
                        Handler(Looper.myLooper()!!).postDelayed({
                            if (!isSuccess) {
                                Common.countShowAdsPdf = 0
                            }
                            gotoPdfViewFragment()
                        }, 500)
                    }
                })
            } else {
                gotoPdfViewFragment()
            }
        }

    }

    private fun gotoPdfViewFragment() {
        runOnUiThread {
            if (!isFinishing && !isDestroyed) {
                replaceFragment(PDFViewerFragment(), intent.extras, R.id.layout_container)
                logEventFirebase(AppConfig.KEY_EVENT_FB_OPEN_PDF, AppConfig.KEY_EVENT_FB_OPEN_PDF)
            }
        }
    }

    override fun initEvents() {
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Logger.showLog("Thuytv---------onNewIntent")
    }
}