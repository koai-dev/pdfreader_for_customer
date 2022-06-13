package com.igi.office.ui.setting

import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.igi.office.MainActivity
import com.igi.office.R
import com.igi.office.common.*
import com.igi.office.databinding.ActivityChangeThemeBinding
import com.igi.office.ui.base.BaseActivity
import com.igi.office.ui.setting.adapter.ChangeThemeAdapter
import com.igi.office.ui.setting.model.ThemeModel

/**
 * Created by Thuytv on 13/06/2022.
 */
class ChangeThemeActivity : BaseActivity<ActivityChangeThemeBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityChangeThemeBinding
        get() = ActivityChangeThemeBinding::inflate

    private lateinit var changeThemeAdapter: ChangeThemeAdapter
    private var mThemeModel: ThemeModel? = null
    private var typeScreen: String? = null

    override fun initData() {
        typeScreen = intent.getStringExtra(AppKeys.KEY_BUNDLE_SCREEN)
        sharedPreferences.setValueBoolean(SharePreferenceUtils.KEY_FIRST_LOGIN, true)
        val theme = sharedPreferences.getThemeApp()
        val lstThemeApp: ArrayList<ThemeModel> = ArrayList()
        lstThemeApp.add(ThemeModel(R.color.rgb_F44336, true, AppConfig.THEME_1))
        lstThemeApp.add(ThemeModel(R.color.rgb_6F6AF8, false, AppConfig.THEME_2))
        lstThemeApp.add(ThemeModel(R.color.rgb_2B85FF, false, AppConfig.THEME_3))
        lstThemeApp.add(ThemeModel(R.color.rgb_ED6316, false, AppConfig.THEME_4))
        lstThemeApp.add(ThemeModel(R.color.rgb_433EA6, false, AppConfig.THEME_5))
        lstThemeApp.add(ThemeModel(R.color.rgb_167E30, false, AppConfig.THEME_6))
        lstThemeApp.add(ThemeModel(R.color.rgb_0F4743, false, AppConfig.THEME_7))
        for (item in lstThemeApp) {
            if (item.strTheme == theme) {
                lstThemeApp[0].isSelected = false
                item.isSelected = true
                mThemeModel = item
                break
            }
        }
        changeThemeAdapter = ChangeThemeAdapter(baseContext, lstThemeApp, object : ChangeThemeAdapter.OnItemClickListener {
            override fun onClickItem(themeModel: ThemeModel) {
                mThemeModel = themeModel
            }
        })

        binding.rcvThemeApp.apply {
            layoutManager = GridLayoutManager(baseContext, 7)
//            layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = changeThemeAdapter
        }
    }

    override fun initEvents() {
        binding.imvAllBack.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            if (typeScreen == AppConfig.TYPE_SCREEN_FROM_SPLASH) {
                onNextScreen(MainActivity::class.java, null, true)
            } else {
                finish()
            }
        }
        binding.imvConfirmTheme.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            if (typeScreen == AppConfig.TYPE_SCREEN_FROM_SPLASH) {
                mThemeModel?.apply {
                    sharedPreferences.setThemeApp(strTheme)
                }
                onNextScreen(MainActivity::class.java, null, true)
            } else {
                mThemeModel?.apply {
                    if (strTheme != sharedPreferences.getThemeApp()) {
                        sharedPreferences.setThemeApp(strTheme)
                        RxBus.publish(EventsBus.RELOAD_THEME)
                    }
                }
                finish()
            }
        }
//        binding.btnRed.setOnClickListener {
//            sharedPreferences.setThemeApp(AppConfig.THEME_4)
//            RxBus.publish(EventsBus.RELOAD_THEME)
//            finish()
//        }
//        binding.btnBlue.setOnClickListener {
//            sharedPreferences.setThemeApp(AppConfig.THEME_3)
//            RxBus.publish(EventsBus.RELOAD_THEME)
//            finish()
//        }
    }
}