package com.igi.office.common

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.igi.office.BuildConfig

object Logger {
    fun showToast(context: Context, message: String?) {
//        if (BuildConfig.DEBUG) {
        Toast.makeText(context, message ?: "", Toast.LENGTH_LONG).show()
//        }
    }

    fun showLog(message: String?) {
        if (BuildConfig.DEBUG) {
            Log.d("Thuytv", message?:"")
        }
    }

    fun showLog(tag: String, message: String?) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message?:"")
        }
    }

    fun showLogError(message: String?) {
        if (BuildConfig.DEBUG) {
            Log.e("Thuytv", message?:"")
        }
    }

    fun showSnackbar(context: Context, message: String?) {
        if (context is Activity) {
            Snackbar.make(
                context.findViewById(android.R.id.content),
                message ?: "",
                Snackbar.LENGTH_INDEFINITE
            ).show()
        }

    }
}