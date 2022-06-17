package com.igi.office.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.igi.office.R
import com.igi.office.common.AppConfig
import com.igi.office.common.AppKeys
import com.igi.office.common.MultiClickPreventer
import com.igi.office.common.listenClickViews
import com.igi.office.databinding.FragmentSettingBinding
import com.igi.office.ui.base.BaseFragment
import com.igi.office.ui.setting.ChangeThemeActivity
import com.igi.office.ui.setting.LanguageActivity

/**
 * Created by Thuytv on 09/06/2022.
 */
class SettingFragment : BaseFragment<FragmentSettingBinding>(), View.OnClickListener {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingBinding = FragmentSettingBinding::inflate
    override fun initData() {

    }

    override fun initEvents() {
        listenClickViews(binding.llSettingLanguage, binding.llSettingTheme, binding.llSettingShare, binding.llSettingRate, binding.llSettingTerm)
    }

    override fun onClick(v: View?) {
        MultiClickPreventer.preventMultiClick(v)
        when (v?.id) {
            R.id.llSettingLanguage -> {
                getBaseActivity()?.onNextScreen(LanguageActivity::class.java, null, false)
            }
            R.id.llSettingTheme -> {
                getBaseActivity()?.onNextScreen(ChangeThemeActivity::class.java, null, false)
            }
            R.id.llSettingShare -> {

            }
            R.id.llSettingRate -> {

            }
            R.id.llSettingTerm -> {

            }
        }
    }
}