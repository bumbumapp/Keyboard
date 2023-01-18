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

package dev.patrickgold.florisboard.app.settings.localization

import android.provider.Settings.Global.getString
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.app.florisPreferenceModel
import dev.patrickgold.florisboard.ime.core.DisplayLanguageNamesIn
import dev.patrickgold.florisboard.ime.core.Subtype
import dev.patrickgold.florisboard.ime.core.SubtypeJsonConfig
import dev.patrickgold.florisboard.ime.core.SubtypeLayoutMap
import dev.patrickgold.florisboard.ime.core.SubtypeNlpProviderMap
import dev.patrickgold.florisboard.ime.keyboard.LayoutType
import dev.patrickgold.florisboard.keyboardManager
import dev.patrickgold.florisboard.lib.FlorisLocale
import dev.patrickgold.florisboard.lib.compose.FlorisButtonBar
import dev.patrickgold.florisboard.lib.compose.FlorisScreen
import dev.patrickgold.florisboard.lib.compose.FlorisWarningCard
import dev.patrickgold.florisboard.lib.compose.stringRes
import dev.patrickgold.florisboard.lib.ext.ExtensionComponentName
import dev.patrickgold.florisboard.lib.observeAsNonNullState
import dev.patrickgold.florisboard.subtypeManager
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.ListPreference
import dev.patrickgold.jetpref.datastore.ui.Preference
import dev.patrickgold.jetpref.datastore.ui.PreferenceGroup
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefListItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

private val SelectComponentName = ExtensionComponentName("00", "00")
private val SelectNlpProviderId = SelectComponentName.toString()

private val SelectLayoutMap = SubtypeLayoutMap(
    characters = SelectComponentName,
    symbols = SelectComponentName,
    symbols2 = SelectComponentName,
    numeric = SelectComponentName,
    numericAdvanced = SelectComponentName,
    numericRow = SelectComponentName,
    phone = SelectComponentName,
    phone2 = SelectComponentName,
)
private val SelectLocale = FlorisLocale.from("00", "00")



private class SubtypeEditorStates(init: Subtype?) {
    companion object {
        val Saver = Saver<SubtypeEditorStates, String>(
            save = { editor ->
                val subtype = Subtype(
                    id = editor.id.value,
                    primaryLocale = editor.primaryLocale.value,
                    secondaryLocales = editor.secondaryLocales.value,
                    nlpProviders = editor.nlpProviders.value,
                    composer = editor.composer.value,
                    currencySet = editor.currencySet.value,
                    punctuationRule = editor.punctuationRule.value,
                    popupMapping = editor.popupMapping.value,
                    layoutMap = editor.layoutMap.value,
                )
                SubtypeJsonConfig.encodeToString(subtype)
            },
            restore = { str ->
                val subtype = SubtypeJsonConfig.decodeFromString<Subtype>(str)
                SubtypeEditorStates(subtype)
            },
        )
    }

    val id: MutableState<Long> = mutableStateOf(init?.id ?: -1)
    val primaryLocale: MutableState<FlorisLocale> = mutableStateOf(init?.primaryLocale ?: SelectLocale)
    val secondaryLocales: MutableState<List<FlorisLocale>> = mutableStateOf(init?.secondaryLocales ?: listOf())
    val nlpProviders: MutableState<SubtypeNlpProviderMap> = mutableStateOf(init?.nlpProviders ?: Subtype.DEFAULT.nlpProviders)
    val composer: MutableState<ExtensionComponentName> = mutableStateOf(init?.composer ?: SelectComponentName)
    val currencySet: MutableState<ExtensionComponentName> = mutableStateOf(init?.currencySet ?: SelectComponentName)
    val punctuationRule: MutableState<ExtensionComponentName> = mutableStateOf(init?.punctuationRule ?: Subtype.DEFAULT.punctuationRule)
    val popupMapping: MutableState<ExtensionComponentName> = mutableStateOf(init?.popupMapping ?: SelectComponentName)
    val layoutMap: MutableState<SubtypeLayoutMap> = mutableStateOf(init?.layoutMap ?: SelectLayoutMap)

    fun applySubtype(subtype: Subtype) {
        id.value = subtype.id
        primaryLocale.value = subtype.primaryLocale
        secondaryLocales.value = subtype.secondaryLocales
        composer.value = subtype.composer
        currencySet.value = subtype.currencySet
        punctuationRule.value = subtype.punctuationRule
        popupMapping.value = subtype.popupMapping
        layoutMap.value = subtype.layoutMap
    }

