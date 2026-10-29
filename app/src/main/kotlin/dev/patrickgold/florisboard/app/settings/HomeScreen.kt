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

package dev.patrickgold.florisboard.app.settings

import android.content.Context
import dev.patrickgold.florisboard.ads.AdsConsent
import dev.patrickgold.florisboard.ads.InterstitialHelper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.app.settings.localization.findActivity
import dev.patrickgold.florisboard.lib.compose.FlorisErrorCard
import dev.patrickgold.florisboard.lib.compose.FlorisScreen
import dev.patrickgold.florisboard.lib.compose.FlorisWarningCard
import dev.patrickgold.florisboard.lib.compose.stringRes
import dev.patrickgold.florisboard.lib.util.InputMethodUtils
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.Preference



/** Settings navigation is a natural break point: show the preloaded interstitial (60 s cap), then navigate. */
fun navigatee(context: Context, theme: String, navController: NavController) {
    InterstitialHelper.showIfReady(context.findActivity()) { navController.navigate(theme) }
}
@Composable
fun HomeScreen() = FlorisScreen {
    title = stringRes(R.string.settings__home__title)
    navigationIconVisible = false
    previewFieldVisible = true

    val navController = LocalNavController.current
    val context = LocalContext.current

    content {
        val isFlorisBoardEnabled by InputMethodUtils.observeIsFlorisboardEnabled(foregroundOnly = true)
        val isFlorisBoardSelected by InputMethodUtils.observeIsFlorisboardSelected(foregroundOnly = true)
        if (!isFlorisBoardEnabled) {
            FlorisErrorCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                showIcon = false,
                text = stringRes(R.string.settings__home__ime_not_enabled),
                onClick = { InputMethodUtils.showImeEnablerActivity(context) },
            )
        } else if (!isFlorisBoardSelected) {
            FlorisWarningCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                showIcon = false,
                text = stringRes(R.string.settings__home__ime_not_selected),
                onClick = { InputMethodUtils.showImePicker(context) },
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BentoHero(
                title = stringRes(R.string.settings__theme__title),
                subtitle = stringRes(R.string.bento__home_hero_subtitle),
                onClick = { navigatee(context, Routes.Settings.Theme, navController) },
            )
            BentoRow(
                BentoTileSpec(R.drawable.ic_language, R.string.settings__localization__title, BentoTone.Blue) {
                    navController.navigate(Routes.Settings.Localization)
                },
                BentoTileSpec(R.drawable.ic_keyboard, R.string.settings__keyboard__title, BentoTone.Violet) {
                    navigatee(context, Routes.Settings.Keyboard, navController)
                },
            )
            BentoRow(
                BentoTileSpec(R.drawable.ic_smartbar, R.string.settings__smartbar__title, BentoTone.Green) {
                    navigatee(context, Routes.Settings.Smartbar, navController)
                },
                BentoTileSpec(R.drawable.ic_spellcheck, R.string.settings__typing__title, BentoTone.Orange) {
                    navController.navigate(Routes.Settings.Typing)
                },
            )
            BentoRow(
                BentoTileSpec(R.drawable.ic_sentiment_satisfied, R.string.settings__media__title, BentoTone.Pink) {
                    navigatee(context, Routes.Settings.Media, navController)
                },
                BentoTileSpec(R.drawable.ic_gesture, R.string.settings__gestures__title, BentoTone.Teal) {
                    navigatee(context, Routes.Settings.Gestures, navController)
                },
            )
            Spacer(Modifier.height(4.dp))
            BentoGroup {
                BentoListRow(R.drawable.ic_library_books, stringRes(R.string.settings__dictionary__title)) {
                    navController.navigate(Routes.Settings.Dictionary)
                }
                BentoListRow(R.drawable.ic_assignment, stringRes(R.string.settings__clipboard__title)) {
                    navController.navigate(Routes.Settings.Clipboard)
                }
                BentoListRow(R.drawable.ic_build, stringRes(R.string.settings__advanced__title)) {
                    navController.navigate(Routes.Settings.Advanced)
                }
                // UMP: users in regions that require it must be able to change their ad consent.
                if (AdsConsent.isPrivacyOptionsRequired(context)) {
                    BentoListRow(R.drawable.ic_policy, stringRes(R.string.ads__privacy_options)) {
                        context.findActivity()?.let { AdsConsent.showPrivacyOptions(it) }
                    }
                }
            }
        }
    }
}

/** Pastel icon colours for the tool tiles: (background, foreground), light and dark. */
enum class BentoTone(val light: Pair<Long, Long>, val dark: Pair<Long, Long>) {
    Blue(0xFFE8F1FD to 0xFF1C5FC7, 0xFF16263D to 0xFF7FB2FF),
    Violet(0xFFF1ECFE to 0xFF5B3CC4, 0xFF241C3D to 0xFFB9A4FF),
    Green(0xFFEAF7F1 to 0xFF0E7A57, 0xFF12302A to 0xFF5FD3A6),
    Orange(0xFFFFF1E8 to 0xFFC2551A, 0xFF35221A to 0xFFFFAA7A),
    Pink(0xFFFDEBF3 to 0xFFB4236E, 0xFF35182A to 0xFFFF8AC2),
    Teal(0xFFE6F6F8 to 0xFF0B7285, 0xFF102D33 to 0xFF62CFE0),
}

class BentoTileSpec(val icon: Int, val title: Int, val tone: BentoTone, val onClick: () -> Unit)

@Composable
private fun BentoHero(title: String, subtitle: String, onClick: () -> Unit) {
    val accent = MaterialTheme.colors.primary
    val onAccent = MaterialTheme.colors.onPrimary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(accent)
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Three key caps, like a mini keyboard row.
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                listOf(1f, 0.75f, 0.5f).forEach { alpha ->
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color.White.copy(alpha = alpha)),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(onAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_palette), contentDescription = null, tint = onAccent)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(title, color = onAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = onAccent.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BentoRow(a: BentoTileSpec, b: BentoTileSpec) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.height(IntrinsicSize.Min),
    ) {
        BentoTile(a, Modifier.weight(1f).fillMaxHeight())
        BentoTile(b, Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun BentoTile(spec: BentoTileSpec, modifier: Modifier) {
    val dark = !MaterialTheme.colors.isLight
    val (bg, fg) = if (dark) spec.tone.dark else spec.tone.light
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colors.surface)
            .clickable(onClick = spec.onClick)
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(bg)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(spec.icon), contentDescription = null, tint = Color(fg), modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(
            stringRes(spec.title),
            color = MaterialTheme.colors.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun BentoGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colors.surface)
            .padding(vertical = 4.dp),
    ) { content() }
}

@Composable
private fun BentoListRow(icon: Int, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.size(16.dp))
        Text(title, color = MaterialTheme.colors.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
