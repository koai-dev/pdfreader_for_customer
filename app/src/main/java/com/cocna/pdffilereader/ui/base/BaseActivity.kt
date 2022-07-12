@file:Suppress("DEPRECATION")

package com.cocna.pdffilereader.ui.base

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.cocna.pdffilereader.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.ui.home.dialog.WellComeBackDialog
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Thuytv on 09/06/2022.
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater) -> VB
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var mInterstitialAd: InterstitialAd? = null
    private var countRetry: Int = 0

    private var broadcastReceiver: ConnectivityReceiver? = null
    lateinit var sharedPreferences: SharePreferenceUtils
    private var primaryBaseActivity: Context? = null
    private var currentNativeAd: NativeAd? = null

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.onActivityCreateSetTheme(this)
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        firebaseAnalytics = Firebase.analytics
        setContentView(requireNotNull(_binding).root)
        onHandleNetworkListener()
        MobileAds.initialize(this) { }
        broadcastReceiver = ConnectivityReceiver()
        registerReceiver(
            broadcastReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        sharedPreferences = SharePreferenceUtils(this)
        initData()
        initEvents()

    }

    abstract fun initData()
    abstract fun initEvents()
    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
        if (Common.checkTimeUseApp() && !Common.IS_SEND_FIREBASE) {
            Logger.showLog("Thuytv-----main_in_app_2mins")
            Common.IS_SEND_FIREBASE = true
            logEventFirebase(AppConfig.KEY_EVENT_FB_APP_2MINS, AppConfig.KEY_EVENT_FB_APP_2MINS)
        }
        Logger.showLog("Thuytv-----onResume------" + Common.IS_BACK_FROM_BACKGROUND)
        if (Common.IS_BACK_FROM_BACKGROUND == false) {
            Common.IS_BACK_FROM_BACKGROUND = true
//            loadInterstAds(getString(R.string.id_interstitial_ad_background), null)
            WellComeBackDialog().show(supportFragmentManager, "WELL_COMEBACK")
        }
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
        _binding = null
        unregisterReceiver(broadcastReceiver)
    }

    override fun attachBaseContext(newBase: Context) {
        primaryBaseActivity = newBase
        val localeToSwitchTo = SharePreferenceUtils(newBase).getLanguage()
        val localeUpdatedContext: ContextWrapper = LanguageUtils.updateLocale(newBase, Locale(localeToSwitchTo))
        super.attachBaseContext(localeUpdatedContext)
    }

    private fun isNetworkAvailable(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val networkInfo = manager!!.activeNetworkInfo
        var isAvailable = false
        if (networkInfo != null && networkInfo.isConnected) {
            // Network is present and connected
            isAvailable = true
        }
        return isAvailable
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
//        Logger.showToast(this, "Network: $isConnected")
    }

    fun onHandleNetworkListener() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
//                Logger.showToast(this@BaseActivity, "Network Available")
            }

            override fun onUnavailable() {
//                Logger.showToast(this@BaseActivity, "Network UnAvailable")
            }
        }
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    fun onNextScreen(nextActivity: Class<*>, bundle: Bundle?, isFinish: Boolean) {
        val intent = Intent(this, nextActivity)
        bundle?.let {
            intent.putExtras(it)
        }
        startActivity(intent)
        if (isFinish) {
            finish()
        }
    }

    fun onNextScreenClearTop(nextActivity: Class<*>, bundle: Bundle?) {
        val intent = Intent(this, nextActivity)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        bundle?.let {
            intent.putExtras(it)
        }
        startActivity(intent)
    }

    fun replaceFragment(fragment: Fragment, bundle: Bundle?, containerId: Int) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        bundle?.let {
            fragment.arguments = it
        }
        fragmentTransaction.replace(containerId, fragment)
        fragmentTransaction.commit()
    }

    fun printFile(myFilesModel: MyFilesModel) {
        val printManager: PrintManager = primaryBaseActivity?.getSystemService(Context.PRINT_SERVICE) as PrintManager
        try {
            myFilesModel.uriPath?.apply {
                val file = File(this)
                val printAdapter = PdfDocumentAdapter(file.absolutePath, myFilesModel.name ?: "")
                printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadInterstAds(uuidAds: String, onCallbackLoadAds: OnCallbackLoadAds?) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, uuidAds, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Logger.showLog("---onAdFailedToLoad: " + adError.message + "---countRetry: $countRetry")
                Handler(Looper.myLooper()!!).postDelayed({
                    mInterstitialAd = null
                    if (countRetry < 2) {
                        countRetry++
                        loadInterstAds(uuidAds, onCallbackLoadAds)
                    } else {
                        onCallbackLoadAds?.onCallbackActionLoadAds(false)
                    }
                }, AppConfig.DELAY_TIME_RETRY_ADS)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Logger.showLog("---onAdLoaded--Success")
                mInterstitialAd = interstitialAd
                showInterstitial(onCallbackLoadAds)
            }
        })
    }

    private fun showInterstitial(onCallbackLoadAds: OnCallbackLoadAds?) {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Logger.showLog("--fullScreenContentCallback-onAdDismissedFullScreenContent")
                    onCallbackLoadAds?.onCallbackActionLoadAds(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Logger.showLog("---fullScreenContentCallback--onAdFailedToShowFullScreenContent : " + adError.message)
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Logger.showLog("---fullScreenContentCallback--onAdShowedFullScreenContent")
                    mInterstitialAd = null
                }
            }
            mInterstitialAd!!.show(this)
        } else {
            onCallbackLoadAds?.onCallbackActionLoadAds(false)
        }
    }

    fun logEventFirebase(eventName: String, method: String) {
        firebaseAnalytics.logEvent(eventName) {
            param(FirebaseAnalytics.Param.ITEM_NAME, method)
        }
    }

    fun loadNativeAds(frameAdsNative: FrameLayout, uuidAds: String) {
        val builder = AdLoader.Builder(this, uuidAds)

        builder.forNativeAd { nativeAd ->
            var activityDestroyed = false
            activityDestroyed = isDestroyed
            if (activityDestroyed || isFinishing || isChangingConfigurations) {
                nativeAd.destroy()
                return@forNativeAd
            }
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            currentNativeAd?.destroy()
            currentNativeAd = nativeAd

            val adView = LayoutInflater.from(this)
                .inflate(R.layout.ads_unfield_item_file, null) as NativeAdView
            populateNativeAdView(nativeAd, adView)
            frameAdsNative.removeAllViews()
            frameAdsNative.addView(adView)
        }
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline

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
        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)

    }
}