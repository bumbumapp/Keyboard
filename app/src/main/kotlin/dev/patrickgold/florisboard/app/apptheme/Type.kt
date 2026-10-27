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

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import dev.patrickgold.florisboard.R

/** Manrope across the settings app (2026 premium refresh). */
val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)

val Typography = Typography(
    defaultFontFamily = Manrope,
    h4 = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.5).sp),
    h5 = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 24.sp, letterSpacing = (-0.3).sp),
    h6 = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    subtitle1 = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    body1 = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    body2 = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    button = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    caption = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 12.sp),
)
