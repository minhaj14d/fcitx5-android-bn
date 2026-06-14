/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.ui.main.settings.im

import org.fcitx.fcitx5.android.core.FcitxAPI
import org.fcitx.fcitx5.android.core.RawConfig
import org.fcitx.fcitx5.android.ui.main.settings.FcitxPreferenceFragment
import org.fcitx.fcitx5.android.ui.main.settings.SettingsRoute
import org.fcitx.fcitx5.android.utils.addPreference
import org.fcitx.fcitx5.android.utils.lazyRoute
import org.fcitx.fcitx5.android.utils.navigateWithAnim
import androidx.navigation.fragment.findNavController
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.plugin.bangla.AvroKeyboardIds

class InputMethodConfigFragment : FcitxPreferenceFragment() {
    val args by lazyRoute<SettingsRoute.InputMethodConfig>()

    private var avroDictLinkAdded = false

    override fun getPageTitle(): String = args.name

    override suspend fun obtainConfig(fcitx: FcitxAPI): RawConfig {
        return fcitx.getImConfig(args.uniqueName)
    }

    override suspend fun saveConfig(fcitx: FcitxAPI, newConfig: RawConfig) {
        fcitx.setImConfig(args.uniqueName, newConfig)
    }

    override fun onStart() {
        super.onStart()
        if (args.uniqueName == AvroKeyboardIds.IME_AVRO && !avroDictLinkAdded) {
            avroDictLinkAdded = true
            preferenceScreen.addPreference(R.string.avro_dict) {
                findNavController().navigateWithAnim(SettingsRoute.AvroDict)
            }
        }
    }
}