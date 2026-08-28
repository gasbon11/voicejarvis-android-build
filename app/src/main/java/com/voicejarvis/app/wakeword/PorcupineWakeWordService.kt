package com.voicejarvis.app.wakeword

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.voicejarvis.app.stt.TurkishSpeechManager
import com.voicejarvis.app.engine.CommandInterpreter

/**
 * Arka planda SADECE "Jarvis" kelimesini dinleyen offline motor.
 * Ekranda hiçbir rahatsız edici overlay açmaz.
 */
class PorcupineWakeWordService : Service() {

    private val CHANNEL_ID = "VoiceJarvisWakeWordChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(101, buildNotification())
        listenForCommand()
    }

    private fun listenForCommand() {
        TurkishSpeechManager.startListening(
            onResult = { recognizedText ->
                if (recognizedText.isNotBlank()) {
                    CommandInterpreter.processVoiceCommand(recognizedText)
                }
                listenForCommand()
            },
            onError = {
                // SpeechRecognizer can fail when the microphone is busy; retry quietly.
                listenForCommand()
            }
        )
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VoiceJarvis")
            .setContentText("Sessizce 'Jarvis' komutunu bekliyor...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VoiceJarvis Wake Service",
                NotificationManager.IMPORTANCE_MIN
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TurkishSpeechManager.stopListening()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}