package com.igi.office.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.igi.office.MainActivity
import com.igi.office.R
import com.igi.office.common.AppConfig
import com.igi.office.common.AppKeys
import com.igi.office.common.Logger
import com.igi.office.common.SharePreferenceUtils
import com.igi.office.databinding.ActivitySplassScreenBinding
import com.igi.office.ui.base.BaseActivity
import com.igi.office.ui.setting.LanguageActivity
import java.util.*

/**
 * Created by Thuytv on 09/06/2022.
 */
class SplashScreenActivity : BaseActivity<ActivitySplassScreenBinding>() {
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"
    private var countRetry: Int = 0

    override val bindingInflater: (LayoutInflater) -> ActivitySplassScreenBinding
        get() = ActivitySplassScreenBinding::inflate

    override fun initData() {
        val testDeviceIds = Arrays.asList("B8D2F4981BD1CDC61FB420D2A9CC64E0")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)

        loadInterstAds()
//        gotoMainScreen()
    }

    private fun showInterstitial() {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Logger.showLog("--fullScreenContentCallback-onAdDismissedFullScreenContent")
                    gotoMainScreen()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Logger.showLog("---fullScreenContentCallback--onAdFailedToShowFullScreenContent : " + adError.message)
                    mInterstitialAd = null
                    if (countRetry < 5) {
                        countRetry++
                        loadInterstAds()
                    } else {
                        gotoMainScreen()
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    Logger.showLog("---fullScreenContentCallback--onAdShowedFullScreenContent")
                    mInterstitialAd = null
                }
            }
            mInterstitialAd!!.show(this)
        } else {
            Logger.showLog("Ad did not load")
            gotoMainScreen()
        }
    }

    override fun initEvents() {
//        binding.btnBannerAds.setOnClickListener {
//            loadBannerAds()
//        }
//        binding.btnInterstitialAds.setOnClickListener {
//            loadFullScreenInterstAds()
//        }
    }

//    private fun loadBannerAds() {
//        val adRequest = AdRequest.Builder().build()
//        binding.adViewBannerApp.loadAd(adRequest)
//        binding.adViewBannerApp.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                Logger.showLog("Thuytv--Banner Ads-----onAdLoaded")
//            }
//
//            override fun onAdFailedToLoad(adError: LoadAdError) {
//                Logger.showLog("Thuytv--Banner Ads-----onAdFailedToLoad : " + adError.message)
//            }
//
//            override fun onAdOpened() {
//                // Code to be executed when an ad opens an overlay that
//                // covers the screen.
//                Logger.showLog("Thuytv--Banner Ads-----onAdOpened")
//            }
//
//            override fun onAdClicked() {
//                // Code to be executed when the user clicks on an ad.
//                Logger.showLog("Thuytv--Banner Ads-----onAdClicked")
//            }
//
//            override fun onAdClosed() {
//                // Code to be executed when the user is about to return
//                // to the app after tapping on an ad.
//                Logger.showLog("Thuytv--Banner Ads-----onAdClosed")
//            }
//        }
//    }

    private fun loadInterstAds() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Logger.showLog("---onAdFailedToLoad: " + adError?.message + "---countRetry: $countRetry")
                mInterstitialAd = null
                if (countRetry < 5) {
                    countRetry++
                    loadInterstAds()
                } else {
                    gotoMainScreen()
                }
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Logger.showLog("---onAdLoaded--Success")
                mInterstitialAd = interstitialAd
                showInterstitial()
            }
        })
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
}