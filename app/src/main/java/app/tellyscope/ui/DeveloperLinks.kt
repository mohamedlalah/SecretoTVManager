// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import app.tellyscope.R

/** User-initiated HTTPS links only; Android retains control of link-handler preferences. */
internal fun openDeveloperLink(context: Context, url: String, youtube: Boolean = false) {
    if (!launchDeveloperLink(context, url, youtube)) linkUnavailable(context)
}

/** Instagram uses its HTTPS profile URL, so the same URL also works in a browser. */
internal fun openInstagramProfile(context: Context, url: String) {
    if (!launchDeveloperLink(context, url, youtube = false, instagram = true)) linkUnavailable(context)
}

private fun launchDeveloperLink(context: Context, url: String, youtube: Boolean, instagram: Boolean = false): Boolean {
    val uri = Uri.parse(url)
    if (uri.scheme != "https" || uri.host.isNullOrEmpty()) return false
    val view = Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)
    fun launch(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
    if (youtube && uri.host in setOf("www.youtube.com", "youtube.com") &&
        launch(Intent(view).setPackage("com.google.android.youtube"))) return true
    if (instagram && uri.host in setOf("www.instagram.com", "instagram.com") &&
        launch(Intent(view).setPackage("com.instagram.android"))) return true
    return launch(view)
}

private fun linkUnavailable(context: Context) {
    Toast.makeText(context, R.string.developer_link_unavailable, Toast.LENGTH_LONG).show()
}
