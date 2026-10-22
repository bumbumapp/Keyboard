package dev.patrickgold.florisboard.ads

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class InterstitialHelperTest : FreeSpec({
    "Frequency cap" - {
        "blocks a second ad within 60 s" {
            InterstitialHelper.capAllows(100_000L, 50_000L) shouldBe false
        }
        "allows an ad after 60 s" {
            InterstitialHelper.capAllows(110_000L, 50_000L) shouldBe true
        }
        "allows the first ad" {
            InterstitialHelper.capAllows(0L, -InterstitialHelper.MIN_INTERVAL_MS) shouldBe true
        }
    }
})
