/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.input.keyboard.bn

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.allViews
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.FcitxKeyMapping
import org.fcitx.fcitx5.android.core.InputMethodEntry
import org.fcitx.fcitx5.android.core.KeyState
import org.fcitx.fcitx5.android.core.KeyStates
import org.fcitx.fcitx5.android.core.KeySym
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.keyboard.AlphabetKey
import org.fcitx.fcitx5.android.input.keyboard.AltTextKeyView
import org.fcitx.fcitx5.android.input.keyboard.BackspaceKey
import org.fcitx.fcitx5.android.input.keyboard.BaseKeyboard
import org.fcitx.fcitx5.android.input.keyboard.CapsKey
import org.fcitx.fcitx5.android.input.keyboard.CommaKey
import org.fcitx.fcitx5.android.input.keyboard.KeyAction
import org.fcitx.fcitx5.android.input.keyboard.KeyDef
import org.fcitx.fcitx5.android.input.keyboard.KeyDef.Appearance.Variant
import org.fcitx.fcitx5.android.input.keyboard.LanguageKey
import org.fcitx.fcitx5.android.input.keyboard.LayoutSwitchKey
import org.fcitx.fcitx5.android.input.keyboard.ReturnKey
import org.fcitx.fcitx5.android.input.keyboard.SpaceKey
import org.fcitx.fcitx5.android.input.keyboard.SymbolKey
import org.fcitx.fcitx5.android.input.popup.PopupAction
import org.fcitx.fcitx5.android.plugin.avro.AvroDotKeyCycleState
import org.fcitx.fcitx5.android.plugin.avro.AvroKeyboardIds
import splitties.views.imageResource

/**
 * Roman QWERTY layout for Bangla Avro phonetic input.
 * Keys feed the native Avro engine via [KeyAction.FcitxKeyAction].
 */
