package com.cocna.pdffilereader.ui.home

import android.view.LayoutInflater
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.base.OnCallbackTittleTab

/**
 * Created by Thuytv on 10/06/2022.
 */
class MyFileDetailActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding
        get() = ActivityBaseBinding::inflate

    override fun initData() {
        replaceFragment(MyFileDetailFragment(), intent.extras, R.id.layout_container)
    }

    override fun initEvents() {
    }
}