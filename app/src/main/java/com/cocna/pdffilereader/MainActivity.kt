package com.cocna.pdffilereader

import android.view.LayoutInflater
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.home.MainFragment

class MainActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding = ActivityBaseBinding::inflate


    override fun initData() {
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