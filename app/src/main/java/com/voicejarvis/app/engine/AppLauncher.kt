package com.voicejarvis.app.engine

import android.content.Context
import android.content.Intent

object AppLauncher {
    private var context: Context? = null

    fun init(appContext: Context) {
        context = appContext.applicationContext
    }

    fun launchApp(target: String): Boolean {
        val appContext = context ?: return false
        val normalizedTarget = FuzzyMatcher.normalize(target)
        val packageManager = appContext.packageManager
        val candidates = packageManager.getInstalledApplications(0)
        val match = candidates.firstOrNull {
            val label = packageManager.getApplicationLabel(it).toString()
            FuzzyMatcher.calculateSimilarity(normalizedTarget, label) >= 0.70f
        } ?: return false
        val launchIntent = packageManager.getLaunchIntentForPackage(match.packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(launchIntent)
        return true
    }
}