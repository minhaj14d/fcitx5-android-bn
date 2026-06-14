/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.input.keyboard.bn

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.Keep
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.InputMethodEntry
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.keyboard.BackspaceKey
import org.fcitx.fcitx5.android.input.keyboard.BaseKeyboard
import org.fcitx.fcitx5.android.input.keyboard.CommaKey
import org.fcitx.fcitx5.android.input.keyboard.KeyDef
import org.fcitx.fcitx5.android.input.keyboard.KeyDef.Appearance.Variant
import org.fcitx.fcitx5.android.input.keyboard.LanguageKey
import org.fcitx.fcitx5.android.input.keyboard.LayoutSwitchKey
import org.fcitx.fcitx5.android.input.keyboard.ReturnKey
import org.fcitx.fcitx5.android.input.keyboard.SpaceKey
import org.fcitx.fcitx5.android.plugin.bangla.AvroKeyboardIds
import splitties.views.imageResource

/**
 * Probhat fixed-layout Bengali keyboard (Ekushey / OpenBangla normal layer).
 * Keys commit Bengali characters directly via [KeyAction.CommitAction].
 */
@SuppressLint("ViewConstructor")
class ProbhatKeyboard(
    context: Context,
    theme: Theme,
) : BaseKeyboard(context, theme, Layout) {

    companion object {
        const val Name = AvroKeyboardIds.LAYOUT_PROBHAT

        // Normal-layer mappings from OpenBangla Probhat.json (Unicode Probhat).
        private val ROW1 = listOf("দ", "ঊ", "ঈ", "ড়", "ঠ", "ঐ", "উ", "ই", "ঔ", "ফ")
        private val ROW2 = listOf("অ", "ষ", "ঢ", "থ", "ঘ", "ঃ", "ঝ", "খ", "ং")
        private val ROW3 = listOf("য", "ঢ়", "ছ", "ঋ", "ভ", "ণ", "ঙ")

        val Layout: List<List<KeyDef>> = listOf(
            ROW1.map { BengaliCommitKey(it) },
            ROW2.map { BengaliCommitKey(it) },
            listOf(
                BengaliCommitKey("্", variant = Variant.Alternative),
                *ROW3.map { BengaliCommitKey(it) }.toTypedArray(),
                BackspaceKey(),
            ),
            listOf(
                LayoutSwitchKey("?123", ""),
                CommaKey(0.1f, Variant.Alternative),
                LanguageKey(),
                SpaceKey(),
                BengaliCommitKey("।", percentWidth = 0.1f, variant = Variant.Alternative),
                ReturnKey(),
            ),
        )
    }

    val backspace: org.fcitx.fcitx5.android.input.keyboard.ImageKeyView by lazy { findViewById(R.id.button_backspace) }
    val lang: org.fcitx.fcitx5.android.input.keyboard.ImageKeyView by lazy { findViewById(R.id.button_lang) }
    val space: org.fcitx.fcitx5.android.input.keyboard.TextKeyView by lazy { findViewById(R.id.button_space) }
    val `return`: org.fcitx.fcitx5.android.input.keyboard.ImageKeyView by lazy { findViewById(R.id.button_return) }

    private val showLangSwitchKey = AppPrefs.getInstance().keyboard.showLangSwitchKey

    @Keep
    private val showLangSwitchKeyListener = ManagedPreference.OnChangeListener<Boolean> { _, v ->
        updateLangSwitchKey(v)
    }

    init {
        updateLangSwitchKey(showLangSwitchKey.getValue())
        showLangSwitchKey.registerOnChangeListener(showLangSwitchKeyListener)
    }

    override fun onAttach() = Unit

    override fun onReturnDrawableUpdate(returnDrawable: Int) {
        `return`.img.imageResource = returnDrawable
    }

    override fun onPunctuationUpdate(mapping: Map<String, String>) = Unit

    override fun onInputMethodUpdate(ime: InputMethodEntry) {
        space.mainText.text = buildString {
            append(ime.displayName)
            ime.subMode.run { label.ifEmpty { name.ifEmpty { null } } }?.let { append(" ($it)") }
        }
    }

    private fun updateLangSwitchKey(visible: Boolean) {
        lang.visibility = if (visible) View.VISIBLE else View.GONE
    }
}
