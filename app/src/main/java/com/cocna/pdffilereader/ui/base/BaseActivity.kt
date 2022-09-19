@file:Suppress("DEPRECATION")

package com.cocna.pdffilereader.ui.base

import android.content.*
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintJob
import android.print.PrintManager
import android.provider.Settings
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
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.print.PDFDocumentAdapter
import com.cocna.pdffilereader.print.PrintJobMonitorService
import com.cocna.pdffilereader.ui.home.dialog.ProgressDialogLoadingAds
import com.cocna.pdffilereader.ui.home.dialog.WellComeBackDialog
import com.cocna.pdffilereader.ui.home.model.AdsLogModel
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.kochava.tracker.events.Event
import com.kochava.tracker.events.EventType
import io.reactivex.disposables.Disposable
import java.io.File
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
    private var mUUIDAds: String? = null

    private var broadcastReceiver: ConnectivityReceiver? = null
    lateinit var sharedPreferences: SharePreferenceUtils
    private var primaryBaseActivity: Context? = null
    private var currentNativeAd: NativeAd? = null
    private var mOnCallbackLoadAds: OnCallbackLoadAds? = null
    var isCurrentNetwork = true
    private var eventsBusDisposable: Disposable? = null

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.onActivityCreateSetTheme(this)
        super.onCreate(savedInstanceState)
        if (!isFinishing && !isDestroyed) {
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
    }

    abstract fun initData()
    abstract fun initEvents()
    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
        if (Common.checkTimeUseApp() && !Common.IS_SEND_FIREBASE) {
            Common.IS_SEND_FIREBASE = true
            logEventFirebase(AppConfig.KEY_EVENT_FB_APP_2MINS, AppConfig.KEY_EVENT_FB_APP_2MINS)
        }
        if (Common.IS_BACK_FROM_BACKGROUND == false) {
            Common.IS_BACK_FROM_BACKGROUND = true
//            loadInterstAds(getString(R.string.id_interstitial_ad_background), null)
            WellComeBackDialog.newInstance(application, this).show(supportFragmentManager, "WELL_COMEBACK")
        } else if (mInterstitialAd != null) {
            Handler(Looper.myLooper()!!).postDelayed({
                showInterstitial(mUUIDAds, mOnCallbackLoadAds)
            }, 500)
        }
//        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
//        IronSource.onPause(this)
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
        _binding = null
        unregisterReceiver(broadcastReceiver)

    }

    override fun onStop() {
        super.onStop()
        if (eventsBusDisposable?.isDisposed == false) eventsBusDisposable?.dispose()
    }

    override fun onStart() {
        super.onStart()
        onListenEventbus()
    }

    private fun onListenEventbus() {
        if (eventsBusDisposable == null || eventsBusDisposable?.isDisposed == true) {
            eventsBusDisposable = RxBus.listenDeBounce(EventsBus::class.java).subscribe {
                if (EventsBus.SHOW_ADS_BACK == it) {
                    if (!isFinishing && !isDestroyed) {
                        runOnUiThread {
                            loadInterstAds(AppConfig.ID_ADS_INTERSTITIAL_BACK_FILE, null)
                        }
                    }
                }
            }
        }
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
        Logger.showLog("Network: $isConnected")
        isCurrentNetwork = isConnected
    }

    fun enabaleNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
        } else {
            val wifimanager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifimanager?.apply {
                this.isWifiEnabled = true
            }
        }
    }

    fun onHandleNetworkListener() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
