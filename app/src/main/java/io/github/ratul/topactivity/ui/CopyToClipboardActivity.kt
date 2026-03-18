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

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.ratul.topactivity.App
import io.github.ratul.topactivity.R

class CopyToClipboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        copyToClipboard(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        copyToClipboard(intent)
    }

    private fun copyToClipboard(intent: Intent) {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!text.isNullOrEmpty()) {
            val clipData = ClipData.newPlainText(getString(R.string.app_name), text)
            App.instance.clipboardManager.setPrimaryClip(clipData)
        }
    }
}
