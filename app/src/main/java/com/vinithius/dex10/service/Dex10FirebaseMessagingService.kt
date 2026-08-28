package com.vinithius.dex10.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vinithius.dex10.R
import com.vinithius.dex10.ui.MainActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Locale

class Dex10FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body, remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM Token", token)
        registerDeviceOnServer(token)
    }

    private fun registerDeviceOnServer(token: String) {
        val locale = Locale.getDefault().language      // ex: "pt", "en"
        val country = Locale.getDefault().country      // ex: "BR", "US"
        val label = "android-${token.take(8)}"

        val json = """{"app":"dex10","label":"$label","token":"$token","locale":"$locale","countryCode":"$country"}"""

        val client = OkHttpClient()
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://universal-mcp-push-notification.marcos-vinithius.workers.dev/register-device")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCM", "Erro ao registrar device: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        Log.d("FCM", "Device registrado com sucesso: ${it.body?.string()}")
                    } else {
                        Log.e("FCM", "Falha ao registrar device: HTTP ${it.code}")
                    }
                }
            }
        })
    }

    private fun sendNotification(title: String?, messageBody: String?, data: Map<String, String>) {
        val intent: Intent
        val route = when {
            data.containsKey("deeplink") -> data["deeplink"]
            data.containsKey("url") -> data["url"]
            else -> null
        }

        intent = if (route != null) {
            Intent(Intent.ACTION_VIEW, route.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, "default_channel")
            .setSmallIcon(R.drawable.ic_notification_dex10)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}
