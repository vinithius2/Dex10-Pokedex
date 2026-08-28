package com.vinithius.dex10.service

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Locale

private const val REGISTER_DEVICE_URL =
    "https://universal-mcp-push-notification.marcos-vinithius.workers.dev/register-device"

/**
 * Envia o token FCM para o servidor MCP universal de push notifications.
 * O servidor se encarrega de subscrever o device nos tópicos FCM corretos
 * (lang_XX, country_XX, all_users) via FCM IID API — sem código no app.
 *
 * Chamado em dois momentos:
 *  • onNewToken() — quando o FCM gera/renova o token
 *  • Startup com versão nova — para garantir registro após update do app
 */
fun registerDeviceOnMcpServer(token: String) {
    val locale = Locale.getDefault().language      // ex: "pt", "en"
    val country = Locale.getDefault().country      // ex: "BR", "US"
    val label = "android-${token.take(8)}"

    val json = """{"app":"dex10","label":"$label","token":"$token","locale":"$locale","countryCode":"$country"}"""

    val client = OkHttpClient()
    val body = json.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(REGISTER_DEVICE_URL)
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
