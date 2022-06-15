package com.igi.office.common

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.igi.office.ui.home.model.MyFilesModel

/**
 * Created by Thuytv2 on 11/15/2018.
 */
class SharePreferenceUtils(context: Context?) {

    private var IShare: SharedPreferences? = null

    init {
        if (context != null)
            IShare = context.getSharedPreferences(context.applicationInfo.packageName, Context.MODE_PRIVATE)
    }

    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    ////// Remove Share Preferences
    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    fun PreferenceUtilRemove(context: Context?) {
        if (context != null)
            IShare = context.getSharedPreferences(context.applicationInfo.packageName, Context.MODE_PRIVATE)
        IShare?.edit()?.clear()?.apply()
    }


    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    ////// Remove EnMemberInfo When Logout User
    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    fun RemoveDataWhenLogOut() {
        removeValue(DEVICE_ID)
    }

    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    ////// Get Set By Other Key
    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    fun setValueLong(key: String, `val`: Long) {
        IShare?.edit()?.putLong(key, `val`)?.apply()
    }

    fun setValueString(key: String?, `val`: String?) {
        IShare?.edit()?.putString(key, `val`)?.apply()
    }

    fun setValueBoolean(key: String, `val`: Boolean) {
        IShare?.edit()?.putBoolean(key, `val`)?.apply()
    }

    fun setValueInteger(key: String, `val`: Int) {
        IShare?.edit()?.putInt(key, `val`)?.apply()
    }

    fun getValueInteger(key: String): Int? {
        return IShare?.getInt(key, -1)
    }

    fun getValueLong(key: String): Long? {
        return IShare?.getLong(key, -1)
    }

    fun getValueBoolean(key: String): Boolean? {
        return IShare?.getBoolean(key, false)
    }

    fun getValueString(key: String?): String? {
        return IShare?.getString(key, "")?.trim { it <= ' ' }
    }

    fun getValueString(key: String?, value: String?): String? {
        return IShare?.getString(key, value)?.trim { it <= ' ' }
    }

    fun removeValue(key: String) {
        IShare?.edit()?.remove(key)?.apply()
    }

    fun setLanguage(data: String?) {
        setValueString(KEY_LANGUAGE, data)
    }

    fun getLanguage(): String {
        return IShare?.getString(KEY_LANGUAGE, AppConfig.ID_LANGUAGE_ENGLISH)
            ?: AppConfig.ID_LANGUAGE_ENGLISH
    }

    fun setThemeApp(data: String?) {
        setValueString(KEY_THEME_APP, data)
    }

    fun getThemeApp(): String {
        return IShare?.getString(KEY_THEME_APP, AppConfig.THEME_1)
            ?: AppConfig.THEME_1
    }

    fun removeRecentFile(data: MyFilesModel) {
        val lstRecent = getRecentFile()
        if (lstRecent.contains(data)) {
            lstRecent.remove(data)
        }
        val strData = Gson().toJson(lstRecent.take(100))
        setValueString(KEY_RECENT_FILE, strData)
    }

    fun setRecentFile(data: MyFilesModel) {
        val lstRecent = getRecentFile()
        if (lstRecent.contains(data)) {
            lstRecent.remove(data)
        }
        lstRecent.add(0, data)
        val strData = Gson().toJson(lstRecent.take(50))
        setValueString(KEY_RECENT_FILE, strData)
    }

    fun getRecentFile(): ArrayList<MyFilesModel> {
        val data = getValueString(KEY_RECENT_FILE)
        if (data.isNullOrEmpty()) return ArrayList()
        return Gson().fromJson<ArrayList<MyFilesModel>>(
            data,
            object : TypeToken<ArrayList<MyFilesModel>>() {}.type
        )
            ?: ArrayList()
    }

    fun removeFavoriteFile(data: MyFilesModel) {
        val lstRecent = getFavoriteFile()
        if (lstRecent.contains(data)) {
            lstRecent.remove(data)
        }
        val strData = Gson().toJson(lstRecent.take(100))
        setValueString(KEY_FAVORITE_FILE, strData)
    }

    fun setFavoriteFile(data: MyFilesModel) {
        val lstRecent = getFavoriteFile()
        if (lstRecent.contains(data)) {
            lstRecent.remove(data)
        }
        lstRecent.add(0, data)
        val strData = Gson().toJson(lstRecent.take(100))
        setValueString(KEY_FAVORITE_FILE, strData)
    }

    fun getFavoriteFile(): ArrayList<MyFilesModel> {
        val data = getValueString(KEY_FAVORITE_FILE)
        if (data.isNullOrEmpty()) return ArrayList()
        return Gson().fromJson<ArrayList<MyFilesModel>>(
            data,
            object : TypeToken<ArrayList<MyFilesModel>>() {}.type
        )
            ?: ArrayList()
    }

    companion object {

        const val DEVICE_ID = "device_id"
        const val KEY_LANGUAGE = "KEY_LANGUAGE"
        const val KEY_TYPE_VIEW_FILE = "KEY_TYPE_VIEW_FILE"
        const val KEY_THEME_APP = "KEY_THEME_APP"
        const val KEY_FIRST_LOGIN = "KEY_FIRST_LOGIN"
        const val KEY_RECENT_FILE = "KEY_RECENT_FILE"
        const val KEY_FAVORITE_FILE = "KEY_FAVORITE_FILE"
    }

}

