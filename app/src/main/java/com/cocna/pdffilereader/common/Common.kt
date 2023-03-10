package com.cocna.pdffilereader.common

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.TextView
import androidx.annotation.Keep
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.google.android.gms.ads.nativead.NativeAd
import com.kochava.tracker.events.Event
import com.kochava.tracker.events.EventType
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by Thuytv on 12/06/2022.
 */

object Common {
    var TIME_USE_APP_START: Long? = null
    var IS_SEND_FIREBASE: Boolean = false
    var IS_BACK_FROM_BACKGROUND: Boolean? = null
    var countShowAdsOpenResume: Int = 0
    var countShowAdsPdf: Int = 0
    var lstDataDetail: ArrayList<MyFilesModel>? = null
    var listAllData: ArrayList<MyFilesModel>? = null
    var listAllFolder: ArrayList<MyFilesModel>? = null
    var countRatingApp: Int = 0
    var isFromPDFView: Boolean? = null
    var isShowTheme: Boolean = true

    @Keep
    var mNativeAdLanguage: NativeAd? = null

    @Keep
    var mNativeAdTheme: NativeAd? = null

    @JvmStatic
    fun covertTimeLongToString(time: Long?): String {
        time?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(time))
        }
        return ""
    }

    @JvmStatic
    fun covertTimeLongToStringGrid(time: Long?): String {
        time?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(time))
        }
        return ""
    }

    fun checkTimeUseApp(): Boolean {
        if ((TIME_USE_APP_START ?: 0) > 0) {
            val currentTime = System.currentTimeMillis()
            val duration = currentTime - TIME_USE_APP_START!!
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
            return minutes >= 2
        }
        return false
    }

    fun checkDayUseApp(mContext: Context, days: Int): Boolean {
        val sharePreferenceUtils = SharePreferenceUtils(mContext)
        val timeInstall = sharePreferenceUtils.getValueLong(SharePreferenceUtils.KEY_TIME_INSTALL)
        if ((timeInstall ?: 0) > 0) {
            val currentTime = System.currentTimeMillis()
            val duration = currentTime - timeInstall!!
            val mDays = TimeUnit.MILLISECONDS.toDays(duration)
            return mDays >= days
        }
        return false
    }

    fun getVersionApp(context: Context?): String {
        try {
            val pInfo = context?.packageManager?.getPackageInfo(context.packageName ?: "", 0)
            return pInfo?.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getDeviceName(context: Context?): String {
        try {
            return Build.BRAND + "-" + Build.MODEL + " Version: " + Build.VERSION.SDK_INT + "--Version App: " + getVersionApp(context)
        } catch (e: Exception) {
            return ""
        }
    }

    fun convertByteToString(bytes: Long?): String {
        if (bytes == null) return ""
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024
        return if (bytes in 0 until kilobyte) {
            "$bytes B"
        } else if (bytes in kilobyte until megabyte) {
            (bytes / kilobyte).toString() + " KB"
        } else if (bytes in megabyte until gigabyte) {
            (bytes / megabyte).toString() + " MB"
        } else if (bytes in gigabyte until terabyte) {
            (bytes / gigabyte).toString() + " GB"
        } else if (bytes >= terabyte) {
            (bytes / terabyte).toString() + " TB"
        } else {
            "$bytes Bytes"
        }
    }

    fun setEventAdsBanner(unitID: String) {
        Event.buildWithEventType(EventType.AD_VIEW)
            .setName("AdMob")
            .setAdSize("SMART_BANNER")
            .setAdPlacement(unitID)
            .send()
    }

    fun setEventAdsInterstitial(unitID: String) {
        Event.buildWithEventType(EventType.AD_VIEW)
            .setName("AdMob")
            .setAdType("Interstitial")
            .setAdPlacement(unitID)
            .send()
    }

    fun setEventAdsNative(unitID: String) {
        Event.buildWithEventType(EventType.AD_VIEW)
            .setName("AdMob")
            .setAdType("Native")
            .setAdSize("Small")
            .setAdPlacement(unitID)
            .send()
    }

    @JvmStatic
    fun covertDateLongToString(time: Long?): String {
        time?.let {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(time * 1000))
//            return DateFormat.getDateInstance().format(Date(time * 1000))
        }
        return ""
    }

    @JvmStatic
    fun covertTimeDisplayResult(time: Long?): String {
        time?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy-hh:MM:ss", Locale.getDefault())
            return sdf.format(Date(time))
//            return DateFormat.getDateInstance().format(Date(time * 1000))
        }
        return ""
    }

    @JvmStatic
    fun getDateCreatePdf(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy-hh:MM:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDateCompare(strDate: String): String {
        if (strDate.isEmpty()) return ""
        val dfDisplay = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val displayDate = dfDisplay.parse(strDate)
        val dfServer = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return dfServer.format(displayDate)
    }

    @Suppress("DEPRECATION")
    fun setTextColor(mContext: Context?, textView: TextView, color: Int) {
        mContext?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.setTextColor(mContext.resources.getColor(color, mContext.theme))
            } else {
                textView.setTextColor(mContext.resources.getColor(color))
            }
        }
    }
}