package com.cocna.pdffilereader.ui.drive.dropbox

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cocna.pdffilereader.common.SharePreferenceUtils
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential

class DropboxCredentialUtil(val appContext: Context) {

    fun readCredentialLocally(): DbxCredential? {
        val sharedPreferences = SharePreferenceUtils(appContext)
        val serializedCredentialJson = sharedPreferences.getValueString(SharePreferenceUtils.PREF_DROPBOX_CREDENTIAL, null)
        Log.d(TAG, "Local Credential Value from Shared Preferences: $serializedCredentialJson")
        return try {
            DbxCredential.Reader.readFully(serializedCredentialJson)
        } catch (e: Exception) {
            Log.d(TAG, "Something went wrong parsing the credential, clearing it")
            removeCredentialLocally()
            null
        }
    }

    //serialize the credential and store in SharedPreferences
    fun storeCredentialLocally(dbxCredential: DbxCredential) {
        Log.d(TAG, "Storing credential in Shared Preferences")
        val sharedPreferences = SharePreferenceUtils(appContext)
        sharedPreferences.setValueString(SharePreferenceUtils.PREF_DROPBOX_CREDENTIAL, DbxCredential.Writer.writeToString(dbxCredential))
    }

    fun removeCredentialLocally() {
        Log.d(TAG, "Clearing credential from Shared Preferences")
        val sharedPreferences = SharePreferenceUtils(appContext)
        sharedPreferences.removeValue(SharePreferenceUtils.PREF_DROPBOX_CREDENTIAL)
    }

    fun isAuthenticated(): Boolean {
        return readCredentialLocally() != null
    }

    private companion object {
        private val TAG = DropboxCredentialUtil::class.java.simpleName
    }
}