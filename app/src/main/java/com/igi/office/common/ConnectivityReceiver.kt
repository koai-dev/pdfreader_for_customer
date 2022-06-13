package com.igi.office.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class ConnectivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(isConnectedOrConnecting(context!!))
        }

    }

    private fun isConnectedOrConnecting(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val networkInfo = manager!!.activeNetworkInfo
        var isAvailable = false
        if (networkInfo != null && networkInfo.isConnected) {
            isAvailable = true
        }
        return isAvailable
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
    }
}