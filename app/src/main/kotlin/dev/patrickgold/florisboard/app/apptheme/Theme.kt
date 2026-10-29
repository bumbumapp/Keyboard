/*
 * Copyright (C) 2021 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.app.apptheme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.patrickgold.florisboard.app.AppTheme

private val AmoledDarkColorPalette = darkColors(
    primary = Green500,
    primaryVariant = Green700,
    secondary = Orange700,
    secondaryVariant = Orange900,

    background = Color(0xFF000000),
    surface = Color(0xFF14161D),
    onPrimary = Color(0xFF1A1300),
    onSecondary = Color(0xFF1A1300),
)

private val DarkColorPalette = darkColors(
    primary = Green500,
    primaryVariant = Green700,
    secondary = Orange700,
    secondaryVariant = Orange900,

    background = Color(0xFF0D0F16),
    surface = Color(0xFF171A23),
    onPrimary = Color(0xFF1A1300),
    onSecondary = Color(0xFF1A1300),
)

private val LightColorPalette = lightColors(
    primary = Green500,
    primaryVariant = Green700,
    // Section titles, switches and links use secondary: a deep amber that reads on #F3F4F8.
    secondary = Color(0xFF9A6200),
    secondaryVariant = Color(0xFF7A4E00),


    background = Color(0xFFF3F4F8),
    surface = Color(0xFFFFFFFF),

    onPrimary = Color(0xFF1A1300),
    onSecondary = Color.White,
    onBackground = Color(0xFF0E1222),
    onSurface = Color(0xFF0E1222),
)

@Composable
fun FlorisAppTheme(
    theme: AppTheme,
    content: @Composable () -> Unit
) {
    val colors = when (theme) {
        AppTheme.AUTO -> when {
            isSystemInDarkTheme() -> DarkColorPalette
            else -> LightColorPalette
        }
        AppTheme.AUTO_AMOLED -> when {
            isSystemInDarkTheme() -> AmoledDarkColorPalette
            else -> LightColorPalette
        }
        AppTheme.LIGHT -> LightColorPalette
        AppTheme.DARK -> DarkColorPalette
        AppTheme.AMOLED_DARK -> AmoledDarkColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}

val Colors.outline: Color
    @Composable
    get() = this.onSurface.copy(alpha = ButtonDefaults.OutlinedBorderOpacity)
