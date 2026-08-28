package com.voicejarvis.app.engine

import java.text.Normalizer
import java.util.Locale
import kotlin.math.max

object FuzzyMatcher {
    data class MessageCommand(val targetPerson: String, val messageBody: String)

    fun normalize(value: String): String {
        val lowered = value.lowercase(Locale("tr", "TR"))
            .replace('ı', 'i')
            .replace('ş', 's')
            .replace('ğ', 'g')
            .replace('ü', 'u')
            .replace('ö', 'o')
            .replace('ç', 'c')
        return Normalizer.normalize(lowered, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .replace("[^a-z0-9 ]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    fun calculateSimilarity(left: String, right: String): Float {
        if (left.isBlank() || right.isBlank()) return 0f
        val a = normalize(left)
        val b = normalize(right)
        if (a == b) return 1f
        val distance = levenshtein(a, b)
        return 1f - distance.toFloat() / max(a.length, b.length).coerceAtLeast(1)
    }

    fun extractAppOpenTarget(command: String): String? {
        val match = Regex("""(?:uygulamayı|uygulamayi|uygulama|app)\s+(.+?)(?:\s+a[çc]|$)""")
            .find(command)
        return match?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    fun extractMessageCommand(command: String): MessageCommand? {
        val match = Regex("""(.+?)['"](.+?)['"]""").find(command) ?: return null
        val target = command.substringBefore(match.range.first).trim()
            .removePrefix("mesaj gönder")
            .removePrefix("mesaj gonder")
            .removePrefix("whatsapp")
            .trim()
        return target.takeIf { it.isNotBlank() }?.let { MessageCommand(it, match.groupValues[1]) }
    }

    private fun levenshtein(a: String, b: String): Int {
        var previous = IntArray(b.length + 1) { it }
        for (i in a.indices) {
            val current = IntArray(b.length + 1)
            current[0] = i + 1
            for (j in b.indices) {
                current[j + 1] = minOf(
                    current[j] + 1,
                    previous[j + 1] + 1,
                    previous[j] + if (a[i] == b[j]) 0 else 1
                )
            }
            previous = current
        }
        return previous[b.length]
    }
}