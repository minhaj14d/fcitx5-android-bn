/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.data.avro

import org.fcitx.fcitx5.android.utils.appContext
import java.io.File
import java.io.InputStream

/**
 * User Bangla dictionary for Avro phonetic input.
 *
 * Entries are stored at `data/avro/user.dict.txt` under the main app external storage
 * (fcitx [FCITX_DATA_HOME]/avro/user.dict.txt). Each line is `roman<TAB>বাংলা` or
 * `roman=বাংলা`. Lines starting with `#` are ignored.
 */
object AvroDictManager {

    private val dictDir = File(
        appContext.getExternalFilesDir(null)!!, "data/avro"
    ).also { it.mkdirs() }

    val userDictFile: File get() = File(dictDir, "user.dict.txt")

    fun entryCount(): Int = if (userDictFile.isFile) {
        userDictFile.readLines().count { line ->
            line.isNotBlank() && !line.startsWith("#") && parseLine(line) != null
        }
    } else {
        0
    }

    fun importFromInputStream(stream: InputStream): Result<Int> = runCatching {
        val entries = stream.bufferedReader().readLines().mapNotNull(::parseLine)
        require(entries.isNotEmpty()) { "Dictionary file contains no valid entries" }
        userDictFile.writeText(buildString {
            appendLine("# Avro user dictionary")
            entries.forEach { (roman, bangla) ->
                appendLine("$roman\t$bangla")
            }
        })
        entries.size
    }

    fun importFromText(text: String): Result<Int> =
        importFromInputStream(text.byteInputStream())

    private fun parseLine(line: String): Pair<String, String>? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null
        }
        val tab = trimmed.indexOf('\t')
        val eq = trimmed.indexOf('=')
        val sep = when {
            tab >= 0 -> tab
            eq >= 0 -> eq
            else -> return null
        }
        val roman = trimmed.substring(0, sep).trim()
        val bangla = trimmed.substring(sep + 1).trim()
        if (roman.isEmpty() || bangla.isEmpty()) {
            return null
        }
        return roman to bangla
    }
}
