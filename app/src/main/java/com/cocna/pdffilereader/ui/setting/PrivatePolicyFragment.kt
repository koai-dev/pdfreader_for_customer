package com.cocna.pdffilereader.ui.setting

import android.net.Uri
import android.util.DisplayMetrics
import android.view.LayoutInflater
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.databinding.FragmentPrivatePolicyBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView


/**
 * Created by Thuytv on 24/06/2022.
 */
class PrivatePolicyFragment : BaseActivity<FragmentPrivatePolicyBinding>() {
    private lateinit var adView: AdView

    private var initialLayoutComplete = false

    // Determine the screen width (less decorations) to use for the ad width.
    // If the ad hasn't been laid out, default to the full screen width.
    private val adSize: AdSize
        get() {
            val display = windowManager?.defaultDisplay
            val outMetrics = DisplayMetrics()
            display?.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }
    override val bindingInflater: (LayoutInflater) -> FragmentPrivatePolicyBinding
        get() = FragmentPrivatePolicyBinding::inflate

    override fun initData() {
        binding.ttToolbarPolicy.text = getString(R.string.tt_term_codition)
        openPdfPolicy()
        adView = AdView(this)
        binding.adViewContainer.addView(adView)
        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                loadBannerAds()
            }
        }
    }

    override fun initEvents() {
        binding.imvPolicyBack.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            finish()
        }
    }

    private fun openPdfPolicy() {
        val inputStream = this.resources.openRawResource(R.raw.privacy_policy)
        binding.pdfViewerPolicy.fromStream(inputStream)
            .enableSwipe(true)
            .enableDoubletap(true)
            .swipeHorizontal(false)
            .fitEachPage(true)
            .pageFitPolicy(FitPolicy.WIDTH)
            .pageFitPolicy(FitPolicy.WIDTH)
            .load()
    }

    private fun loadBannerAds() {
        adView.adUnitId = getString(R.string.id_ad_banner_main)
        adView.setAdSize(adSize)

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)

    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    /** Called when returning to the activity  */
    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    /** Called before the activity is destroyed  */
    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}