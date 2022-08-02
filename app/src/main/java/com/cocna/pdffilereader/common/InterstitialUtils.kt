package com.cocna.pdffilereader.common

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.cocna.pdffilereader.ui.base.OnCallbackLoadAds
import com.cocna.pdffilereader.common.InterstitialUtils
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Created by Thuytv on 09/07/2022.
 */
class InterstitialUtils {
    private var mInterstitialAd: InterstitialAd? = null
    private val onCallbackLoadAds: OnCallbackLoadAds? = null
    private val isReloaded = false
    private var countRetry: Int = 0

    fun loadInterstAds(uuidAds: String, context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, uuidAds, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Logger.showLog("---onAdFailedToLoad: " + adError.message + "---countRetry: $countRetry")
                Handler(Looper.myLooper()!!).postDelayed({
                    mInterstitialAd = null
                    if (countRetry < 1) {
                        countRetry++
                        loadInterstAds(uuidAds, context)
                    }
//                else {
//                    onCallbackLoadAds?.onCallbackActionLoadAds(false)
//                }
                }, AppConfig.DELAY_TIME_RETRY_ADS)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Logger.showLog("---onAdLoaded--Success")
                mInterstitialAd = interstitialAd
            }
        })
    }

    fun showInterstitial(uuidAds: String, context: Activity, onCallbackLoadAds: OnCallbackLoadAds?) {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Logger.showLog("Thuytv---2-fullScreenContentCallback-onAdDismissedFullScreenContent")
                    onCallbackLoadAds?.onCallbackActionLoadAds(true)
                    loadInterstAds(uuidAds, context)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Logger.showLog("Thuytv---2--fullScreenContentCallback--onAdFailedToShowFullScreenContent : " + adError.message)
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Logger.showLog("Thuytv---2--fullScreenContentCallback--onAdShowedFullScreenContent")
                    mInterstitialAd = null
                }
            }
            mInterstitialAd!!.show(context)
        } else {
            Logger.showLog("Thuytv------show pre load")
            countRetry = 0
            loadInterstAds(uuidAds, context)
            onCallbackLoadAds?.onCallbackActionLoadAds(false)
        }
    }


    companion object {
        var sharedInstance: InterstitialUtils? = null
            get() {
                if (field == null) {
                    field = InterstitialUtils()
                }
                return field
            }
            private set
    }
}