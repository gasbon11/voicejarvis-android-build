package com.voicejarvis.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object TurkishTTSManager {
    private var textToSpeech: TextToSpeech? = null

    fun init(context: Context) {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("tr", "TR")
            }
        }
    }

    fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "voicejarvis-response")
    }

    fun playWakeChime() = speak("Dinliyorum.")
    fun playProcessingTone() = Unit
}