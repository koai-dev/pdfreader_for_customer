package com.cocna.pdffilereader

import android.view.LayoutInflater
import android.view.View
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.home.MainFragment
import com.cocna.pdffilereader.ui.home.dialog.LoadingAdsDialog

class MainActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding = ActivityBaseBinding::inflate
    private var countExit = 0

    override fun initData() {
        val typeScreen = intent.extras?.getString(AppKeys.KEY_BUNDLE_SCREEN)
        if (typeScreen == AppConfig.TYPE_SCREEN_SHOW_ADS) {
            if (sharedPreferences.getAdsConfig().ads_inter_spalsh) {
                LoadingAdsDialog.newInstance(this, AppConfig.ID_ADS_INTERSTITIAL).show(supportFragmentManager, "LOADING_ADS")
            }
        }
        replaceFragment(MainFragment(), intent.extras, R.id.layout_container)
    }

    override fun initEvents() {
    }

    /** Override the default implementation when the user presses the back key. */
    override fun onBackPressed() {
        // Move the task containing the MainActivity to the back of the activity stack, instead of
        // destroying it. Therefore, MainActivity will be shown when the user switches back to the app.
        countExit++
        binding.adFrameMain.gone()
        Logger.showLog("Thuytv--------onBackPressed: Main--: $countExit")
        if (countExit == 1 && sharedPreferences.getAdsConfig().ads_inter_spalsh) {
            binding.adFrameMain.visible()
            binding.prbLoadingMain.visible()
            showNativeAdsBottom(binding.adFrameMain, AppConfig.ID_ADS_NATIVE_EXIT, binding.prbLoadingMain)
        } else {
            countExit = 0
            moveTaskToBack(true)
        }
    }

    fun hideNativeAdsExit() {
        if (!isFinishing && !isDestroyed && binding.adFrameMain.visibility == View.VISIBLE) {
            binding.adFrameMain.gone()
            countExit = 0
        }
    }


}