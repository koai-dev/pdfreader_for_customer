package com.cocna.pdffilereader.ui.home.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.Window
import androidx.documentfile.provider.DocumentFile
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Common
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.common.RxBus
import com.cocna.pdffilereader.databinding.DialogDeleteFileBinding
import com.cocna.pdffilereader.databinding.DialogProgressBarBinding
import com.cocna.pdffilereader.databinding.DialogProgressLoadAdsBinding
import com.cocna.pdffilereader.myinterface.OnDialogItemClickListener
import com.cocna.pdffilereader.myinterface.OnUpdateVersionClickListener
import com.cocna.pdffilereader.ui.base.OnCallbackTittleTab
import com.cocna.pdffilereader.ui.home.HistoryFragment
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import java.io.File

/**
 * Created by Thuytv on 13/06/2022.
 */
class ProgressDialogLoadingAds(private val mContext: Context) : Dialog(mContext, R.style.AlertDialogStyle) {
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent, context.theme)))
        } else {
            window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent)))
        }
        val bind: DialogProgressLoadAdsBinding = DialogProgressLoadAdsBinding.inflate(LayoutInflater.from(context))
        setContentView(bind.root)
        setCancelable(false)
        startCountDown(bind)
    }

    override fun onStop() {
        super.onStop()
        Logger.showLog("Thuytv--------onStop")
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun startCountDown(binding: DialogProgressLoadAdsBinding?) {
        if (countDownTimer == null) {
            countDownTimer = object : CountDownTimer(30 * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val second = (millisUntilFinished / 1000).toInt()
                    Logger.showLog("Thuytv--------onTick : $second")
                    binding?.vlLoadingAds?.text = context.getString(R.string.vl_loading_ads, (second.toString().plus("s")))
                }

                override fun onFinish() {
                    countDownTimer = null
                    if (isShowing) {
                        dismiss()
                    }
                }
            }
            countDownTimer?.start()
        }
    }
}