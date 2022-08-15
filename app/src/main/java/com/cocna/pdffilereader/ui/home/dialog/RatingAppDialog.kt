package com.cocna.pdffilereader.ui.home.dialog

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.core.content.ContextCompat.startActivity
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.databinding.DialogRatingAppBinding
import me.zhanghai.android.materialratingbar.MaterialRatingBar


/**
 * Created by Thuytv on 13/06/2022.
 */
class RatingAppDialog(
    private val mContext: Context, private val isFirst: Boolean
) : Dialog(mContext, R.style.AlertDialogStyle) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.setBackgroundDrawable(ColorDrawable(mContext.resources.getColor(android.R.color.transparent, mContext.theme)))
        } else {
            window?.setBackgroundDrawable(ColorDrawable(mContext.resources.getColor(android.R.color.transparent)))
        }

        val binding: DialogRatingAppBinding = DialogRatingAppBinding.inflate(LayoutInflater.from(mContext))
        setContentView(binding.root)
        if (isFirst) {
            binding.vlTitleRate.text = mContext.getString(R.string.tt_rating_app_very_good_first)
            binding.vlContentRate.text = mContext.getString(R.string.vl_rating_app_very_good_first)
        } else {
            binding.vlTitleRate.text = mContext.getString(R.string.tt_rating_app_very_good)
            binding.vlContentRate.text = mContext.getString(R.string.vl_rating_app_very_good)
        }

        binding.rateBarApp.onRatingChangeListener =
            MaterialRatingBar.OnRatingChangeListener { _, rating -> updateViewRating(binding, rating) }
        binding.btnRateApp.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            val rating = binding.rateBarApp.rating
            if (rating >= 4) {
                gotoRatingApp()
            } else {
                sendEmailFeedback()
            }
            dismiss()
        }
        binding.imvCloseRate.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            dismiss()
        }
    }

    private fun updateViewRating(binding: DialogRatingAppBinding, rating: Float) {
        if (rating >= 4) {
            binding.imvRateStatus.setImageResource(R.mipmap.ic_rate_very_good)
            if (isFirst) {
                binding.vlTitleRate.text = mContext.getString(R.string.tt_rating_app_very_good_first)
                binding.vlContentRate.text = mContext.getString(R.string.vl_rating_app_very_good_first)
            } else {
                binding.vlTitleRate.text = mContext.getString(R.string.tt_rating_app_very_good)
                binding.vlContentRate.text = mContext.getString(R.string.vl_rating_app_very_good)
            }
            binding.btnRateApp.text = mContext.getString(R.string.btn_rate)
        } else {
            if (rating == 3f) {
                binding.imvRateStatus.setImageResource(R.mipmap.ic_rate_good)
            } else {
                binding.imvRateStatus.setImageResource(R.mipmap.ic_rate_bad)
            }
            binding.vlTitleRate.text = mContext.getString(R.string.tt_rating_app_bad)
            binding.vlContentRate.text = mContext.getString(R.string.vl_rating_app_bad)
            binding.btnRateApp.text = mContext.getString(R.string.btn_feedback)
        }
    }

    private fun gotoRatingApp() {
        val packageName = mContext.packageName ?: "com.cocna.pdfreader.viewpdf"
        val uri = Uri.parse("market://details?id=$packageName")
        Logger.showLog("Thuytv------gotoPlayStore: " + uri.path)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            mContext.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            mContext.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun sendEmailFeedback() {
//        val intent = Intent(Intent.ACTION_SENDTO)
//        intent.data = Uri.parse("mailto:thanhbg.bk01@gmail.com")
//        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for PDF Reader")
//        mContext.startActivity(intent)

        val selectorIntent = Intent(Intent.ACTION_SENDTO)
        selectorIntent.data = Uri.parse("mailto:")

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("thanhbg.bk01@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for PDF Reader")
        emailIntent.selector = selectorIntent

        mContext.startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }
}