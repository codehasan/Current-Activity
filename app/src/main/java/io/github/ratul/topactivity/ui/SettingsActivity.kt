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

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.ratul.topactivity.App.Companion.REPO_URL
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.utils.DatabaseUtil

class SettingsActivity : AppCompatActivity() {

    lateinit var serviceMode: ListPreference
    lateinit var useAccessibility: SwitchPreferenceCompat
    lateinit var scanSpeed: ListPreference
    lateinit var historySize: ListPreference
    lateinit var windowSize: EditTextPreference
    lateinit var enableAutostart: Preference
    lateinit var autoUpdate: SwitchPreferenceCompat
    lateinit var useSystemFont: SwitchPreferenceCompat
    lateinit var checkUpdate: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<FloatingActionButton>(R.id.start).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, REPO_URL.toUri()))
        }

        val fragment =
            supportFragmentManager.findFragmentById(R.id.preferences_container) as SettingsFragment
        bindPreferences(fragment)
    }

    private fun bindPreferences(fragment: SettingsFragment) {
        serviceMode = fragment.serviceMode
        useAccessibility = fragment.useAccessibility
        scanSpeed = fragment.scanSpeed
        historySize = fragment.historySize
        windowSize = fragment.windowSize
        enableAutostart = fragment.enableAutostart
        autoUpdate = fragment.autoUpdate
        useSystemFont = fragment.useSystemFont
        checkUpdate = fragment.checkUpdate
    }

    private fun applyTheme() {
        setTheme(
            if (DatabaseUtil.useSystemFont) R.style.AppTheme_SystemFont
            else R.style.AppTheme
        )
    }
}