//                Logger.showToast(this@BaseActivity, "Network Available")
            }

            override fun onUnavailable() {
                Logger.showToast(this@BaseActivity, "Network UnAvailable")
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

    fun addFragment(fragment: Fragment, bundle: Bundle?, containerId: Int) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        bundle?.let {
            fragment.arguments = it
        }
        fragmentTransaction.add(containerId, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    fun printFile(myFilesModel: MyFilesModel) {
        try {
//            val printManager: PrintManager = primaryBaseActivity?.getSystemService(Context.PRINT_SERVICE) as PrintManager
            myFilesModel.uriPath?.apply {
                val file = File(this)
                val printAdapter = PDFDocumentAdapter(applicationContext, file)
                printPdf("Document", printAdapter, PrintAttributes.Builder().build())
//                val printAdapter = PdfDocumentAdapter(file.absolutePath, myFilesModel.name ?: "")
//                printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun printPdf(
        name: String, adapter: PrintDocumentAdapter,
        attrs: PrintAttributes
    ): PrintJob? {
        val printManager: PrintManager = primaryBaseActivity?.getSystemService(Context.PRINT_SERVICE) as PrintManager
        startService(Intent(this, PrintJobMonitorService::class.java))
        return printManager.print(name, adapter, attrs)
    }

    fun loadInterstAds(uuidAds: String, onCallbackLoadAds: OnCallbackLoadAds?) {
        val adRequest = AdRequest.Builder().build()
        val progressLoadingAds = ProgressDialogLoadingAds(this)
        if (uuidAds != AppConfig.ID_ADS_INTERSTITIAL) {
            progressLoadingAds.show()
        }
        mOnCallbackLoadAds = onCallbackLoadAds
        InterstitialAd.load(this, uuidAds, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Logger.showLog("---onAdFailedToLoad: " + adError.message + "---countRetry: $countRetry")
                setLogDataToFirebase(
                    AdsLogModel(
                        adsId = uuidAds,
                        adsName = "Ads Interstitial Load",
                        message = adError.message,
                        deviceName = Common.getDeviceName(this@BaseActivity)
                    )
                )
                if (!isFinishing && !isDestroyed && uuidAds != AppConfig.ID_ADS_INTERSTITIAL) {
                    progressLoadingAds.dismiss()
                }
//                Handler(Looper.myLooper()!!).postDelayed({
                mInterstitialAd = null
//                    if (countRetry < 2) {
//                        countRetry++
//                        loadInterstAds(uuidAds, onCallbackLoadAds)
//                    } else {
                onCallbackLoadAds?.onCallbackActionLoadAds(false)

//                    }
//                }, AppConfig.DELAY_TIME_RETRY_ADS)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Logger.showLog("---onAdLoaded--Success---BaseActivity")
                mInterstitialAd = interstitialAd
                mUUIDAds = uuidAds
                if (!isFinishing && !isDestroyed) {
                    if (uuidAds != AppConfig.ID_ADS_INTERSTITIAL) {
                        progressLoadingAds.dismiss()
                    }
                    showInterstitial(uuidAds, onCallbackLoadAds)
                }
            }
        })
    }

    private fun showInterstitial(uuidAds: String?, onCallbackLoadAds: OnCallbackLoadAds?) {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Logger.showLog("--fullScreenContentCallback-onAdDismissedFullScreenContent")
                    onCallbackLoadAds?.onCallbackActionLoadAds(true)
                    mInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Logger.showLog("---fullScreenContentCallback--onAdFailedToShowFullScreenContent : " + adError.message)
                    setLogDataToFirebase(
                        AdsLogModel(
                            adsId = uuidAds,
                            adsName = "Ads Interstitial Show",
                            message = adError.message,
                            deviceName = Common.getDeviceName(this@BaseActivity)
                        )
                    )
//                    if (countRetry < 2) {
//                        countRetry++
//                        loadInterstAds(uuidAds, onCallbackLoadAds)
//                    } else {
//                        onCallbackLoadAds?.onCallbackActionLoadAds(false)
//                    }
//                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Logger.showLog("---fullScreenContentCallback--onAdShowedFullScreenContent")
                    mInterstitialAd = null
                    Common.setEventAdsInterstitial(uuidAds ?: "")
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
        Event.buildWithEventType(EventType.LEVEL_COMPLETE).setName(eventName).send()

    }

    fun logEventFirebase(eventName: String, paramName: String, value: String) {
        firebaseAnalytics.logEvent(eventName) {
            param(paramName, value)
        }
        Event.buildWithEventType(EventType.LEVEL_COMPLETE).setName(eventName).setCustomStringValue(paramName, value).send()
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
                setLogDataToFirebase(
                    AdsLogModel(
                        adsId = uuidAds,
                        adsName = "Ads Native",
                        message = loadAdError.message,
                        deviceName = Common.getDeviceName(this@BaseActivity)
                    )
                )
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Common.setEventAdsNative(uuidAds)
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

    fun setLogDataToFirebase(adsLogModel: AdsLogModel) {
        try {
            val reference = FirebaseDatabase.getInstance().getReference("AdsError")
            reference.push().setValue(adsLogModel)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun gotoPlayStore() {
        val packageName = this.packageName ?: "com.cocna.pdfreader.viewpdf"
        val uri = Uri.parse("market://details?id=$packageName")
        Logger.showLog("Thuytv------gotoPlayStore: " + uri.path)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            this.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            this.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }


}