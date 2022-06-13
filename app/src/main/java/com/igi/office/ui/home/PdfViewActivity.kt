package com.igi.office.ui.home

import android.view.LayoutInflater
import com.igi.office.R
import com.igi.office.databinding.ActivityBaseBinding
import com.igi.office.ui.base.BaseActivity

/**
 * Created by Thuytv on 10/06/2022.
 */
class PdfViewActivity : BaseActivity<ActivityBaseBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding
        get() = ActivityBaseBinding::inflate

    override fun initData() {
        replaceFragment(PDFViewerFragment(), intent.extras, R.id.layout_container)
    }

    override fun initEvents() {
    }
}