package com.cocna.pdffilereader.common

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Thuytv on 12/06/2022.
 */
object Common {
    var TIME_USE_APP_START: Long? = null
    var IS_SEND_FIREBASE: Boolean = false
    var IS_BACK_FROM_BACKGROUND: Boolean? = null

    @JvmStatic
    fun covertTimeLongToString(time: Long?): String {
        time?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy     HH:mm", Locale.getDefault())
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
}