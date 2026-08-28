package com.voicejarvis.app.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * OpenAI ve Gemini API anahtarlarını düz metin yerine Android Keystore ile şifreli saklar.
 */
object KeyStoreVault {

    private const val PREFS_FILE = "voicejarvis_secure_vault"
    private var securePrefs: SharedPreferences? = null

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        securePrefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveGeminiKey(key: String) {
        securePrefs?.edit()?.putString("KEY_GEMINI", key)?.apply()
    }

    fun getGeminiKey(): String? = securePrefs?.getString("KEY_GEMINI", null)

    fun saveOpenAIKey(key: String) {
        securePrefs?.edit()?.putString("KEY_OPENAI", key)?.apply()
    }

    fun getOpenAIKey(): String? = securePrefs?.getString("KEY_OPENAI", null)

    fun setPreferredProvider(provider: String) {
        securePrefs?.edit()?.putString("PREF_PROVIDER", provider)?.apply()
    }

    fun getPreferredProvider(): String = securePrefs?.getString("PREF_PROVIDER", "gemini") ?: "gemini"
}