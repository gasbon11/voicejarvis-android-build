package com.voicejarvis.app.ai

class GeminiProvider {
    fun generate(prompt: String, screenTexts: List<String>): AIProviderManager.AIResponse =
        AIProviderManager.offlineResponse(prompt)
}