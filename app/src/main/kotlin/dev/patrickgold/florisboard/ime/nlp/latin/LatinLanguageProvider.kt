/*
 * Copyright (C) 2022 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.ime.nlp.latin

import android.content.Context
import dev.patrickgold.florisboard.appContext
import dev.patrickgold.florisboard.ime.core.Subtype
import dev.patrickgold.florisboard.ime.editor.EditorContent
import dev.patrickgold.florisboard.ime.nlp.SpellingProvider
import dev.patrickgold.florisboard.ime.nlp.SpellingResult
import dev.patrickgold.florisboard.ime.nlp.SuggestionCandidate
import dev.patrickgold.florisboard.ime.nlp.SuggestionProvider
import dev.patrickgold.florisboard.ime.nlp.WordSuggestionCandidate
import dev.patrickgold.florisboard.lib.android.readText
import dev.patrickgold.florisboard.lib.devtools.flogDebug
import dev.patrickgold.florisboard.lib.kotlin.guardedByLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class LatinLanguageProvider(context: Context) : SpellingProvider, SuggestionProvider {
    companion object {
        // Default user ID used for all subtypes, unless otherwise specified.
        // See `ime/core/Subtype.kt` Line 210 and 211 for the default usage
        const val ProviderId = "org.florisboard.nlp.providers.latin"

        /**
         * Most frequent dictionary words that start with [input] (case-insensitive, the input itself excluded),
         * capitalized like the input. The bundled dictionary is English only.
         */
        // The bundled English word list contains profanity; hidden unless "Block possibly offensive words" is off.
        private val OffensivePrefixes = listOf(
            "fuck", "shit", "bitch", "cunt", "nigg", "fag", "slut", "whore", "porn", "asshole", "pussy",
            "dickhead", "motherf", "bastard", "wank", "twat", "bollock", "retard",
        )
        private val OffensiveWords = setOf("dick", "dicks", "cock", "cocks", "cum", "tits", "ass", "arse", "piss", "rape")

        fun isPossiblyOffensive(word: String): Boolean {
            val w = word.lowercase()
            return w in OffensiveWords || OffensivePrefixes.any { w.startsWith(it) }
        }

        fun prefixSuggestions(
            words: Map<String, Int>,
            input: String,
            max: Int,
            allowPossiblyOffensive: Boolean = false,
        ): List<String> {
            val prefix = input.lowercase()
            if (prefix.isEmpty() || max <= 0) return emptyList()
            val matches = words.entries
                .filter { it.key.length > prefix.length && it.key.startsWith(prefix) }
                .filter { allowPossiblyOffensive || !isPossiblyOffensive(it.key) }
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.length })
                .take(max)
                .map { it.key }
            return when {
                input.length > 1 && input.all { it.isUpperCase() } -> matches.map { it.uppercase() }
                input.first().isUpperCase() -> matches.map { w -> w.replaceFirstChar { it.uppercaseChar() } }
                else -> matches
            }
        }
    }

    private val appContext by context.appContext()

    private val wordData = guardedByLock { mutableMapOf<String, Int>() }
    private val wordDataSerializer = MapSerializer(String.serializer(), Int.serializer())

    override val providerId = ProviderId

    override suspend fun create() {
        // Here we initialize our provider, set up all things which are not language dependent.
    }

    override suspend fun preload(subtype: Subtype) = withContext(Dispatchers.IO) {
        // Here we have the chance to preload dictionaries and prepare a neural network for a specific language.
        // Is kept in sync with the active keyboard subtype of the user, however a new preload does not necessary mean
        // the previous language is not needed anymore (e.g. if the user constantly switches between two subtypes)

        // To read a file from the APK assets the following methods can be used:
        // appContext.assets.open()
        // appContext.assets.reader()
        // appContext.assets.bufferedReader()
        // appContext.assets.readText()
        // To copy an APK file/dir to the file system cache (appContext.cacheDir), the following methods are available:
        // appContext.assets.copy()
        // appContext.assets.copyRecursively()

        // The subtype we get here contains a lot of data, however we are only interested in subtype.primaryLocale and
        // subtype.secondaryLocales.

        wordData.withLock { wordData ->
            if (wordData.isEmpty()) {
                // Here we use readText() because the test dictionary is a json dictionary
                val rawData = appContext.assets.readText("ime/dict/data.json")
                val jsonData = Json.decodeFromString(wordDataSerializer, rawData)
                wordData.putAll(jsonData)
            }
        }
    }

    override suspend fun spell(
        subtype: Subtype,
        word: String,
        precedingWords: List<String>,
        followingWords: List<String>,
        maxSuggestionCount: Int,
        allowPossiblyOffensive: Boolean,
        isPrivateSession: Boolean,
    ): SpellingResult {
        // No real spell checking engine yet: never flag words (the sample provider marked "typo"/"gerror").
        return SpellingResult.validWord()
    }

    override suspend fun suggest(
        subtype: Subtype,
        content: EditorContent,
        maxCandidateCount: Int,
        allowPossiblyOffensive: Boolean,
        isPrivateSession: Boolean,
    ): List<SuggestionCandidate> {
        // Placeholder candidates ("hello0", "hello1"...) came from the FlorisBoard sample provider; use the dictionary.
        if (subtype.primaryLocale.language != "en") return emptyList()
        val words = wordData.withLock { it.toMap() }
        return prefixSuggestions(words, content.composingText.toString(), maxCandidateCount, allowPossiblyOffensive).map { text ->
            WordSuggestionCandidate(
                text = text,
                confidence = 0.5,
                isEligibleForAutoCommit = false,
                // We set ourselves as the source provider so we can get notify events for our candidate
                sourceProvider = this@LatinLanguageProvider,
            )
        }
    }

    override suspend fun notifySuggestionAccepted(subtype: Subtype, candidate: SuggestionCandidate) {
        // We can use flogDebug, flogInfo, flogWarning and flogError for debug logging, which is a wrapper for Logcat
        flogDebug { "notify accepted suggestion $candidate" }
    }

    override suspend fun notifySuggestionReverted(subtype: Subtype, candidate: SuggestionCandidate) {
        flogDebug { "notify reverted suggestion $candidate" }
    }

    override suspend fun removeSuggestion(subtype: Subtype, candidate: SuggestionCandidate): Boolean {
        flogDebug { "remove suggestion request $candidate" }
        return false
    }

    override suspend fun getListOfWords(subtype: Subtype): List<String> {
        return wordData.withLock { it.keys.toList() }
    }

    override suspend fun getFrequencyForWord(subtype: Subtype, word: String): Double {
        return wordData.withLock { it.getOrDefault(word, 0) / 255.0 }
    }

    override suspend fun destroy() {
        // Here we have the chance to de-allocate memory and finish our work. However this might never be called if
        // the app process is killed (which will most likely always be the case).
    }
}
