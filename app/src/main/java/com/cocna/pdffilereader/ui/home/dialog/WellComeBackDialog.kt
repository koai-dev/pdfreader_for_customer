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


/**
 * Created by Thuytv on 26/06/2022.
 */
class WellComeBackDialog : DialogFragment() {
    private var _binding: DialogWellComebackBinding? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var countRetry: Int = 0
    private var countDownTimer: CountDownTimer? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mActivity: BaseActivity<*>? = null
        private var mApplication: Application? = null
        fun newInstance(mApplication: Application?, mActivity: BaseActivity<*>?): WellComeBackDialog {
            val fragment = WellComeBackDialog()
            this.mActivity = mActivity
            this.mApplication = mApplication
            return fragment
        }
    }

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
        showCountDownTime()
    }

    override fun onStop() {
        super.onStop()
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun showCountDownTime() {
        if (countDownTimer == null) {
            countDownTimer = object : CountDownTimer(20 * 1000, 1000) {
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
        mActivity?.apply {
            if (sharedPreferences.getAdsConfig().ads_open_resume) {
                pdfApplication.showAdIfAvailable(
                    this,
                    object : PdfApplication.OnShowAdCompleteListener {
                        override fun onShowAdComplete() {
                            Logger.showLog("Thuytv-----onShowAdComplete----isVisible: $isVisible ----isShowing: " + dialog?.isShowing)
                            if (isVisible && dialog?.isShowing == true) {
                                dialog?.dismiss()
                            }
                        }
                    })
            }
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