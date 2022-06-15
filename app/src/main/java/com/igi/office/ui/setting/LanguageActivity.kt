package com.igi.office.ui.setting

import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.igi.office.MainActivity
import com.igi.office.R
import com.igi.office.common.AppConfig
import com.igi.office.common.AppKeys
import com.igi.office.common.MultiClickPreventer
import com.igi.office.common.invisible
import com.igi.office.databinding.ActivityLanguageBinding
import com.igi.office.ui.base.BaseActivity
import com.igi.office.ui.setting.adapter.ChangeLanguageAdapter
import com.igi.office.ui.setting.model.LanguageModel

/**
 * Created by Thuytv on 09/06/2022.
 */
class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityLanguageBinding
        get() = ActivityLanguageBinding::inflate
    private var mLanguageModelSelected: LanguageModel? = null
    private var typeScreen: String? = null

    override fun initData() {
        typeScreen = intent.getStringExtra(AppKeys.KEY_BUNDLE_SCREEN)
        if (typeScreen == AppConfig.TYPE_SCREEN_FROM_SPLASH) {
            binding.imvAllBack.invisible()
        }

        val lstLanguage: ArrayList<LanguageModel> = ArrayList()
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_ENGLISH, true, getString(R.string.vl_english), R.mipmap.ic_language_english))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_CHINA, false, getString(R.string.vl_china), R.mipmap.ic_language_china))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_INDIA, false, getString(R.string.vl_india), R.mipmap.ic_language_india))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_FRANCE, false, getString(R.string.vl_france), R.mipmap.ic_language_france))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_SPAIN, false, getString(R.string.vl_spain), R.mipmap.ic_language_spain))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_PORTUGAL, false, getString(R.string.vl_portugal), R.mipmap.ic_language_portugal))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_INDONESIA, false, getString(R.string.vl_indonesia), R.mipmap.ic_language_indonesia))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_KOREA, false, getString(R.string.vl_korean), R.mipmap.ic_language_korean))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_NETHERLANDS, false, getString(R.string.vl_netherlands), R.mipmap.ic_language_netherlands))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_JAPAN, false, getString(R.string.vl_japan), R.mipmap.ic_language_japan))
        val mLanguage = sharedPreferences.getLanguage()
        for (item in lstLanguage) {
            if (item.idLanguage == mLanguage) {
                lstLanguage[0].isSelected = false
                item.isSelected = true
                mLanguageModelSelected = item
                break
            }
        }
        val languageAdapter = ChangeLanguageAdapter(lstLanguage, object : ChangeLanguageAdapter.OnItemClickListener {
            override fun onClickItem(mData: LanguageModel) {
                mLanguageModelSelected = mData
            }

        })
        binding.rcvLanguageApp.apply {
            layoutManager = LinearLayoutManager(baseContext)
            adapter = languageAdapter
        }
        loadBannerAds()
    }

    override fun initEvents() {
        binding.imvAllBack.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            finish()
        }
        binding.imvConfirmLanguage.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            if (typeScreen == AppConfig.TYPE_SCREEN_FROM_SPLASH) {
                sharedPreferences.setLanguage(mLanguageModelSelected!!.idLanguage)
                onNextScreenClearTop(ChangeThemeActivity::class.java, intent.extras)
            } else {
                if (mLanguageModelSelected?.idLanguage != sharedPreferences.getLanguage()) {
                    sharedPreferences.setLanguage(mLanguageModelSelected!!.idLanguage)
                    onNextScreenClearTop(MainActivity::class.java, null)
                } else {
                    finish()
                }
            }
        }
    }

    private fun loadBannerAds() {
        val adRequest = AdRequest.Builder().build()
        binding.adViewBannerApp.loadAd(adRequest)
    }

    override fun onPause() {
        binding.adViewBannerApp.pause()
        super.onPause()
    }

    override fun onResume() {
        binding.adViewBannerApp.resume()
        super.onResume()
    }

    override fun onDestroy() {
        binding.adViewBannerApp.destroy()
        super.onDestroy()
    }
}