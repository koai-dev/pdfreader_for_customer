package com.cocna.pdffilereader

import android.view.LayoutInflater
import com.cocna.pdffilereader.databinding.ActivityMainBinding
import com.cocna.pdffilereader.ui.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding = ActivityMainBinding::inflate


    override fun initData() {
        loadInterstAds(getString(R.string.id_interstitial_ad_splash))
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