package com.voicejarvis.app

import android.app.Application
import com.voicejarvis.app.engine.AppLauncher
import com.voicejarvis.app.security.KeyStoreVault
import com.voicejarvis.app.stt.TurkishSpeechManager
import com.voicejarvis.app.tts.TurkishTTSManager

class VoiceJarvisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KeyStoreVault.init(this)
        AppLauncher.init(this)
        TurkishSpeechManager.init(this)
        TurkishTTSManager.init(this)
    }
}