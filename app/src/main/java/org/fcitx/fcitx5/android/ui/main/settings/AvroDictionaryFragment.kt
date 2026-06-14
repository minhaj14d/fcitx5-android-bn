/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.ui.main.settings

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.avro.AvroDictManager
import org.fcitx.fcitx5.android.ui.common.PaddingPreferenceFragment
import org.fcitx.fcitx5.android.ui.main.MainViewModel
import org.fcitx.fcitx5.android.utils.addPreference
import org.fcitx.fcitx5.android.utils.importErrorDialog

/** Import a plain-text Avro user dictionary (`roman<TAB>বাংলা` per line). */
class AvroDictionaryFragment : PaddingPreferenceFragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                        AvroDictManager.importFromInputStream(stream).getOrThrow()
                    } ?: error("Cannot read file")
                }
            }.onSuccess { count ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.avro_dict_imported, count),
                    Toast.LENGTH_SHORT
                ).show()
                refreshImportSummary()
            }.onFailure {
                requireContext().importErrorDialog(it)
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.createPreferenceScreen(requireContext()).also {
            preferenceScreen = it
            refreshImportSummary(it)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.setToolbarTitle(getString(R.string.avro_dict))
    }

    private fun refreshImportSummary(screen: androidx.preference.PreferenceScreen = preferenceScreen) {
        screen.removeAll()
        screen.addPreference(
            title = getString(R.string.avro_dict_import),
            summary = getString(R.string.avro_dict_import_summary, AvroDictManager.entryCount()),
            onClick = { launcher.launch("text/*") },
        )
    }
}
