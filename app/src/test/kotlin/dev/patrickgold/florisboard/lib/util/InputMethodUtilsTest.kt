package dev.patrickgold.florisboard.lib.util

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

/**
 * Regression: the setup wizard read Settings.Secure.ENABLED_INPUT_METHODS, which returns null for
 * apps targeting recent SDKs, so it never advanced. IME state now comes from InputMethodManager and
 * is matched with isFlorisboardComponent().
 */
class InputMethodUtilsTest : FreeSpec({
    val pkg = "com.bumbumapps.keyboardpro"
    "matches our IME service under the release package name" {
        InputMethodUtils.isFlorisboardComponent(pkg, pkg, "dev.patrickgold.florisboard.FlorisImeService") shouldBe true
    }
    "rejects other keyboards and other packages" {
        InputMethodUtils.isFlorisboardComponent(pkg, "com.google.android.inputmethod.latin",
            "com.android.inputmethod.latin.LatinIME") shouldBe false
        InputMethodUtils.isFlorisboardComponent(pkg, "dev.patrickgold.florisboard",
            "dev.patrickgold.florisboard.FlorisImeService") shouldBe false
        InputMethodUtils.isFlorisboardComponent(pkg, null, null) shouldBe false
    }
})
