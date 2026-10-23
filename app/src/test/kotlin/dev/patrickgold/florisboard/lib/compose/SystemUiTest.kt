package dev.patrickgold.florisboard.lib.compose

import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class SystemUiTest : FreeSpec({
    "System bar icon appearance" - {
        "dark background of the 'light' app palette gets light icons" {
            useDarkSystemBarIcons(Color(0xFF221F35)) shouldBe false
        }
        "dark palette background gets light icons" {
            useDarkSystemBarIcons(Color(0xFF1F1F1F)) shouldBe false
            useDarkSystemBarIcons(Color.Black) shouldBe false
        }
        "white background gets dark icons" {
            useDarkSystemBarIcons(Color.White) shouldBe true
            useDarkSystemBarIcons(Color(0xFFF5F5F5)) shouldBe true
        }
    }
})
