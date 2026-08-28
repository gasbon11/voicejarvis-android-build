package com.voicejarvis.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.voicejarvis.app.engine.CommandInterpreter
import com.voicejarvis.app.engine.FuzzyMatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * VoiceJarvis Core Accessibility Service
 * Telefonun o anki aktif ekranındaki tüm bileşenleri okur ve sesli komutlarla kontrol eder.
 */
class VoiceAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        var instance: VoiceAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Ekran değişimlerini sessizce dinler
    }

    override fun onInterrupt() {
        // Kesilme durumunda kaynakları temizler
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /**
     * Ekranda verilen metne veya içeriğe en uygun düğümü bulur (Fuzzy Match >= 0.70)
     */
    fun findMatchingNode(targetText: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val candidates = mutableListOf<AccessibilityNodeInfo>()
        collectAllNodes(rootNode, candidates)

        var bestNode: AccessibilityNodeInfo? = null
        var highestScore = 0.0f

        for (node in candidates) {
            val nodeText = node.text?.toString() ?: ""
            val desc = node.contentDescription?.toString() ?: ""
            val viewId = node.viewIdResourceName?.substringAfterLast("/") ?: ""

            val textScore = FuzzyMatcher.calculateSimilarity(targetText, nodeText)
            val descScore = FuzzyMatcher.calculateSimilarity(targetText, desc)
            val idScore = FuzzyMatcher.calculateSimilarity(targetText, viewId)

            val maxNodeScore = maxOf(textScore, descScore, idScore)

            if (maxNodeScore > highestScore && maxNodeScore >= 0.70f) {
                highestScore = maxNodeScore
                bestNode = node
            }
        }

        return bestNode
    }

    /**
     * Hedef elemana tıklar
     */
    fun clickNode(node: AccessibilityNodeInfo): Boolean {
        var target: AccessibilityNodeInfo? = node
        while (target != null && !target.isClickable) {
            target = target.parent
        }
        return target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
    }

    /**
     * Odaklanılan veya hedef metin kutusuna yazı yazar
     */
    fun typeTextIntoNode(node: AccessibilityNodeInfo, textToType: String): Boolean {
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToType)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    /**
     * Sistem seviyesinde yukarı/aşağı kaydırma jesti simüle eder
     */
    fun performScroll(direction: ScrollDirection): Boolean {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val startX = (width / 2).toFloat()
        val startY = if (direction == ScrollDirection.DOWN) (height * 0.75f) else (height * 0.25f)
        val endY = if (direction == ScrollDirection.DOWN) (height * 0.25f) else (height * 0.75f)

        val swipePath = Path().apply {
            moveTo(startX, startY)
            lineTo(startX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(swipePath, 0, 300))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    /**
     * Ekrandaki tüm görünür metinleri toplar (Gemini/OpenAI bağlamı için)
     */
    fun extractScreenContext(): List<String> {
        val rootNode = rootInActiveWindow ?: return emptyList()
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        collectAllNodes(rootNode, nodes)

        return nodes.mapNotNull { node ->
            // Şifre alanlarını gizlilik için kesinlikle filtrele
            if (node.isPassword) return@mapNotNull null
            node.text?.toString() ?: node.contentDescription?.toString()
        }.filter { it.isNotBlank() }
    }

    private fun collectAllNodes(node: AccessibilityNodeInfo, list: MutableList<AccessibilityNodeInfo>) {
        list.add(node)
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                collectAllNodes(child, list)
            }
        }
    }

    enum class ScrollDirection { UP, DOWN }
}