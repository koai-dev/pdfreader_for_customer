package com.cocna.pdffilereader.ui.drive

import android.view.LayoutInflater
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.drive.adapter.DropBoxFragment

/**
 * Created by Thuytv on 24/10/2022.
 */
class GoogleDriveActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding
        get() = ActivityBaseBinding::inflate

    override fun initData() {
//        replaceFragment(GoogleDriveFragment(), intent.extras, R.id.layout_container)
        replaceFragment(DropBoxFragment(), intent.extras, R.id.layout_container)
    }

    override fun initEvents() {
    }
}