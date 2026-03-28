/*
 *   Copyright (C) 2022 Ratul Hasan
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.ratul.topactivity.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.repository.DataRepository

class SettingsFragment : PreferenceFragmentCompat() {

    lateinit var serviceMode: ListPreference
    lateinit var useAccessibility: SwitchPreferenceCompat
    lateinit var scanSpeed: ListPreference
    lateinit var historySize: ListPreference
    lateinit var windowSize: ListPreference
    lateinit var enableAutostart: Preference
    lateinit var autoUpdate: SwitchPreferenceCompat
    lateinit var useSystemFont: SwitchPreferenceCompat
    lateinit var checkUpdate: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        serviceMode = findPreference("service_mode")!!
        useAccessibility = findPreference("use_accessibility")!!
        scanSpeed = findPreference("scan_speed")!!
        historySize = findPreference("history_size")!!
        windowSize = findPreference("window_size")!!
        enableAutostart = findPreference("enable_autostart")!!
        autoUpdate = findPreference("auto_update")!!
        useSystemFont = findPreference("system_font")!!
        checkUpdate = findPreference("check_update")!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.isVerticalScrollBarEnabled = false
        listView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

    override fun onStart() {
        super.onStart()

        val attachedActivity = activity ?: return
        val changeListener = RestartOnChange(attachedActivity)

        useSystemFont.onPreferenceChangeListener = changeListener
    }

    class RestartOnChange(private val activity: FragmentActivity) :
        Preference.OnPreferenceChangeListener {

        override fun onPreferenceChange(
            preference: Preference,
            newValue: Any?
        ): Boolean {
            Snackbar.make(
                activity.window.decorView.rootView,
                R.string.restart_required,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.restart) {
                DataRepository.updateStatus(false)
                activity.finishAffinity()
            }.show()
            return true
        }
    }
}
