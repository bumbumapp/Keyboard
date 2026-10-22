package dev.patrickgold.florisboard.lib.util

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

/**
 * Regression: getUrls()/getEmailAddresses() used named-group lookup (Matcher.start(String), API 26+)
 * which crashed on Android 7.x (minSdk 24). They now use the group index.
 */
class NetworkUtilsTest : FreeSpec({
    "getUrls extracts the whole URL" {
        NetworkUtils.getUrls("see https://example.com/a?b=1 now").map { it.value } shouldBe
            listOf("https://example.com/a?b=1")
    }
    "getEmailAddresses extracts the whole address" {
        NetworkUtils.getEmailAddresses("mail jane.doe@example.org please").map { it.value } shouldBe
            listOf("jane.doe@example.org")
    }
})
