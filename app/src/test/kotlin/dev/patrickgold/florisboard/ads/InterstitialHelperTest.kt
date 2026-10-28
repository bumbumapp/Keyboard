package dev.patrickgold.florisboard.ads

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class InterstitialHelperTest : FreeSpec({
    val min = 60_000L
    val t0 = 1_700_000_000_000L

    "Frequency cap" - {
        "is 15 minutes with a 3 minute first-open grace" {
            InterstitialHelper.MIN_INTERVAL_MS shouldBe 15 * min
            InterstitialHelper.FIRST_GRACE_MS shouldBe 3 * min
        }
        "blocks ads right after install" {
            InterstitialHelper.capAllows(t0 + 3 * min - 1, 0L, t0) shouldBe false
            InterstitialHelper.capAllows(t0 + 3 * min, 0L, t0) shouldBe true
        }
        "blocks a second ad within 15 minutes" {
            val shown = t0 + 120 * min
            InterstitialHelper.capAllows(shown + 15 * min - 1, shown, t0) shouldBe false
            InterstitialHelper.capAllows(shown + 15 * min, shown, t0) shouldBe true
        }
    }
})
