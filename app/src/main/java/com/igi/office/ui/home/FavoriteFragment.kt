package com.igi.office.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import com.igi.office.databinding.FragmentFavoriteBinding
import com.igi.office.ui.base.BaseFragment

/**
 * Created by Thuytv on 09/06/2022.
 */
class FavoriteFragment : BaseFragment<FragmentFavoriteBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFavoriteBinding = FragmentFavoriteBinding::inflate
    override fun initData() {
    }

    override fun initEvents() {
    }
}