package com.cocna.pdffilereader

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.multidex.MultiDexApplication
import androidx.lifecycle.ProcessLifecycleOwner
import com.cocna.pdffilereader.common.Common
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.FirebaseApp
import com.cocna.pdffilereader.common.Logger
import java.util.*

/**
 * Created by Thuytv on 19/06/2022.
 */

class PdfApplication : MultiDexApplication(), LifecycleObserver {
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        FirebaseApp.initializeApp(this)
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this)

    }

    // ads start
    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
    @Suppress("DEPRECATION")
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        Logger.showLog("Thuytv------onMoveToForeground : " + Common.IS_BACK_FROM_BACKGROUND)
        countDownTimer?.cancel()
        countDownTimer = null
    }

    @Suppress("DEPRECATION")
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (countDownTimer == null) {
            countDownTimer = object : CountDownTimer(30 * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Logger.showLog("Thuytv------millisUntilFinished: $millisUntilFinished ---IS_BACK_FROM_BACKGROUND: " + Common.IS_BACK_FROM_BACKGROUND)
                }

                override fun onFinish() {
                    Common.IS_BACK_FROM_BACKGROUND = false
                    countDownTimer = null
                    Logger.showLog("Thuytv------onAppBackgrounded")
                }
            }
            countDownTimer?.start()
        }
    }
}