/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.input.keyboard.bn

import org.fcitx.fcitx5.android.input.keyboard.KeyAction
import org.fcitx.fcitx5.android.input.keyboard.KeyDef

/** Key that commits a Bengali (or other) string directly to the editor. */
class BengaliCommitKey(
    displayText: String,
    commitText: String = displayText,
    percentWidth: Float = 0.1f,
    variant: KeyDef.Appearance.Variant = KeyDef.Appearance.Variant.Normal,
) : KeyDef(
    KeyDef.Appearance.Text(
        displayText = displayText,
        textSize = 20f,
        percentWidth = percentWidth,
        variant = variant,
    ),
    setOf(
        KeyDef.Behavior.Press(KeyAction.CommitAction(commitText))
    ),
)
