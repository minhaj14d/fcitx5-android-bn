/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.plugin.bangla

/** Shared identifiers for Bangla Avro / Probhat keyboard integration. */
object AvroKeyboardIds {
    /** Fcitx input method unique name for Avro phonetic typing. */
    const val IME_AVRO = "avro"

    /** Fcitx input method unique name for Probhat fixed layout. */
    const val IME_PROBHAT = "probhat"

    /** Soft keyboard layout id (roman QWERTY feeding the Avro engine). */
    const val LAYOUT_AVRO = "keyboard-bn-avro"

    /** Soft keyboard layout id (Probhat Bengali fixed layout). */
    const val LAYOUT_PROBHAT = "keyboard-bn-probhat"

    fun isBanglaIme(uniqueName: String): Boolean =
        uniqueName == IME_AVRO || uniqueName == IME_PROBHAT

    fun layoutForIme(uniqueName: String): String? = when (uniqueName) {
        IME_AVRO -> LAYOUT_AVRO
        IME_PROBHAT -> LAYOUT_PROBHAT
        else -> null
    }
}
