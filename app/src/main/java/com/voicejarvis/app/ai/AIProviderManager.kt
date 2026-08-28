package com.voicejarvis.app.ai

import com.voicejarvis.app.security.KeyStoreVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Çoklu AI Sağlayıcı Katmanı (OpenAI + Gemini)
 * Biri hata verirse otomatik diğerine geçer, API anahtarlarını Keystore'da şifreli tutar.
 */
object AIProviderManager {

    data class AIResponse(
        val spokenText: String,
        val actionType: String,
        val payload: String,
        val requiresConfirmation: Boolean,
        val providerUsed: String
    )

    private val geminiProvider = GeminiProvider()
    private val openAIProvider = OpenAIProvider()

    internal fun offlineResponse(prompt: String): AIResponse = AIResponse(
        spokenText = "Bu sürüm temel sesli komutları ve ekran erişimini destekliyor. Komut: $prompt",
        actionType = "INFO",
        payload = "",
        requiresConfirmation = false,
        providerUsed = "Offline"
    )

    suspend fun processGenerativeTask(prompt: String, screenTexts: List<String>): AIResponse = withContext(Dispatchers.IO) {
        val preferredProvider = KeyStoreVault.getPreferredProvider() // "gemini" or "openai"

        try {
            if (preferredProvider == "openai") {
                return@withContext openAIProvider.generate(prompt, screenTexts)
            } else {
                return@withContext geminiProvider.generate(prompt, screenTexts)
            }
        } catch (e: Exception) {
            // Otomatik Fallback (B planına geçiş)
            return@withContext try {
                if (preferredProvider == "openai") {
                    geminiProvider.generate(prompt, screenTexts)
                } else {
                    openAIProvider.generate(prompt, screenTexts)
                }
            } catch (fallbackError: Exception) {
                AIResponse(
                    spokenText = "İnternet bağlantınızı veya API kotanızı kontrol edin.",
                    actionType = "ERROR",
                    payload = "",
                    requiresConfirmation = false,
                    providerUsed = "Offline Safe Fallback"
                )
            }
        }
    }
}