package com.igi.office.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import com.igi.office.databinding.FragmentBrowseBinding
import com.igi.office.ui.base.BaseFragment

/**
 * Created by Thuytv on 09/06/2022.
 */
class BrowseFragment : BaseFragment<FragmentBrowseBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBrowseBinding = FragmentBrowseBinding::inflate
    override fun initData() {
    }

    override fun initEvents() {
    }
}