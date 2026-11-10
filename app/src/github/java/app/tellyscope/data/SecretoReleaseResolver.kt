// SPDX-License-Identifier: GPL-3.0-only
package app.tellyscope.data

import app.tellyscope.R
import org.json.JSONObject

private val ARTIFACT_NAME = Regex("[A-Za-z0-9][A-Za-z0-9._+-]{0,199}\\.apk", RegexOption.IGNORE_CASE)
private val NON_RELEASE = Regex("(^|[-_.])(debug|test|tests|androidtest|unsigned|unaligned)([-_.]|$)", RegexOption.IGNORE_CASE)
private val TAG = Regex("[A-Za-z0-9][A-Za-z0-9._+-]{0,99}")
private val DIGEST = Regex("sha256:([0-9a-fA-F]{64})")
private val ABI_NAMES = listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86", "arm64", "armv7", "aarch64")
private fun assetAbi(name: String): String? = ABI_NAMES.firstOrNull { name.lowercase().contains(it) }?.let {
    when (it) { "arm64", "aarch64" -> "arm64-v8a"; "armv7" -> "armeabi-v7a"; else -> it }
}

/** One confirmed immutable asset, not a versioned URL compiled into Manager. Fail closed on ambiguity. */
fun resolveSecretoRelease(json: String, target: LauncherTarget? = null): LauncherRelease {
    fun fail(id: Int): Nothing = throw ReleaseUnavailable(id)
    try {
        val r = JSONObject(json)
        if (r.getBoolean("draft") || r.getBoolean("prerelease")) fail(R.string.secreto_launcher_invalid_release)
        val tag = r.getString("tag_name")
        val releases = "${SecretoLauncher.REPOSITORY}/releases"
        if (!TAG.matches(tag) || r.getString("html_url") != "$releases/tag/$tag") fail(R.string.secreto_launcher_invalid_release)
        val assets = r.getJSONArray("assets")
        val apks = (0 until assets.length()).map(assets::getJSONObject).filter {
            val name = it.getString("name")
            ARTIFACT_NAME.matches(name) && !NON_RELEASE.containsMatchIn(name)
        }
        if (apks.isEmpty()) fail(R.string.secreto_launcher_missing_apk)
        // Prefer a universal artifact. Otherwise the TV's ABI preference order decides; never the phone's ABI.
        val universal = apks.filter { assetAbi(it.getString("name")) == null || it.getString("name").contains("universal", true) }
        val compatible = if (universal.isNotEmpty()) universal else target?.abis?.firstNotNullOfOrNull { abi ->
            apks.filter { assetAbi(it.getString("name")) == abi }.takeIf { it.isNotEmpty() }
        }.orEmpty()
        if (compatible.isEmpty()) fail(R.string.secreto_launcher_incompatible)
        val apk = compatible.singleOrNull() ?: fail(R.string.secreto_launcher_ambiguous_apk)
        val name = apk.getString("name")
        val expected = "$releases/download/$tag/$name"
        val digest = DIGEST.matchEntire(apk.optString("digest"))?.groupValues?.get(1)?.lowercase()
            ?: fail(R.string.secreto_launcher_invalid_release)
        val size = apk.getLong("size")
        if (apk.getString("browser_download_url") != expected || size !in 1..SecretoLauncher.entry.maxBytes)
            fail(R.string.secreto_launcher_invalid_release)
        return LauncherRelease(tag, "$releases/tag/$tag", expected, size, digest)
    } catch (e: ReleaseUnavailable) { throw e }
    catch (_: Exception) { fail(R.string.secreto_launcher_invalid_release) }
}
