package io.github.ratul.topactivity.manager

import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.ratul.topactivity.App.Companion.API_URL
import io.github.ratul.topactivity.App.Companion.REPO_URL
import io.github.ratul.topactivity.BuildConfig
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.extensions.openLink
import io.github.ratul.topactivity.extensions.showMessage
import org.json.JSONObject

class AppUpdateManager(private val activity: AppCompatActivity) {

    fun checkForUpdate(silent: Boolean = false) {
        try {
            Volley.newRequestQueue(activity).add(buildVersionCheckRequest(silent))
        } catch (_: Exception) {
            handleUpdateError(silent)
        }
    }

    private fun buildVersionCheckRequest(silent: Boolean): JsonObjectRequest {
        return JsonObjectRequest(
            GET, "$API_URL/releases/latest", null,
            { response ->
                try {
                    processUpdateResponse(response, silent)
                } catch (_: Exception) {
                    handleUpdateError(silent)
                }
            },
            { handleUpdateError(silent) }
        ).apply {
            setShouldRetryConnectionErrors(true)
            setShouldCache(false)
        }
    }

    private fun handleUpdateError(silent: Boolean) {
        if (!silent) {
            activity.showMessage(R.string.update_check_failed)
            activity.openLink("$REPO_URL/releases/latest")
        }
    }

    private fun processUpdateResponse(response: JSONObject, silent: Boolean) {
        val tag = response.getString("tag_name")
        val serverVersion = tag.replace(Regex("[^0-9]"), "").toInt()
        val currentVersion = BuildConfig.VERSION_NAME.replace(Regex("[^0-9]"), "").toInt()

        if (serverVersion > currentVersion) {
            MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.update_available)
                .setMessage(activity.getString(R.string.new_version_available, tag))
                .setPositiveButton(R.string.download) { dialog, _ ->
                    activity.openLink("$REPO_URL/releases/tag/$tag")
                    dialog.dismiss()
                }
                .setNeutralButton(R.string.later) { dialog, _ -> dialog.dismiss() }
                .show()
        } else if (!silent) {
            activity.showMessage(R.string.already_using_latest)
        }
    }
}