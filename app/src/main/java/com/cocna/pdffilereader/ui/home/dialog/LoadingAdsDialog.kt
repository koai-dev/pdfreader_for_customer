package com.cocna.pdffilereader.ui.home.dialog

import android.annotation.SuppressLint
import android.app.Application
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.AppConfig
import com.cocna.pdffilereader.common.Common
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.databinding.DialogLoadingAdsBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.home.model.AdsLogModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.database.FirebaseDatabase


/**
 * Created by Thuytv on 26/06/2022.
 */
class LoadingAdsDialog : DialogFragment() {
    private var _binding: DialogLoadingAdsBinding? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var countRetry: Int = 0
    private var countDownTimer: CountDownTimer? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mActivity: BaseActivity<*>? = null
        fun newInstance(mActivity: BaseActivity<*>?): LoadingAdsDialog {
            val fragment = LoadingAdsDialog()
            this.mActivity = mActivity
            return fragment
        }
    }

    @NonNull
    override fun onCreateDialog(@Nullable savedInstanceState: Bundle?): Dialog {
        val mDialog = activity?.let { Dialog(it) }
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mDialog?.setCancelable(false)
        return mDialog!!
    }


    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.dialog_loading_ads, container, false)
        _binding = DialogLoadingAdsBinding.bind(v)
        return v
    }

    override fun onStart() {
        super.onStart()
        loadInterstAds()
        dialog?.run {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            window?.setLayout(width, height)
        }
        showCountDownTime()
    }

    override fun onStop() {
        super.onStop()
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun showCountDownTime() {
        if (countDownTimer == null) {
            countDownTimer = object : CountDownTimer(60 * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Logger.showLog("Thuytv------millisUntilFinished: $millisUntilFinished")
                }

                override fun onFinish() {
                    if (isVisible && dialog?.isShowing == true) {
                        dialog?.dismiss()
                    }
                }
            }
            countDownTimer?.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadInterstAds() {
        val adRequest = AdRequest.Builder().build()
        activity?.run {
            InterstitialAd.load(this, AppConfig.ID_ADS_INTERSTITIAL, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    setLogDataToFirebase(
                        AdsLogModel(
                            adsId = AppConfig.ID_ADS_INTERSTITIAL,
                            adsName = "Ads Interstitial Splash Load",
                            message = adError.message, deviceName = Common.getDeviceName(this@run)
                        )
                    )
//                    Handler(Looper.myLooper()!!).postDelayed({
                    mInterstitialAd = null
//                        if (countRetry < 2) {
//                            countRetry++
//                            loadInterstAds()
//                        } else {
                    dismiss()
//                        }
//                    }, AppConfig.DELAY_TIME_RETRY_ADS)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Logger.showLog("---onAdLoaded--Success Loading ads dialog")
                    mInterstitialAd = interstitialAd
                    showInterstitial()
                }
            })
        }
    }

    private fun showInterstitial() {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    dismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null

                    if (isVisible && activity != null) {
                        setLogDataToFirebase(
                            AdsLogModel(
                                adsId = AppConfig.ID_ADS_INTERSTITIAL,
                                adsName = "Ads Interstitial Splash Show",
                                message = adError.message, deviceName = Common.getDeviceName(activity!!)
                            )
                        )
                        dismiss()
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    mInterstitialAd = null
                }
            }
            activity?.run {
                mInterstitialAd!!.show(this)
            }
        } else {
            dismiss()
        }
    }

    private fun setLogDataToFirebase(adsLogModel: AdsLogModel) {
        try {
            val reference = FirebaseDatabase.getInstance().getReference("AdsError")
            reference.push().setValue(adsLogModel)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}