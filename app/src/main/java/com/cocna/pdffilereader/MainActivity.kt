package com.cocna.pdffilereader

import android.view.LayoutInflater
import com.cocna.pdffilereader.common.AppConfig
import com.cocna.pdffilereader.common.AppKeys
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.home.MainFragment
import com.cocna.pdffilereader.ui.home.dialog.LoadingAdsDialog

class MainActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding = ActivityBaseBinding::inflate


    override fun initData() {
        val typeScreen = intent.extras?.getString(AppKeys.KEY_BUNDLE_SCREEN)
//        if (typeScreen == AppConfig.TYPE_SCREEN_SHOW_ADS) {
//            LoadingAdsDialog.newInstance(this).show(supportFragmentManager, "LOADING_ADS")
//        }
        replaceFragment(MainFragment(), intent.extras, R.id.layout_container)
    }

    override fun initEvents() {
    }

    /** Override the default implementation when the user presses the back key. */
    override fun onBackPressed() {
        // Move the task containing the MainActivity to the back of the activity stack, instead of
        // destroying it. Therefore, MainActivity will be shown when the user switches back to the app.
        moveTaskToBack(true)
    }


}