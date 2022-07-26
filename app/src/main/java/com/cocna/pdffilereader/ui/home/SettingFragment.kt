package com.cocna.pdffilereader.ui.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.common.listenClickViews
import com.cocna.pdffilereader.databinding.FragmentSettingBinding
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.setting.ChangeThemeActivity
import com.cocna.pdffilereader.ui.setting.LanguageActivity
import com.cocna.pdffilereader.ui.setting.PrivatePolicyFragment

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
                shareApplication()
            }
            R.id.llSettingRate -> {
                getBaseActivity()?.gotoPlayStore()
            }
            R.id.llSettingTerm -> {
                getBaseActivity()?.onNextScreen(PrivatePolicyFragment::class.java, null, false)
            }
        }
    }


    private fun shareApplication() {
        val packageName = getBaseActivity()?.packageName ?: "com.cocna.pdfreader.viewpdf"
        val strPath = "https://play.google.com/store/apps/details?id=$packageName"
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, strPath)
        getBaseActivity()?.startActivity(Intent.createChooser(shareIntent, getString(R.string.vl_share_application)))
    }
}