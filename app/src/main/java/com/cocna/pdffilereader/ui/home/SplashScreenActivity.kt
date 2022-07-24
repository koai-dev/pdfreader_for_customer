package com.cocna.pdffilereader.ui.home

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import com.google.android.gms.ads.*
import com.cocna.pdffilereader.MainActivity
import com.cocna.pdffilereader.PdfApplication
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.ActivitySplassScreenBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.base.OnCallbackLoadAds
import com.cocna.pdffilereader.ui.setting.LanguageActivity
import com.google.android.gms.ads.interstitial.InterstitialAd
import java.util.*

/**
 * Created by Thuytv on 09/06/2022.
 */
class SplashScreenActivity : BaseActivity<ActivitySplassScreenBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivitySplassScreenBinding
        get() = ActivitySplassScreenBinding::inflate

    override fun initData() {
//        val testDeviceIds = Arrays.asList("B8D2F4981BD1CDC61FB420D2A9CC64E0")
//        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//        MobileAds.setRequestConfiguration(configuration)

        Common.TIME_USE_APP_START = System.currentTimeMillis()
        if ((sharedPreferences.getValueLong(SharePreferenceUtils.KEY_TIME_INSTALL) ?: 0) <= 0) {
            Logger.showLog("Thuytv----------KEY_TIME_INSTALL")
            sharedPreferences.setValueLong(SharePreferenceUtils.KEY_TIME_INSTALL, System.currentTimeMillis())
        } else {
            val isSend3Days = sharedPreferences.getValueBoolean(SharePreferenceUtils.KEY_SEND_EVENT_3DAYS)
            val isSend7Days = sharedPreferences.getValueBoolean(SharePreferenceUtils.KEY_SEND_EVENT_7DAYS)
            if (isSend3Days == false && Common.checkDayUseApp(this, 3)) {
                Logger.showLog("Thuytv----------KEY_EVENT_FB_APP_3DAYS")
                sharedPreferences.setValueBoolean(SharePreferenceUtils.KEY_SEND_EVENT_3DAYS, true)
                logEventFirebase(AppConfig.KEY_EVENT_FB_APP_3DAYS, AppConfig.KEY_EVENT_FB_APP_3DAYS)
            }
            if (isSend7Days == false && Common.checkDayUseApp(this, 7)) {
                Logger.showLog("Thuytv----------KEY_EVENT_FB_APP_7DAYS")
                sharedPreferences.setValueBoolean(SharePreferenceUtils.KEY_SEND_EVENT_7DAYS, true)
                logEventFirebase(AppConfig.KEY_EVENT_FB_APP_7DAYS, AppConfig.KEY_EVENT_FB_APP_7DAYS)
            }
        }

//        createTimer(3L)
        loadInterstAds(AppConfig.ID_ADS_INTERSTITIAL, object : OnCallbackLoadAds {
            override fun onCallbackActionLoadAds(isSuccess: Boolean) {
                gotoMainScreen()
            }
        })
    }

    override fun initEvents() {
    }

    private fun gotoMainScreen() {
        if (sharedPreferences.getValueBoolean(SharePreferenceUtils.KEY_FIRST_LOGIN) == false) {
            val bundle = Bundle()
            bundle.putString(AppKeys.KEY_BUNDLE_SCREEN, AppConfig.TYPE_SCREEN_FROM_SPLASH)
            onNextScreen(LanguageActivity::class.java, bundle, true)
        } else {
            onNextScreen(MainActivity::class.java, null, true)
        }
    }

    private fun createTimer(seconds: Long) {
        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {

                val application = application as? PdfApplication

                // If the application is not an instance of MyApplication, log an error message and
                // start the MainActivity without showing the app open ad.
                if (application == null) {
                    gotoMainScreen()
                    return
                }

//                // Show the app open ad.
//                application.showAdIfAvailable(
//                    this@SplashScreenActivity,
//                    object : PdfApplication.OnShowAdCompleteListener {
//                        override fun onShowAdComplete() {
//                            gotoMainScreen()
//                        }
//                    })
            }
        }
        countDownTimer.start()
    }
}