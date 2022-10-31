package com.cocna.pdffilereader.ui.scan

import android.view.LayoutInflater
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity

/**
 * Created by Thuytv on 12/10/2022.
 */
class ScanCameraActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding
        get() = ActivityBaseBinding::inflate

    override fun initData() {
        replaceFragment(ScanCameraFragment(), null, R.id.layout_container)
    }

    override fun initEvents() {
    }
}