    fun toSubtype() = runCatching<Subtype> {
        check(primaryLocale.value != SelectLocale)
        check(nlpProviders.value.spelling != SelectNlpProviderId)
        check(nlpProviders.value.suggestion != SelectNlpProviderId)
        check(composer.value != SelectComponentName)
        check(currencySet.value != SelectComponentName)
        check(punctuationRule.value != SelectComponentName)
        check(popupMapping.value != SelectComponentName)
        check(layoutMap.value.characters != SelectComponentName)
        check(layoutMap.value.symbols != SelectComponentName)
        check(layoutMap.value.symbols2 != SelectComponentName)
        check(layoutMap.value.numeric != SelectComponentName)
        check(layoutMap.value.numericAdvanced != SelectComponentName)
        check(layoutMap.value.numericRow != SelectComponentName)
        check(layoutMap.value.phone != SelectComponentName)
        check(layoutMap.value.phone2 != SelectComponentName)
        Subtype(
            id.value, primaryLocale.value, secondaryLocales.value, nlpProviders.value, composer.value,
            currencySet.value, punctuationRule.value, popupMapping.value, layoutMap.value,
        )
    }
}


@Composable
fun LocalizationScreen(id:Long?) = FlorisScreen {
    title = stringRes(R.string.settings__localization__title)
    previewFieldVisible = true
    iconSpaceReserved = false
    val prefs by florisPreferenceModel()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val subtypeManager by context.subtypeManager()
    var saveSubtype by rememberSaveable { mutableStateOf<Subtype?>(null) }
    val subtypePresets by keyboardManager.resources.subtypePresets.observeAsNonNullState()
    var searchTermValue by remember { mutableStateOf(TextFieldValue()) }
    val subtypeEditor = rememberSaveable(saver = SubtypeEditorStates.Saver) {
        val subtype = id?.let { subtypeManager.getSubtypeById(id) }
        SubtypeEditorStates(subtype)
    }
    val currencySets by keyboardManager.resources.currencySets.observeAsNonNullState()
    val layouts by keyboardManager.resources.layouts.observeAsNonNullState()
    val displayLanguageNamesIn by prefs.localization.displayLanguageNamesIn.observeAsState()
    var showSubtypePresetsDialog by rememberSaveable { mutableStateOf(false) }
    var showSelectAsError by rememberSaveable { mutableStateOf(false) }
    var errorDialogStrId by rememberSaveable { mutableStateOf<Int?>(null) }
    floatingActionButton {
        ExtendedFloatingActionButton(
            icon = { Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringRes(R.string.settings__localization__subtype_add_title),
            ) },
            text = { Text(
                text = stringRes(R.string.settings__localization__subtype_add_title),
            ) },
            onClick = { showSubtypePresetsDialog = true },
        )
    }


    content {

        ListPreference(
            prefs.localization.displayLanguageNamesIn,
            title = stringRes(R.string.settings__localization__display_language_names_in__label),
            entries = DisplayLanguageNamesIn.listEntries(),
        )
        PreferenceGroup(title = stringRes(R.string.settings__localization__group_subtypes__label)) {
            val subtypes by subtypeManager.subtypesFlow.collectAsState()
            if (subtypes.isEmpty()) {
                FlorisWarningCard(
                    modifier = Modifier.padding(all = 8.dp),
                    text = stringRes(R.string.settings__localization__subtype_no_subtypes_configured_warning),
                )
            } else {

                for (subtype in subtypes) {
                    val cMeta = layouts[LayoutType.CHARACTERS]?.get(subtype.layoutMap.characters)
                    val sMeta = layouts[LayoutType.SYMBOLS]?.get(subtype.layoutMap.symbols)
                    val currMeta = currencySets[subtype.currencySet]
                    val summary = stringRes(
                        id = R.string.settings__localization__subtype_summary,
                        "characters_name" to (cMeta?.label ?: "null"),
                        "symbols_name" to (sMeta?.label ?: "null"),
                        "currency_set_name" to (currMeta?.label ?: "null"),
                    )
                    Preference(
                        title = when (displayLanguageNamesIn) {
                            DisplayLanguageNamesIn.SYSTEM_LOCALE -> subtype.primaryLocale.displayName()
                            DisplayLanguageNamesIn.NATIVE_LOCALE -> subtype.primaryLocale.displayName(subtype.primaryLocale)
                        },
                        summary = summary,
                        onClick = { navController.navigate(
                            Routes.Settings.SubtypeEdit(subtype.id)
                        ) },
                    )
                }
            }
        }

        if (showSubtypePresetsDialog) {
            JetPrefAlertDialog(
                title = stringRes(R.string.settings__localization__subtype_presets),
                dismissLabel = stringRes(android.R.string.cancel),
                scrollModifier = Modifier,
                contentPadding = PaddingValues(horizontal = 8.dp),
                onDismiss = {
                    showSubtypePresetsDialog = false
                },
            ) {
               Column{

                   TextField(
                       modifier = Modifier.fillMaxWidth(),
                       value = searchTermValue,
                       onValueChange = { searchTermValue = it },
                       placeholder = { Text(stringRes(R.string.settings__localization__subtype_search_locale_placeholder)) },
                       leadingIcon = {
                           Icon(
                               painter = painterResource(R.drawable.ic_search),
                               contentDescription = null,
                           )
                       },
                       singleLine = true,
                       shape = RectangleShape,
                       colors = TextFieldDefaults.textFieldColors(
                           focusedIndicatorColor = Color.Transparent,
                           unfocusedIndicatorColor = Color.Transparent,
                           disabledIndicatorColor = Color.Transparent,
                       ),
                   )
                   val subtype = remember(displayLanguageNamesIn) {
                       subtypePresets.sortedBy { subtype ->
                           when (displayLanguageNamesIn) {
                               DisplayLanguageNamesIn.SYSTEM_LOCALE -> subtype.locale.displayName()
                               DisplayLanguageNamesIn.NATIVE_LOCALE -> subtype.locale.displayName(subtype.locale)
                           }.lowercase()
                       }
                   }

                   val filteredSystemLocales = remember(searchTermValue) {
                       if (searchTermValue.text.isBlank()) {
                           subtype
                       } else {
                           val term = searchTermValue.text.trim().lowercase()
                           subtype.filter { subtyp ->
                               subtyp.locale.displayName().lowercase().contains(term) ||
                                   subtyp.locale.displayName(subtyp.locale).lowercase().contains(term) ||
                                   subtyp.locale.languageTag().lowercase().startsWith(term) ||
                                   subtyp.locale.localeTag().lowercase().startsWith(term)
                           }
                       }
                   }
                   if (filteredSystemLocales.isEmpty()) {
                       Text(
                           modifier = Modifier
                               .padding(16.dp)
                               .align(Alignment.CenterHorizontally),
                           text = stringRes(
                               R.string.settings__localization__subtype_search_locale_not_found,
                               "search_term" to searchTermValue.text,
                           ),
                           color = LocalContentColor.current.copy(alpha = 0.54f),
                       )
                   }

                   LazyColumn {

                       items(filteredSystemLocales) { subtypePreset->
                           JetPrefListItem(

                               modifier = Modifier.clickable {
                                   subtypeEditor.applySubtype(subtypePreset.toSubtype())
                                   saveSubtype = subtypePreset.toSubtype()
                                   showSubtypePresetsDialog = false

                               },
                               text = when (displayLanguageNamesIn) {
                                   DisplayLanguageNamesIn.SYSTEM_LOCALE -> subtypePreset.locale.displayName()
                                   DisplayLanguageNamesIn.NATIVE_LOCALE -> subtypePreset.locale.displayName(
                                       subtypePreset.locale
                                   )
                               },
                               secondaryText = subtypePreset.preferred.characters.componentId,
                           )
                       }
                   }
               }


            }
        }
        saveSubtype?.let {subtype->
            JetPrefAlertDialog(
                title = stringRes(R.string.action__save),
                confirmLabel = stringRes(android.R.string.ok),
                dismissLabel =stringRes(android.R.string.cancel) ,
                onConfirm = {
                    subtypeEditor.toSubtype().onSuccess { subtype ->
                        if (id == null) {
                            if (!subtypeManager.addSubtype(subtype)) {
                                errorDialogStrId = R.string.settings__localization__subtype_error_already_exists
                            }
                        } else {
                            subtypeManager.modifySubtypeWithSameId(subtype)
                        }
                        saveSubtype=null
                    }.onFailure {
                        showSelectAsError = true
                        errorDialogStrId = R.string.settings__localization__subtype_error_fields_no_value
                    }
                },
                onDismiss = {
                    saveSubtype = null
                },
            ) {
                Text(text =stringRes(R.string.do_you_want))
            }
        }
        errorDialogStrId?.let { strId ->
            JetPrefAlertDialog(
                title = stringRes(R.string.error__title),
                confirmLabel = stringRes(android.R.string.ok),
                onConfirm = {
                    errorDialogStrId = null
                },
                onDismiss = {
                    errorDialogStrId = null
                },
            ) {
                Text(text = stringRes(strId))
            }
        }
        //PreferenceGroup(title = stringRes(R.string.settings__localization__group_layouts__label)) {
        //}
    }

}

