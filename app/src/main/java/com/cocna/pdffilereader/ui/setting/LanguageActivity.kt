package com.cocna.pdffilereader.ui.setting

import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.cocna.pdffilereader.MainActivity
import com.cocna.pdffilereader.databinding.ActivityLanguageBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.setting.adapter.ChangeLanguageAdapter
import com.cocna.pdffilereader.ui.setting.model.LanguageModel
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.ui.home.dialog.LoadingAdsDialog
import com.cocna.pdffilereader.ui.home.model.AdsLogModel

/**
 * Created by Thuytv on 09/06/2022.
 */
class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    var currentNativeAd: NativeAd? = null
    override val bindingInflater: (LayoutInflater) -> ActivityLanguageBinding
        get() = ActivityLanguageBinding::inflate
    private var mLanguageModelSelected: LanguageModel? = null
    private var typeScreen: String? = null

    override fun initData() {
        typeScreen = intent.getStringExtra(AppKeys.KEY_BUNDLE_SCREEN)
        if (typeScreen == AppConfig.TYPE_SCREEN_FROM_SPLASH) {
            LoadingAdsDialog.newInstance(this).show(supportFragmentManager, "LOADING_ADS")
            binding.imvAllBack.invisible()
            preLoadAdsNativeTheme()
        }
        if (Common.mNativeAdLanguage == null) {
            Logger.showLog("Thuytv----Refresh Ads Language")
            refreshAd()
        } else {
            Logger.showLog("Thuytv----Show Ads Language")
            showNativeAds(Common.mNativeAdLanguage!!)
        }

        val lstLanguage: ArrayList<LanguageModel> = ArrayList()
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_ENGLISH, true, getString(R.string.vl_english), R.mipmap.ic_language_english))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_CHINA, false, getString(R.string.vl_china), R.mipmap.ic_language_china))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_INDIA, false, getString(R.string.vl_india), R.mipmap.ic_language_india))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_FRANCE, false, getString(R.string.vl_france), R.mipmap.ic_language_france))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_SPAIN, false, getString(R.string.vl_spain), R.mipmap.ic_language_spain))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_PORTUGAL, false, getString(R.string.vl_portugal), R.mipmap.ic_language_portugal))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_INDONESIA, false, getString(R.string.vl_indonesia), R.mipmap.ic_language_indonesia))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_KOREA, false, getString(R.string.vl_korean), R.mipmap.ic_language_korean))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_NETHERLANDS, false, getString(R.string.vl_netherlands), R.mipmap.ic_language_netherlands))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_JAPAN, false, getString(R.string.vl_japan), R.mipmap.ic_language_japan))
        lstLanguage.add(LanguageModel(AppConfig.ID_LANGUAGE_GERMANY, false, getString(R.string.vl_germany), R.mipmap.ic_language_germany))
        val mLanguage = sharedPreferences.getLanguage()
        for (item in lstLanguage) {
            if (item.idLanguage == mLanguage) {
                lstLanguage[0].isSelected = false
                item.isSelected = true
                mLanguageModelSelected = item
                break
            }
        }
        val languageAdapter = ChangeLanguageAdapter(lstLanguage, object : ChangeLanguageAdapter.OnItemClickListener {
            override fun onClickItem(mData: LanguageModel) {
                mLanguageModelSelected = mData
            }

        })
        binding.rcvLanguageApp.apply {
            layoutManager = LinearLayoutManager(baseContext)
            adapter = languageAdapter
        }
    }

    override fun initEvents() {
        binding.imvAllBack.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            finish()
        }
        binding.imvConfirmLanguage.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            logEventFirebase(AppConfig.KEY_EVENT_FB_LANGUAGE_CHANGE, AppConfig.KEY_EVENT_FB_LANGUAGE_CHANGE)
            if (typeScreen == AppConfig.TYPE_SCREEN_FROM_SPLASH) {
                sharedPreferences.setLanguage(mLanguageModelSelected!!.idLanguage)
                if(Common.isShowTheme) {
                    onNextScreenClearTop(ChangeThemeActivity::class.java, intent.extras)
                }else{
                    onNextScreenClearTop(MainActivity::class.java, null)
                }
            } else {
                if (mLanguageModelSelected?.idLanguage != sharedPreferences.getLanguage()) {
                    sharedPreferences.setLanguage(mLanguageModelSelected!!.idLanguage)
                    onNextScreenClearTop(MainActivity::class.java, null)
                } else {
                    finish()
                }
            }
        }
    }

    /* init ads start */
    /**
     * Populates a [NativeAdView] object with data from a given
     * [NativeAd].
     *
     * @param nativeAd the object containing the ad's assets
     * @param adView the view to be populated
     */
    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        nativeAd.mediaContent?.let { adView.mediaView?.setMediaContent(it) }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView?.visibility = View.INVISIBLE
        } else {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView?.visibility = View.INVISIBLE
        } else {
            adView.storeView?.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView?.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
//        val vc = nativeAd.mediaContent?.videoController

        // Updates the UI to say whether or not this ad has a video asset.
//        if (vc?.hasVideoContent()) {
//            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
//            // VideoController will call methods on this object when events occur in the video
//            // lifecycle.
//            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
//                override fun onVideoEnd() {
//                    // Publishers should allow native ads to complete video playback before
//                    // refreshing or replacing them with another ad in the same UI location.
//                    refresh_button.isEnabled = true
//                    videostatus_text.text = "Video status: Video playback has ended."
//                    super.onVideoEnd()
//                }
//            }
//        } else {
//            videostatus_text.text = "Video status: Ad does not contain a video asset."
//            refresh_button.isEnabled = true
//        }
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     *
     */
    private fun refreshAd() {

        val builder = AdLoader.Builder(this, AppConfig.ID_ADS_NATIVE_LANGUAGE)

        builder.forNativeAd { nativeAd ->
            // OnUnifiedNativeAdLoadedListener implementation.
            // If this callback occurs after the activity is destroyed, you must call
            // destroy and return or you may get a memory leak.
            showNativeAds(nativeAd)
        }

        val videoOptions = VideoOptions.Builder()
//            .setStartMuted(start_muted_checkbox.isChecked)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        builder.withNativeAdOptions(adOptions)

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                setLogDataToFirebase(
                    AdsLogModel(
                        adsId = AppConfig.ID_ADS_NATIVE_LANGUAGE, adsName = "Ads Native Language", message = loadAdError.message,
                        deviceName = Common.getDeviceName(this@LanguageActivity)
                    )
                )
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Common.setEventAdsNative(AppConfig.ID_ADS_NATIVE_LANGUAGE)
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())

//        videostatus_text.text = ""
    }

    private fun showNativeAds(nativeAd: NativeAd) {
        var activityDestroyed = false
        activityDestroyed = isDestroyed
        if (activityDestroyed || isFinishing || isChangingConfigurations) {
            nativeAd.destroy()
            return
        }
        // You must call destroy on old ads when you are done with them,
        // otherwise you will have a memory leak.
        currentNativeAd?.destroy()
        currentNativeAd = nativeAd
        val adView = layoutInflater
            .inflate(R.layout.ads_unifield, null) as NativeAdView
        populateNativeAdView(nativeAd, adView)
        binding.adFrameLanguage.removeAllViews()
        binding.adFrameLanguage.addView(adView)
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
        if (Common.mNativeAdLanguage != null) {
            Common.mNativeAdLanguage = null
        }
    }
    /* init ads end */

    private fun preLoadAdsNativeTheme() {

        val builder = AdLoader.Builder(this, AppConfig.ID_ADS_NATIVE_THEME)

        builder.forNativeAd { nativeAd ->
            Common.mNativeAdTheme = nativeAd
        }

        val videoOptions = VideoOptions.Builder()
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        builder.withNativeAdOptions(adOptions)

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                setLogDataToFirebase(
                    AdsLogModel(
                        adsId = AppConfig.ID_ADS_NATIVE_THEME, adsName = "Ads Native Theme", message = loadAdError.message,
                        deviceName = Common.getDeviceName(this@LanguageActivity)
                    )
                )
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Common.setEventAdsNative(AppConfig.ID_ADS_NATIVE_THEME)
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
}