package dev.patrickgold.florisboard

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class ImeNavBarInsetTest : FreeSpec({
    "IME navigation bar padding" - {
        "API 35+ pads the keyboard by the nav bar height" {
            imeNavBarBottomPaddingPx(35, 126) shouldBe 126
            imeNavBarBottomPaddingPx(36, 63) shouldBe 63
        }
        "below API 35 the decor already fits the nav bar" {
            imeNavBarBottomPaddingPx(30, 126) shouldBe 0
            imeNavBarBottomPaddingPx(34, 126) shouldBe 0
        }
        "no nav bar means no padding" {
            imeNavBarBottomPaddingPx(35, 0) shouldBe 0
            imeNavBarBottomPaddingPx(35, -5) shouldBe 0
        }
    }
})
