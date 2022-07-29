package com.cocna.pdffilereader.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.TaskStackBuilder
import android.content.ClipData.newIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cocna.pdffilereader.BuildConfig
import com.cocna.pdffilereader.MainActivity
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.ui.home.SplashScreenActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by Thuytv on 29/07/2022.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
//        super.onMessageReceived(message)
        showNotification(applicationContext, message.notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.showLog("Thuytv---token: $token")
    }

    val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

    /**
     * @author Nav Singh
     */
    fun showNotification(context: Context, mNotification: RemoteMessage.Notification?) {
        val mTitle = mNotification?.title ?: "PDF Reader"
        val mDescription = mNotification?.body ?: ""

        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
        ) {
            val name = context.getString(R.string.app_name)
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, SplashScreenActivity::class.java)

        // create a pending intent that opens MainActivity when the user clicks on the notification
        val stackBuilder = TaskStackBuilder.create(context)
            .addParentStack(SplashScreenActivity::class.java)
            .addNextIntent(intent)
        val notificationPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            stackBuilder
                .getPendingIntent(getUniqueId(), FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(
                context, getUniqueId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

//    build the notification object with the data to be shown
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(mTitle)
            .setContentText(mDescription)
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(getUniqueId(), notification)
    }

    private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())
}