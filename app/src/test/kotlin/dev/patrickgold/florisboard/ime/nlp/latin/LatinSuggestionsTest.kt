package dev.patrickgold.florisboard.ime.nlp.latin

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

/** Card #65: suggestions showed placeholder words ("hello0", "hello1") instead of dictionary words. */
class LatinSuggestionsTest : FreeSpec({
    val words = mapOf("hello" to 200, "help" to 230, "helmet" to 90, "he" to 254, "world" to 250)

    "Latin prefix suggestions" - {
        "come from the dictionary, most frequent first" {
            LatinLanguageProvider.prefixSuggestions(words, "hel", 3) shouldBe listOf("help", "hello", "helmet")
        }
        "never produce numbered placeholders for a complete word" {
            LatinLanguageProvider.prefixSuggestions(words, "hello", 3).shouldBeEmpty()
        }
        "keep the input's capitalization and the limit" {
            LatinLanguageProvider.prefixSuggestions(words, "Hel", 1) shouldBe listOf("Help")
            LatinLanguageProvider.prefixSuggestions(words, "HEL", 2) shouldBe listOf("HELP", "HELLO")
        }
        "hide profanity unless offensive words are allowed" {
            val dict = mapOf("fuck" to 200, "fucking" to 190, "fun" to 180, "cocktail" to 100, "cock" to 120)
            LatinLanguageProvider.prefixSuggestions(dict, "fu", 5) shouldBe listOf("fun")
            LatinLanguageProvider.prefixSuggestions(dict, "co", 5) shouldBe listOf("cocktail")
            LatinLanguageProvider.prefixSuggestions(dict, "fu", 5, allowPossiblyOffensive = true) shouldBe
                listOf("fuck", "fucking", "fun")
        }
        "are empty for empty input" {
            LatinLanguageProvider.prefixSuggestions(words, "", 3).shouldBeEmpty()
        }
    }
})
