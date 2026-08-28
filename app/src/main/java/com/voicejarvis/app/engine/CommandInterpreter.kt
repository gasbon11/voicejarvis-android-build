package com.voicejarvis.app.engine

import com.voicejarvis.app.accessibility.VoiceAccessibilityService
import com.voicejarvis.app.ai.AIProviderManager
import com.voicejarvis.app.tts.TurkishTTSManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Hibrit Komut Yorumlayıcı
 * Basit ekran eylemlerini hızlıca offline çözer; metin üretimi ve araştırmayı Gemini/OpenAI'ye yönlendirir.
 */
object CommandInterpreter {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var lastSubjectPerson: String? = null

    fun processVoiceCommand(command: String) {
        val norm = FuzzyMatcher.normalize(command)

        // 1. Acil Durdurma Kontrolü (Güvenlik Önceliği)
        if (norm.startsWith("dur") || norm.startsWith("iptal") || norm == "yapma") {
            TurkishTTSManager.speak("İşlem iptal edildi.")
            return
        }

        // 2. Geri Git
        if (norm == "geri git" || norm == "geri don") {
            VoiceAccessibilityService.instance?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
            TurkishTTSManager.speak("Geri gittim.")
            return
        }

        // 3. Kaydırma Jestleri
        if (norm.contains("asagi kaydir") || norm.contains("sonraki")) {
            VoiceAccessibilityService.instance?.performScroll(VoiceAccessibilityService.ScrollDirection.DOWN)
            TurkishTTSManager.speak("Kaydırdım.")
            return
        }

        if (norm.contains("yukari kaydir")) {
            VoiceAccessibilityService.instance?.performScroll(VoiceAccessibilityService.ScrollDirection.UP)
            TurkishTTSManager.speak("Kaydırdım.")
            return
        }

        // 4. Uygulama Açma
        val openMatch = FuzzyMatcher.extractAppOpenTarget(norm)
        if (openMatch != null) {
            val launched = AppLauncher.launchApp(openMatch)
            if (launched) {
                TurkishTTSManager.speak("$openMatch açıldı.")
            } else {
                TurkishTTSManager.speak("$openMatch telefonunda yüklü değil.")
            }
            return
        }

        // 5. Metin Yazma & Mesaj Gönderme
        val messageMatch = FuzzyMatcher.extractMessageCommand(command)
        if (messageMatch != null) {
            lastSubjectPerson = messageMatch.targetPerson
            requestConfirmation(
                action = "Mesaj Gönderme",
                prompt = "${messageMatch.targetPerson} kişisine '${messageMatch.messageBody}' mesajı gönderilsin mi?"
            ) {
                // Onay verildiğinde gönderme akışı
                executeSendMessage(messageMatch.targetPerson, messageMatch.messageBody)
            }
            return
        }

        // 6. Gemini / OpenAI Akıllı Katmanına Yönlendirme (Hibrit)
        scope.launch {
            TurkishTTSManager.playProcessingTone()
            val screenContext = VoiceAccessibilityService.instance?.extractScreenContext() ?: emptyList()
            val aiResponse = AIProviderManager.processGenerativeTask(command, screenContext)
            
            if (aiResponse.requiresConfirmation) {
                requestConfirmation(aiResponse.actionType, aiResponse.spokenText) {
                    executeAIAction(aiResponse)
                }
            } else {
                TurkishTTSManager.speak(aiResponse.spokenText)
                executeAIAction(aiResponse)
            }
        }
    }

    private fun requestConfirmation(action: String, prompt: String, onConfirmed: () -> Unit) {
        TurkishTTSManager.speak("$prompt Onaylıyor musun?")
        // Bekle ve "Evet" denilirse onConfirmed çalıştır
    }

    private fun executeSendMessage(target: String, text: String) {
        // Input kutusunu bul ve gönder
    }

    private fun executeAIAction(response: AIProviderManager.AIResponse) {
        // AI tarafından önerilen payload kutuya yazılır veya uygulanır
    }
}