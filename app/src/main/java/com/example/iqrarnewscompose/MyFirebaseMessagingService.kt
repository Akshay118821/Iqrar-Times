package com.example.iqrarnewscompose

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        val imageUrl = remoteMessage.notification?.imageUrl?.toString() ?: remoteMessage.data["image"]

        Log.d("FCM", "Title: $title")
        Log.d("FCM", "Body: $body")
        Log.d("FCM", "Image URL: $imageUrl")

        val bitmap = getBitmapFromUrl(imageUrl)

        showNotification(title, body, bitmap)
    }

    private fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
        if (imageUrl.isNullOrEmpty()) return null
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showNotification(title: String?, body: String?, bigPicture: Bitmap?) {

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "news_channel"

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title ?: "News Update")
            .setContentText(body ?: "")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (bigPicture != null) {
            builder.setLargeIcon(bigPicture)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bigPicture)
                    .bigLargeIcon(null as Bitmap?)
            )
        }

        val manager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "News Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}