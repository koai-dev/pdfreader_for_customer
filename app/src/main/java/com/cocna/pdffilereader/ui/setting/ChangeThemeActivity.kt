package com.cocna.pdffilereader.ui.setting

import android.content.res.ColorStateList
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.cocna.pdffilereader.MainActivity
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.ActivityChangeThemeBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.home.model.AdsLogModel
import com.cocna.pdffilereader.ui.setting.adapter.ChangeThemeAdapter
import com.cocna.pdffilereader.ui.setting.model.ThemeModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Created by Thuytv on 13/06/2022.
 */
class ChangeThemeActivity : BaseActivity<ActivityChangeThemeBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityChangeThemeBinding
        get() = ActivityChangeThemeBinding::inflate

    private lateinit var changeThemeAdapter: ChangeThemeAdapter
    private var mThemeModel: ThemeModel? = null
    private var typeScreen: String? = null
    var currentNativeAd: NativeAd? = null

    override fun initData() {
        if (Common.mNativeAdTheme == null) {
            Logger.showLog("Thuytv----Refresh Ads Theme")
            refreshAd()
        } else {
            Logger.showLog("Thuytv----Show Ads Theme")
            showAdsNativeTheme(Common.mNativeAdLanguage!!)
        }
        typeScreen = intent.getStringExtra(AppKeys.KEY_BUNDLE_SCREEN)
        sharedPreferences.setValueBoolean(SharePreferenceUtils.KEY_FIRST_LOGIN, true)
        val theme = sharedPreferences.getThemeApp()
        val lstThemeApp: ArrayList<ThemeModel> = ArrayList()
        lstThemeApp.add(ThemeModel(R.color.rgb_F44336, true, AppConfig.THEME_1))
        lstThemeApp.add(ThemeModel(R.color.rgb_6F6AF8, false, AppConfig.THEME_2))
        lstThemeApp.add(ThemeModel(R.color.rgb_2B85FF, false, AppConfig.THEME_3))
        lstThemeApp.add(ThemeModel(R.color.rgb_ED6316, false, AppConfig.THEME_4))
        lstThemeApp.add(ThemeModel(R.color.rgb_433EA6, false, AppConfig.THEME_5))
        lstThemeApp.add(ThemeModel(R.color.rgb_167E30, false, AppConfig.THEME_6))
        lstThemeApp.add(ThemeModel(R.color.rgb_0F4743, false, AppConfig.THEME_7))
        for (item in lstThemeApp) {
            if (item.strTheme == theme) {
                lstThemeApp[0].isSelected = false
                item.isSelected = true
                mThemeModel = item
                break
            }
        }
        changeThemeAdapter = ChangeThemeAdapter(baseContext, lstThemeApp, object : ChangeThemeAdapter.OnItemClickListener {
            override fun onClickItem(themeModel: ThemeModel) {
                mThemeModel = themeModel
                changeColorTheme(themeModel)
            }
        })

        binding.rcvThemeApp.apply {
            layoutManager = GridLayoutManager(baseContext, 7)
//            layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = changeThemeAdapter
        }

    }
    private fun changeColorTheme(themeModel: ThemeModel){
        themeModel.idTheme?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.ttMenuThemeMyFile.setTextColor(resources.getColor(this, theme))
            }else{
                binding.ttMenuThemeMyFile.setTextColor(resources.getColor(this))
            }
            binding.imvMenuThemeMyFile.setColorFilter(ContextCompat.getColor(this@ChangeThemeActivity, this), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    override fun initEvents() {
        binding.imvAllBack.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            if (typeScreen == AppConfig.TYPE_SCREEN_FROM_SPLASH) {
                onNextScreen(MainActivity::class.java, null, true)
            } else {
                finish()
            }
        }
        binding.imvConfirmTheme.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            if (typeScreen == AppConfig.TYPE_SCREEN_FROM_SPLASH) {
                mThemeModel?.apply {
                    sharedPreferences.setThemeApp(strTheme)
                }
                onNextScreen(MainActivity::class.java, null, true)
            } else {
                mThemeModel?.apply {
                    if (strTheme != sharedPreferences.getThemeApp()) {
                        sharedPreferences.setThemeApp(strTheme)
                        RxBus.publish(EventsBus.RELOAD_THEME)
                    }
                }
                finish()
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

        val builder = AdLoader.Builder(this, AppConfig.ID_ADS_NATIVE_THEME)

        builder.forNativeAd { nativeAd ->
            showAdsNativeTheme(nativeAd)
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
                        adsId = AppConfig.ID_ADS_NATIVE_THEME, adsName = "Ads Native Theme", message = loadAdError.message,
                        deviceName = Common.getDeviceName(this@ChangeThemeActivity)
                    )
                )
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())

//        videostatus_text.text = ""
    }

    private fun showAdsNativeTheme(nativeAd: NativeAd) {
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
        binding.adFrameTheme.removeAllViews()
        binding.adFrameTheme.addView(adView)
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
        if (Common.mNativeAdTheme != null) {
            Common.mNativeAdTheme = null
        }
    }
    /* init ads end */

}