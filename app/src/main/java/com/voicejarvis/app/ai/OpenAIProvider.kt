package com.voicejarvis.app.ai

class OpenAIProvider {
    fun generate(prompt: String, screenTexts: List<String>): AIProviderManager.AIResponse =
        AIProviderManager.offlineResponse(prompt)
}