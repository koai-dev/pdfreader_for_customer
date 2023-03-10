package com.cocna.pdffilereader.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

object PermissionUtil {

    @SuppressLint("NewApi")
    fun checkExternalStoragePermission(context: Context): Boolean {
//        return (sdk29andMore {
//            Environment.isExternalStorageManager()
//        } ?: {
//            ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        }) == true
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager()
        }else{
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

    }

    fun <T> sdk29andMore(code: ()-> T):T?{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) code()
        else return null
    }

}