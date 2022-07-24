package com.cocna.pdffilereader.ui.home.dialog

import android.app.Application
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.cocna.pdffilereader.PdfApplication
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.AppConfig
import com.cocna.pdffilereader.common.Common
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.databinding.DialogWellComebackBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.home.model.AdsLogModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


/**
 * Created by Thuytv on 26/06/2022.
 */
class WellComeBackDialog(private val mApplication: Application?, private val mActivity: BaseActivity<*>) : DialogFragment() {
    private var _binding: DialogWellComebackBinding? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var countRetry: Int = 0


    @NonNull
    override fun onCreateDialog(@Nullable savedInstanceState: Bundle?): Dialog {
        val mDialog = activity?.let { Dialog(it) }
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        return mDialog!!
    }


    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.dialog_well_comeback, container, false)
        _binding = DialogWellComebackBinding.bind(v)
        return v
    }

    override fun onStart() {
        super.onStart()
//        loadInterstAds()
        loadOpenResumeAds()
        dialog?.run {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            window?.setLayout(width, height)
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

    private fun loadOpenResumeAds() {
        val pdfApplication = mApplication as? PdfApplication
        // If the application is not an instance of MyApplication, log an error message and
        // start the MainActivity without showing the app open ad.
        if (pdfApplication == null) {
            Logger.showLog("Failed to cast application to MyApplication.")
            dismiss()
            return
        }

        // Show the app open ad.
        pdfApplication.showAdIfAvailable(
            mActivity,
            object : PdfApplication.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    if (isVisible && dialog?.isShowing == true) {
                        dismiss()
                    }
                }
            })
    }

    private fun loadInterstAds() {
        val adRequest = AdRequest.Builder().build()
        activity?.run {
            InterstitialAd.load(this, AppConfig.ID_ADS_INTERSTITIAL_BACKGROUND, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    setLogDataToFirebase(
                        AdsLogModel(
                            adsId = AppConfig.ID_ADS_INTERSTITIAL_BACKGROUND,
                            adsName = "Ads Interstitial Resume Load",
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
                    Logger.showLog("---onAdLoaded--Success")
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
                                adsId = AppConfig.ID_ADS_INTERSTITIAL_BACKGROUND,
                                adsName = "Ads Interstitial Resume Show",
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