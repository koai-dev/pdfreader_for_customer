package com.cocna.pdffilereader.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.webkit.MimeTypeMap
import androidx.core.content.pm.PackageInfoCompat
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.DocumentFileType
import com.anggrayudi.storage.file.extension
import com.anggrayudi.storage.file.search
import com.google.android.gms.ads.*
import com.cocna.pdffilereader.MainActivity
import com.cocna.pdffilereader.PdfApplication
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.ActivitySplassScreenBinding
import com.cocna.pdffilereader.myinterface.OnUpdateVersionClickListener
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.base.OnCallbackLoadAds
import com.cocna.pdffilereader.ui.home.dialog.ProgressDialog
import com.cocna.pdffilereader.ui.home.dialog.UpdateVersionDialog
import com.cocna.pdffilereader.ui.home.model.AdsLogModel
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.cocna.pdffilereader.ui.setting.LanguageActivity
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.cocna.pdffilereader.imagepicker.model.GridCount
import com.cocna.pdffilereader.imagepicker.model.Image
import com.cocna.pdffilereader.imagepicker.model.ImagePickerConfig
import com.cocna.pdffilereader.imagepicker.ui.imagepicker.registerImagePicker
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Thuytv on 09/06/2022.
 */
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : BaseActivity<ActivitySplassScreenBinding>() {
    private val PATH_DEFAULT_STORE = "/storage/emulated/0"
    private var startTime: Long = 0

    override val bindingInflater: (LayoutInflater) -> ActivitySplassScreenBinding
        get() = ActivitySplassScreenBinding::inflate

    override fun initData() {
//        val testDeviceIds = Arrays.asList("B8D2F4981BD1CDC61FB420D2A9CC64E0", "391347B342346395839E0B6C68235561")
//        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//        MobileAds.setRequestConfiguration(configuration)

        getAllFilePdf()
        getAllFileInDevice()

        Common.TIME_USE_APP_START = System.currentTimeMillis()
        if ((sharedPreferences.getValueLong(SharePreferenceUtils.KEY_TIME_INSTALL) ?: 0) <= 0) {
            sharedPreferences.setValueLong(SharePreferenceUtils.KEY_TIME_INSTALL, System.currentTimeMillis())
        } else {
            val isSend3Days = sharedPreferences.getValueBoolean(SharePreferenceUtils.KEY_SEND_EVENT_3DAYS)
            val isSend7Days = sharedPreferences.getValueBoolean(SharePreferenceUtils.KEY_SEND_EVENT_7DAYS)
            if (isSend3Days == false && Common.checkDayUseApp(this, 3)) {
                sharedPreferences.setValueBoolean(SharePreferenceUtils.KEY_SEND_EVENT_3DAYS, true)
                logEventFirebase(AppConfig.KEY_EVENT_FB_APP_3DAYS, AppConfig.KEY_EVENT_FB_APP_3DAYS)
            }
            if (isSend7Days == false && Common.checkDayUseApp(this, 7)) {
                sharedPreferences.setValueBoolean(SharePreferenceUtils.KEY_SEND_EVENT_7DAYS, true)
                logEventFirebase(AppConfig.KEY_EVENT_FB_APP_7DAYS, AppConfig.KEY_EVENT_FB_APP_7DAYS)
            }
        }
        startTime = System.currentTimeMillis()
        checkNewVersionApp()

    }

    private fun loadAds() {
        gotoMainScreen()
    }

    private fun checkNewVersionApp() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val playStoreVersionCode: Long = FirebaseRemoteConfig.getInstance().getLong(
                        "android_latest_version_code"
                    )
                    Common.isShowTheme = FirebaseRemoteConfig.getInstance().getBoolean("android_is_show_theme")
                    val androidShowAds = FirebaseRemoteConfig.getInstance().getString("android_show_ads")
                    Logger.showLog("Thuytv-------androidShowAds: $androidShowAds")
                    sharedPreferences.setAdsConfig(androidShowAds)
                    val pInfo = this.packageManager.getPackageInfo(packageName, 0)
                    val currentAppVersionCode = PackageInfoCompat.getLongVersionCode(pInfo)
                    Logger.showLog("Thuytv-------playStoreVersionCode: $playStoreVersionCode ----currentAppVersionCode: $currentAppVersionCode ---isShowTheme: " + Common.isShowTheme)
                    if (playStoreVersionCode > currentAppVersionCode) {
                        UpdateVersionDialog(this, object : OnUpdateVersionClickListener {
                            override fun onClickButtonDialog(isUpdateNow: Boolean) {
                                if (isUpdateNow) {
                                    gotoPlayStore()
                                } else {
                                    loadAds()
                                }
                            }
                        }).show()
                    } else {
                        loadAds()
                    }
                } else {
                    loadAds()
                    Logger.showLog("Thuytv--Config params Fetch failed: ")
                }
            }
    }

    override fun initEvents() {
        if (sharedPreferences.getValueBoolean(SharePreferenceUtils.KEY_FIRST_LOGIN) == false) {
            loadNativeAdsLanguage()
        }
    }

    private fun gotoMainScreen() {
        if (sharedPreferences.getValueBoolean(SharePreferenceUtils.KEY_FIRST_LOGIN) == false) {
            val bundle = Bundle()
            bundle.putString(AppKeys.KEY_BUNDLE_SCREEN, AppConfig.TYPE_SCREEN_FROM_SPLASH)
            onNextScreen(LanguageActivity::class.java, bundle, true)
        } else {
            val bundle = Bundle()
            bundle.putString(AppKeys.KEY_BUNDLE_SCREEN, AppConfig.TYPE_SCREEN_SHOW_ADS)
            onNextScreen(MainActivity::class.java, bundle, true)
        }
    }

    private fun createTimer(seconds: Long) {
        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {

                val application = application as? PdfApplication

                // If the application is not an instance of MyApplication, log an error message and
                // start the MainActivity without showing the app open ad.
                if (application == null) {
                    gotoMainScreen()
                    return
                }

//                // Show the app open ad.
//                application.showAdIfAvailable(
//                    this@SplashScreenActivity,
//                    object : PdfApplication.OnShowAdCompleteListener {
//                        override fun onShowAdComplete() {
//                            gotoMainScreen()
//                        }
//                    })
            }
        }
        countDownTimer.start()
    }

    private fun getAllFilePdf() {
        Thread {
            Common.listAllData = ArrayList()
            if (PermissionUtil.checkExternalStoragePermission(this)) {
                var root = DocumentFileCompat.getRootDocumentFile(this, "primary", true)
                if (root == null) {
                    root = DocumentFile.fromFile(File(PATH_DEFAULT_STORE))
                }
                getAllDir(root)
//                RxBus.publish(EventsBus.RELOAD_ALL_FILE)
            }
        }.start()
    }

    private fun getAllDir(rootFile: DocumentFile) {
        rootFile.listFiles().let { files ->
            try {
                val lstFile = files.indices
                for (i in lstFile) {
                    val item = files[i]
                    if (item.isDirectory) {
                        getAllDir(item)
                    } else {
                        if (item.extension.lowercase() == "pdf") {
                            val model =
                                MyFilesModel(
                                    name = item.name,
                                    uriPath = item.uri.path,
                                    uriOldPath = item.uri.path,
                                    lastModified = item.lastModified(),
                                    extensionName = item.extension,
                                    length = item.length(),
                                    locationFile = item.parentFile?.uri?.path,
                                    folderName = item.parentFile?.name
                                )
                            Common.listAllData?.add(model)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


//    private fun getFilePdf(rootFile: DocumentFile) {
//        val pdfArray = rootFile.listFiles()
//        if (pdfArray.isNotEmpty()) {
//            for (item in pdfArray) {
//                if (item.isFile) {
//                    if (item.extension.lowercase() == "pdf") {
//                        val model =
//                            MyFilesModel(
//                                name = item.name,
//                                uriPath = item.uri.path,
//                                uriOldPath = item.uri.path,
//                                lastModified = item.lastModified(),
//                                extensionName = item.extension,
//                                length = item.length(),
//                                locationFile = item.parentFile?.uri?.path,
//                                folderName = item.parentFile?.name
//                            )
//                        Common.listAllData?.add(model)
//                    }
//                } else {
//                    getFilePdf(item)
//                }
//            }
//        }
//    }

    private fun getAllFileInDevice() {
        Thread {
            Common.listAllFolder = ArrayList()
            if (PermissionUtil.checkExternalStoragePermission(this)) {
                Logger.showLog("Thuytv---------getAllFileInDevice--Spalsh")
                val root = DocumentFile.fromFile(File(PATH_DEFAULT_STORE))
                val mimePDF = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")!!
                root.listFiles().let {
                    for (item in it) {
                        if (!item.isFile) {
                            val rootFile = item.search(true, DocumentFileType.FILE, arrayOf(mimePDF))
                            if (rootFile.isNotEmpty()) {
                                val lstChildFile: ArrayList<MyFilesModel> = ArrayList()
                                for (mFile in rootFile) {
                                    val model =
                                        MyFilesModel(
                                            name = mFile.name,
                                            uriPath = mFile.uri.path,
                                            uriOldPath = mFile.uri.path,
                                            lastModified = mFile.lastModified(),
                                            extensionName = mFile.extension,
                                            length = mFile.length(),
                                            locationFile = mFile.parentFile?.uri?.path,
                                            folderName = mFile.parentFile?.name
                                        )
                                    lstChildFile.add(model)
                                }
                                val mFolder = MyFilesModel(folderName = item.name, lstChildFile = lstChildFile)
                                Common.listAllFolder?.add(mFolder)
                            }
                        } else if (item.extension.lowercase() == "pdf") {
                            val model =
                                MyFilesModel(
                                    name = item.name,
                                    uriPath = item.uri.path,
                                    uriOldPath = item.uri.path,
                                    lastModified = item.lastModified(),
                                    extensionName = item.extension,
                                    length = item.length(),
                                    locationFile = item.parentFile?.uri?.path,
                                    folderName = item.parentFile?.name
                                )
                            Common.listAllFolder?.add(model)
                        }
                    }
                }

//                RxBus.publish(EventsBus.RELOAD_ALL_FOLDER)
            }
        }.start()
    }

    private fun loadNativeAdsLanguage() {
        val builder = AdLoader.Builder(this, AppConfig.ID_ADS_NATIVE_LANGUAGE)

        builder.forNativeAd { nativeAd ->
            Common.mNativeAdLanguage = nativeAd
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
                        adsId = AppConfig.ID_ADS_NATIVE_LANGUAGE, adsName = "Ads Native Language", message = loadAdError.message,
                        deviceName = Common.getDeviceName(this@SplashScreenActivity)
                    )
                )
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Common.setEventAdsNative(AppConfig.ID_ADS_NATIVE_LANGUAGE)
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }


}