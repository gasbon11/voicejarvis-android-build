package com.voicejarvis.app

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.voicejarvis.app.wakeword.PorcupineWakeWordService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_AUDIO)
        }
    }

    private fun buildContent(): ScrollView {
        val padding = (24 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
        }

        content.addView(TextView(this).apply {
            text = "VoiceJarvis"
            textSize = 30f
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 0, padding / 2)
        })
        content.addView(TextView(this).apply {
            text = "Türkçe sesli komutlarla Android cihazınızı kontrol edin."
            textSize = 16f
            setPadding(0, 0, 0, padding)
        })

        content.addView(Button(this).apply {
            text = "Erişilebilirlik ayarlarını aç"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        })
        content.addView(Button(this).apply {
            text = "Asistanı başlat"
            setOnClickListener {
                ContextCompat.startForegroundService(
                    this@MainActivity,
                    Intent(this@MainActivity, PorcupineWakeWordService::class.java)
                )
            }
        })
        content.addView(Button(this).apply {
            text = "Asistanı durdur"
            setOnClickListener {
                stopService(Intent(this@MainActivity, PorcupineWakeWordService::class.java))
            }
        })
        content.addView(TextView(this).apply {
            text = "\nGerekli izinler:\n• Mikrofon\n• Erişilebilirlik servisi\n\nNot: Asistanı yalnızca güvendiğiniz uygulamalarda ve cihazlarda etkinleştirin."
            textSize = 14f
            setPadding(0, padding / 2, 0, 0)
        })

        return ScrollView(this).apply { addView(content) }
    }

    companion object {
        private const val REQUEST_AUDIO = 1001
    }
}