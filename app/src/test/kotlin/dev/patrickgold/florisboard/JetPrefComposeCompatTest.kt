package dev.patrickgold.florisboard

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

/**
 * Regression test for the release crash in Theme mode / Add subtype (Huawei review, 1.4.1).
 *
 * JetPref beta12 was compiled against Compose UI 1.1 and calls the removed constructor
 * DialogProperties(ZZSecureFlagPolicy;Z)V ($default form). With Compose 1.9 every JetPref dialog
 * threw NoSuchMethodError. A library compiled against a newer Compose must not reference it.
 */
class JetPrefComposeCompatTest : FreeSpec({
    "JetPref dialogs do not reference the removed DialogProperties constructor" {
        val removedCtor = "(ZZLandroidx/compose/ui/window/SecureFlagPolicy;ZILkotlin/jvm/internal/DefaultConstructorMarker;)V"
        val clazz = Class.forName("dev.patrickgold.jetpref.material.ui.JetPrefAlertDialogKt")
        val bytes = clazz.getResourceAsStream("JetPrefAlertDialogKt.class")!!.use { it.readBytes() }
        String(bytes, Charsets.ISO_8859_1).contains(removedCtor) shouldBe false
    }
})
