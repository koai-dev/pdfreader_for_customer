package com.cocna.pdffilereader.imagepicker.helper

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

class ToastHelper {
    companion object {
        var toast: Toast? = null

        @SuppressLint("ShowToast")
        fun show(context: Context, text: String, duration: Int = Toast.LENGTH_SHORT) {
            if (toast == null) {
                toast = Toast.makeText(context.applicationContext, text, duration)
            } else {
                toast?.cancel()
                toast?.setText(text)
            }
            toast?.show()
        }
    }
}