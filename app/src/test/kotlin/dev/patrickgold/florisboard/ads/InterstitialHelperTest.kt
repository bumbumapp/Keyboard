package dev.patrickgold.florisboard.ads

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class InterstitialHelperTest : FreeSpec({
    val hour = 60 * 60_000L
    val t0 = 1_700_000_000_000L

    "Frequency cap" - {
        "is one hour" {
            InterstitialHelper.MIN_INTERVAL_MS shouldBe hour
        }
        "blocks ads during the first hour after install" {
            InterstitialHelper.capAllows(t0 + 59 * 60_000L, 0L, t0) shouldBe false
            InterstitialHelper.capAllows(t0 + hour, 0L, t0) shouldBe true
        }
        "blocks a second ad within the hour" {
            val shown = t0 + 2 * hour
            InterstitialHelper.capAllows(shown + 10 * 60_000L, shown, t0) shouldBe false
            InterstitialHelper.capAllows(shown + hour, shown, t0) shouldBe true
        }
    }
})
