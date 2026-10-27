package dev.patrickgold.florisboard.ime.theme

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * The default day/night theme ids in AppPrefs must name components that exist in the bundled
 * theme extension; otherwise the keyboard silently falls back to the hard-coded base style.
 */
class DefaultThemeIdsTest : FreeSpec({
    val ext = File("src/main/assets/ime/theme/org.florisboard.themes/extension.json").readText()
    val ids = Regex("\"id\":\\s*\"([a-z_]+)\"").findAll(ext).map { it.groupValues[1] }.toList()
    val prefs = File("src/main/kotlin/dev/patrickgold/florisboard/app/AppPrefs.kt").readText()
    val defaults = Regex("default = extCoreTheme\\(\"([a-z_]+)\"\\)").findAll(prefs).map { it.groupValues[1] }.toList()

    "every default theme id exists in the bundled extension" {
        defaults.size shouldBe 2
        defaults.forEach { ids shouldContain it }
    }
})
