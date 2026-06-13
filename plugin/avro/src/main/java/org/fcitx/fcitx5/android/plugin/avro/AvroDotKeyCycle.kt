/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.plugin.avro

/** The "." key cycles Bengali danda (।), ASCII period, and ellipsis on quick taps. */
class AvroDotKeyCycleState {
    private var lastTapAt: Long = 0L
    private var cycleIndex: Int = 0

    fun reset() {
        lastTapAt = 0L
        cycleIndex = 0
    }

    fun onDotTap(nowMonotonicMs: Long): AvroDotKeyTapResult {
        val continueCycle = nowMonotonicMs - lastTapAt <= DANDA_DOUBLE_TAP_MS
        cycleIndex = if (continueCycle) {
            (cycleIndex % 3) + 1
        } else {
            1
        }
        val commitText = when (cycleIndex) {
            1 -> "।"
            2 -> "."
            else -> "..."
        }
        val sendBackspaceBeforeCommit = continueCycle
        if (cycleIndex == 3) {
            cycleIndex = 0
        }
        lastTapAt = nowMonotonicMs
        return AvroDotKeyTapResult(sendBackspaceBeforeCommit, commitText)
    }

    companion object {
        const val DANDA_DOUBLE_TAP_MS = 350L
    }
}

data class AvroDotKeyTapResult(
    val sendBackspaceBeforeCommit: Boolean,
    val commitText: String,
)
