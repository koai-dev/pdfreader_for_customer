package com.cocna.pdffilereader.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat

class StoragePermissionContract() : ActivityResultContract<String, Boolean>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(input)
    }


    @SuppressLint("NewApi")
    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return when {
            resultCode == Activity.RESULT_OK -> {
                true
            }
            intent?.action == null -> Environment.isExternalStorageManager()

            else -> false
        }

    }

}

class AppSettingsContracts(private val mContext: Context?) : ActivityResultContract<String, Boolean>() {
    override fun createIntent(context: Context, input: String): Intent {
        val intent = Intent(input)
            .setData(Uri.parse("package:" + context.packageName))
        return intent
    }


    @SuppressLint("NewApi")
    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return when {
            resultCode == Activity.RESULT_OK -> {
                true
            }
            intent?.action == null -> {
                if (mContext != null) {
                    ContextCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    false
                }
            }
            else -> false
        }

    }

}