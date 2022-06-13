package com.igi.office.common
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract

class StoragePermissionContract() : ActivityResultContract<String, Boolean>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(input)
    }


    @SuppressLint("NewApi")
    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        Log.e("resultCode", resultCode.toString())
        Log.e("intent", intent?.action.toString())
        return when {
            resultCode == Activity.RESULT_OK -> {
                true
            }
            intent?.action == null -> Environment.isExternalStorageManager()

            else -> false
        }

    }

}
class AppSettingsContracts() : ActivityResultContract<String, Boolean>() {
    override fun createIntent(context: Context, input: String): Intent {
        val intent = Intent(input)
            .setData(Uri.parse("package:" + context.packageName))
        return intent
    }


    @SuppressLint("NewApi")
    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        Log.e("resultCode", resultCode.toString())
        Log.e("intent", intent?.action.toString())
        return when {
            resultCode == Activity.RESULT_OK -> {
                true
            }
            intent?.action == null -> Environment.isExternalStorageManager()

            else -> false
        }

    }

//    class GoToReadActivityContract() : ActivityResultContract<String, Unit>() {
//        override fun createIntent(context: Context, input: String): Intent {
//            return Intent(context, ReadingActivity::class.java)
//                .putExtra("document", input)
//        }
//
//
//        @SuppressLint("NewApi")
//        override fun parseResult(resultCode: Int, intent: Intent?) {
//
//
//        }
//
//    }

}