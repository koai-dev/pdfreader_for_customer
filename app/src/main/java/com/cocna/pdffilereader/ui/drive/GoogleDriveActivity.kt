package com.cocna.pdffilereader.ui.drive

import android.view.LayoutInflater
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.AppConfig
import com.cocna.pdffilereader.common.AppKeys
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity

/**
 * Created by Thuytv on 24/10/2022.
 */
class GoogleDriveActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding
        get() = ActivityBaseBinding::inflate

    override fun initData() {
        val typeScreen = intent.getStringExtra(AppKeys.KEY_BUNDLE_SCREEN)
        if (typeScreen == AppConfig.TYPE_SCREEN_DROPBOX) {
            replaceFragment(DropBoxFragment(), intent.extras, R.id.layout_container)
        } else {
            replaceFragment(GoogleDriveFragment(), intent.extras, R.id.layout_container)
        }
    }

    override fun initEvents() {
    }
}