package com.igi.office.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.igi.office.common.AppConfig
import com.igi.office.common.AppKeys
import com.igi.office.databinding.FragmentSettingBinding
import com.igi.office.ui.base.BaseFragment
import com.igi.office.ui.setting.ChangeThemeActivity
import com.igi.office.ui.setting.LanguageActivity

/**
 * Created by Thuytv on 09/06/2022.
 */
class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingBinding = FragmentSettingBinding::inflate
    override fun initData() {
        binding.btnChangeColor.setOnClickListener {
            getBaseActivity()?.onNextScreen(LanguageActivity::class.java, null, false)
//            getBaseActivity()?.onNextScreen(ChangeThemeActivity::class.java, null, false)
        }
    }

    override fun initEvents() {
    }
}