@SuppressLint("ViewConstructor")
class BanglaAvroKeyboard(
    context: Context,
    theme: Theme,
) : BaseKeyboard(context, theme, Layout) {

    enum class CapsState { None, Once, Lock }

    companion object {
        const val Name = AvroKeyboardIds.LAYOUT_AVRO

        private val BANGLA_DIGITS = mapOf(
            '0' to "০", '1' to "১", '2' to "২", '3' to "৩", '4' to "৪",
            '5' to "৫", '6' to "৬", '7' to "৭", '8' to "৮", '9' to "৯",
        )

        private val AVRO_PUNCTUATION = mapOf(
            "0" to "০", "1" to "১", "2" to "২", "3" to "৩", "4" to "৪",
            "5" to "৫", "6" to "৬", "7" to "৭", "8" to "৮", "9" to "৯",
            "." to "।",
        )

        val Layout: List<List<KeyDef>> = listOf(
            listOf(
                AlphabetKey("Q", "1"),
                AlphabetKey("W", "2"),
                AlphabetKey("E", "3"),
                AlphabetKey("R", "4"),
                AlphabetKey("T", "5"),
                AlphabetKey("Y", "6"),
                AlphabetKey("U", "7"),
                AlphabetKey("I", "8"),
                AlphabetKey("O", "9"),
                AlphabetKey("P", "0"),
            ),
            listOf(
                AlphabetKey("A", "@"),
                AlphabetKey("S", "#"),
                AlphabetKey("D", "&"),
                AlphabetKey("F", "*"),
                AlphabetKey("G", "-"),
                AlphabetKey("H", "+"),
                AlphabetKey("J", "="),
                AlphabetKey("K", "("),
                AlphabetKey("L", ")"),
            ),
            listOf(
                CapsKey(),
                AlphabetKey("Z", "_"),
                AlphabetKey("X", "৳"),
                AlphabetKey("C", "\""),
                AlphabetKey("V", "'"),
                AlphabetKey("B", ":"),
                AlphabetKey("N", ";"),
                AlphabetKey("M", "/"),
                BackspaceKey(),
            ),
            listOf(
                LayoutSwitchKey("?123", ""),
                CommaKey(0.1f, Variant.Alternative),
                LanguageKey(),
                SpaceKey(),
                SymbolKey(".", 0.1f, Variant.Alternative),
                ReturnKey(),
            ),
        )
    }

    val caps: org.fcitx.fcitx5.android.input.keyboard.ImageKeyView by lazy { findViewById(R.id.button_caps) }
    val backspace: org.fcitx.fcitx5.android.input.keyboard.ImageKeyView by lazy { findViewById(R.id.button_backspace) }
    val lang: org.fcitx.fcitx5.android.input.keyboard.ImageKeyView by lazy { findViewById(R.id.button_lang) }
    val space: org.fcitx.fcitx5.android.input.keyboard.TextKeyView by lazy { findViewById(R.id.button_space) }
    val `return`: org.fcitx.fcitx5.android.input.keyboard.ImageKeyView by lazy { findViewById(R.id.button_return) }

    private val showLangSwitchKey = AppPrefs.getInstance().keyboard.showLangSwitchKey

    @Keep
    private val showLangSwitchKeyListener = ManagedPreference.OnChangeListener<Boolean> { _, v ->
        updateLangSwitchKey(v)
    }

    private val keepLettersUppercase by AppPrefs.getInstance().keyboard.keepLettersUppercase

    init {
        updateLangSwitchKey(showLangSwitchKey.getValue())
        showLangSwitchKey.registerOnChangeListener(showLangSwitchKeyListener)
    }

    private val textKeys: List<org.fcitx.fcitx5.android.input.keyboard.TextKeyView> by lazy {
        allViews.filterIsInstance<org.fcitx.fcitx5.android.input.keyboard.TextKeyView>().toList()
    }

    private var capsState: CapsState = CapsState.None
    private val avroDotKeyCycle = AvroDotKeyCycleState()

    private fun transformAlphabet(c: String): String = when (capsState) {
        CapsState.None -> c.lowercase()
        else -> c.uppercase()
    }

    private var punctuationMapping: Map<String, String> = mapOf()
    private fun transformPunctuation(p: String): String {
        AVRO_PUNCTUATION[p]?.let { return it }
        return punctuationMapping.getOrDefault(p, p)
    }

    override fun onAction(action: KeyAction, source: org.fcitx.fcitx5.android.input.keyboard.KeyActionListener.Source) {
        var transformed = action
        when (action) {
            is KeyAction.FcitxKeyAction -> when (source) {
                org.fcitx.fcitx5.android.input.keyboard.KeyActionListener.Source.Keyboard -> {
                    val digit = action.act.singleOrNull()?.takeIf(Char::isDigit)
                    if (digit != null) {
                        super.onAction(
                            KeyAction.CommitAction(BANGLA_DIGITS[digit] ?: action.act),
                            source
                        )
                        return
                    }
                    if (action.act == ".") {
                        val tap = avroDotKeyCycle.onDotTap(SystemClock.uptimeMillis())
                        if (tap.sendBackspaceBeforeCommit) {
                            super.onAction(
                                KeyAction.SymAction(KeySym(FcitxKeyMapping.FcitxKey_BackSpace)),
                                source
                            )
                        }
                        super.onAction(KeyAction.CommitAction(tap.commitText), source)
                        return
                    }
                    when (capsState) {
                        CapsState.None -> transformed = action.copy(act = action.act.lowercase())
                        CapsState.Once -> {
                            transformed = action.copy(
                                act = action.act.uppercase(),
                                states = KeyStates(KeyState.Virtual, KeyState.Shift),
                            )
                            switchCapsState()
                        }
                        CapsState.Lock -> transformed = action.copy(
                            act = action.act.uppercase(),
                            states = KeyStates(KeyState.Virtual, KeyState.CapsLock),
                        )
                    }
                }
                org.fcitx.fcitx5.android.input.keyboard.KeyActionListener.Source.Popup -> {
                    if (capsState == CapsState.Once) switchCapsState()
                }
            }
            is KeyAction.CapsAction -> switchCapsState(action.lock)
            else -> {}
        }
        super.onAction(transformed, source)
    }

    override fun onAttach() {
        capsState = CapsState.None
        avroDotKeyCycle.reset()
        updateCapsButtonIcon()
        updateAlphabetKeys()
    }

    override fun onReturnDrawableUpdate(returnDrawable: Int) {
        `return`.img.imageResource = returnDrawable
    }

    override fun onPunctuationUpdate(mapping: Map<String, String>) {
        punctuationMapping = mapping
        updatePunctuationKeys()
    }

    override fun onInputMethodUpdate(ime: InputMethodEntry) {
        space.mainText.text = buildString {
            append(ime.displayName)
            ime.subMode.run { label.ifEmpty { name.ifEmpty { null } } }?.let { append(" ($it)") }
        }
        if (capsState != CapsState.None) switchCapsState()
        avroDotKeyCycle.reset()
    }

    override fun onPopupAction(action: PopupAction) {
        val newAction = when (action) {
            is PopupAction.PreviewAction -> action.copy(content = transformPopupPreview(action.content))
            is PopupAction.PreviewUpdateAction -> action.copy(content = transformPopupPreview(action.content))
            is PopupAction.ShowKeyboardAction -> {
                when (action.keyboard) {
                    is KeyDef.Popup.Keyboard.Preset -> {
                        val label = action.keyboard.label
                        if (label.length == 1 && label[0].isLetter()) {
                            action.copy(
                                keyboard = action.keyboard.copy(label = transformAlphabet(label))
                            )
                        } else action
                    }
                    is KeyDef.Popup.Keyboard.Explicit -> action
                }
            }
            else -> action
        }
        super.onPopupAction(newAction)
    }

    private fun transformPopupPreview(c: String): String {
        if (c.length != 1) return c
        if (c[0].isLetter()) return transformAlphabet(c)
        return transformPunctuation(c)
    }

    private fun switchCapsState(lock: Boolean = false) {
        capsState = if (lock) {
            when (capsState) {
                CapsState.Lock -> CapsState.None
                else -> CapsState.Lock
            }
        } else {
            when (capsState) {
                CapsState.None -> CapsState.Once
                else -> CapsState.None
            }
        }
        updateCapsButtonIcon()
        updateAlphabetKeys()
    }

    private fun updateCapsButtonIcon() {
        caps.img.imageResource = when (capsState) {
            CapsState.None -> R.drawable.ic_capslock_none
            CapsState.Once -> R.drawable.ic_capslock_once
            CapsState.Lock -> R.drawable.ic_capslock_lock
        }
    }

    private fun updateLangSwitchKey(visible: Boolean) {
        lang.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun updateAlphabetKeys() {
        textKeys.forEach {
            if (it.def !is KeyDef.Appearance.AltText) return
            it.mainText.text = it.def.displayText.let { str ->
                if (str.length != 1 || !str[0].isLetter()) return@forEach
                if (keepLettersUppercase) str.uppercase() else transformAlphabet(str)
            }
        }
    }

    private fun updatePunctuationKeys() {
        textKeys.forEach {
            if (it is AltTextKeyView) {
                it.def as KeyDef.Appearance.AltText
                it.altText.text = transformPunctuation(it.def.altText)
            } else {
                it.def as KeyDef.Appearance.Text
                it.mainText.text = it.def.displayText.let { str ->
                    if (str.isEmpty() || str[0].isLetter() || str[0].isWhitespace()) return@forEach
                    transformPunctuation(str)
                }
            }
        }
    }
}
