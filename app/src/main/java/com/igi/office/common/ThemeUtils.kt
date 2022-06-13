package com.igi.office.common

import android.app.Activity
import android.content.Intent
import com.igi.office.R

object ThemeUtils {
    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    fun changeToTheme(activity: Activity) {
        activity.finish()
        activity.startActivity(Intent(activity, activity.javaClass))
    }

    /**
     * Set the theme of the activity, according to the configuration.
     */
    fun onActivityCreateSetTheme(activity: Activity) {
        val sharePreferenceUtils = SharePreferenceUtils(activity)
        when (sharePreferenceUtils.getThemeApp()) {
            AppConfig.THEME_2 -> activity.setTheme(R.style.MyTheme2)
            AppConfig.THEME_3 -> activity.setTheme(R.style.MyTheme3)
            AppConfig.THEME_4 -> activity.setTheme(R.style.MyTheme4)
            AppConfig.THEME_5 -> activity.setTheme(R.style.MyTheme5)
            AppConfig.THEME_6 -> activity.setTheme(R.style.MyTheme6)
            AppConfig.THEME_7 -> activity.setTheme(R.style.MyTheme7)
            else -> activity.setTheme(R.style.MyTheme1)
        }
    }
}