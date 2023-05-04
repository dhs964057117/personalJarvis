package com.cabbage.jarvis.service

import com.cabbage.jarvis.R
import ai.picovoice.porcupine.*
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cabbage.jarvis.ui.MainActivity

class PorcupineService : Service() {
    private var porcupineManager: PorcupineManager? = null
    private var numUtterances = 0
    private val porcupineManagerCallback = PorcupineManagerCallback { keywordIndex: Int ->
        numUtterances++
        val contentText = if (numUtterances == 1) " time!" else " times!"
        val n = getNotification(
            "Wake word",
            "Detected $numUtterances$contentText"
        )
        val notificationManager =
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.notify(1234, n)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Porcupine",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        numUtterances = 0
        createNotificationChannel()
        try {
            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(ACCESS_KEY)
                .setKeyword(Porcupine.BuiltInKeyword.JARVIS)
                .setSensitivity(0.7f).build(
                    applicationContext,
                    porcupineManagerCallback
                )
            porcupineManager?.start()
        } catch (e: PorcupineInvalidArgumentException) {
            onPorcupineInitError(
                String.format(
                    "%s\nEnsure your accessKey '%s' is a valid access key.",
                    e.message,
                    ACCESS_KEY
                )
            )
        } catch (e: PorcupineActivationException) {
            onPorcupineInitError("AccessKey activation error")
        } catch (e: PorcupineActivationLimitException) {
            onPorcupineInitError("AccessKey reached its device limit")
        } catch (e: PorcupineActivationRefusedException) {
            onPorcupineInitError("AccessKey refused")
        } catch (e: PorcupineActivationThrottledException) {
            onPorcupineInitError("AccessKey has been throttled")
        } catch (e: PorcupineException) {
            onPorcupineInitError("Failed to initialize Porcupine " + e.message)
        }
        val notification = if (porcupineManager == null) getNotification(
            "Porcupine init failed",
            "Service will be shut down"
        ) else getNotification("Wake word", "Service running")
        startForeground(1234, notification)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun onPorcupineInitError(message: String) {
        val i = Intent("PorcupineInitError")
        i.putExtra("errorMessage", message)
        sendBroadcast(i)
    }

    private fun getNotification(title: String, message: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_MUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground_porcupine)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        if (porcupineManager != null) {
            try {
                porcupineManager!!.stop()
                porcupineManager!!.delete()
            } catch (e: PorcupineException) {
                Log.e("PORCUPINE", e.toString())
            }
        }
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "PorcupineServiceChannel"
        private const val ACCESS_KEY = "fSzRJYidSJnwls97234uZNpRuL8j4jvxzE2eqoefFA2ghsUTk6h26A=="
    }
}