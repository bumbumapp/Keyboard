package dev.patrickgold.florisboard.lib.ext

import dev.patrickgold.florisboard.lib.snygg.value.SnyggDpShapeValue
import dev.patrickgold.florisboard.lib.snygg.value.SnyggPercentShapeValue
import dev.patrickgold.florisboard.lib.snygg.value.SnyggSolidColorValue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

/**
 * Regression: rules named like their Snygg value class referenced the (not yet initialised)
 * property instead of the class, crashing the keyboard theme on start (NPE in <clinit>).
 */
class ExtensionValidationTest : FreeSpec({
    "Rules named after their value class point at the class" {
        ExtensionValidation.SnyggSolidColorValue.klass shouldBe SnyggSolidColorValue::class
        ExtensionValidation.SnyggDpShapeValue.klass shouldBe SnyggDpShapeValue::class
        ExtensionValidation.SnyggPercentShapeValue.klass shouldBe SnyggPercentShapeValue::class
    }
})
