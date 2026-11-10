// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties

private const val RELEASE_INTERVAL_MS = 30 * 60 * 1000L
private const val ALPHA_INTERVAL_MS = 5 * 60 * 1000L

/** Metadata only. The application never downloads an APK or offers installation. */

/** Only the official fork's latest published stable release; no alternate repository or endpoint. */
class UpdateCheck(
    private val file: File,
    private val current: String,
    override val alpha: Boolean = false,
    private val clock: () -> Long = System::currentTimeMillis,
    private val fetch: suspend (String) -> ByteArray = ::fetchOfficialRelease,
) : Updates {
    @Volatile override var status: UpdateStatus = props().let { p ->
        if (p.getProperty("source") == RELEASES_API && p.getProperty("status") == UpdateStatus.NoPublishedUpdates.name)
            UpdateStatus.NoPublishedUpdates else UpdateStatus.NotChecked
    }
        private set
    private var latestOfficial: Update? = null

    @get:Synchronized @set:Synchronized
    override var enabled: Boolean
        get() = props().getProperty("off") != "true"
        set(on) {
            save { if (on) remove("off") else setProperty("off", "true") }
            status = if (!on) UpdateStatus.Disabled else props().let { p ->
                if (p.getProperty("source") == RELEASES_API && p.getProperty("status") == UpdateStatus.NoPublishedUpdates.name)
                    UpdateStatus.NoPublishedUpdates else UpdateStatus.NotChecked
            }
        }

    /** Null means no new offer. [status] distinguishes no published release from network errors. */
    override suspend fun check(): Update? = withContext(Dispatchers.IO) {
        val p = props()
        if (p.getProperty("off") == "true") { status = UpdateStatus.Disabled; return@withContext null }
        // The old upstream timestamp must not suppress the first check against the fork.
        val last = p.getProperty("checked")?.toLongOrNull()?.takeIf { p.getProperty("source") == RELEASES_API }
        val now = clock()
        val interval = if (alpha) ALPHA_INTERVAL_MS else RELEASE_INTERVAL_MS
        if (last != null && now - last in 0 until interval) return@withContext null
        try {
            val text = fetch(RELEASES_API).decodeToString()
            val release = parseLatestRelease(text) ?: throw IllegalArgumentException("Invalid latest-release response")
            latestOfficial = release
            val newer = compareVersions(release.version, current) > 0
            status = if (newer) UpdateStatus.Available else UpdateStatus.UpToDate
            checked(now)
            release.takeIf { newer }
        } catch (e: CancellationException) {
            throw e
        } catch (_: FileNotFoundException) {
            latestOfficial = null
            status = UpdateStatus.NoPublishedUpdates
            checked(now)
            null
        } catch (_: Exception) {
            status = UpdateStatus.Failed
            null
        }
    }

    /** What's new uses the already-fetched official release notes, never upstream/main history. */
    override suspend fun newer(): List<Changelog.Release>? = withContext(Dispatchers.IO) {
        if (status == UpdateStatus.Failed) return@withContext null
        val release = latestOfficial?.takeIf { compareVersions(it.version, current) > 0 } ?: return@withContext emptyList()
        val changes = Changelog.parse("# Changelog\n\n## ${release.version}\n\n${release.notes}").firstOrNull()?.changes.orEmpty()
        listOf(Changelog.Release(release.version, null, changes.ifEmpty {
            if (release.notes.isBlank()) emptyList() else listOf(Changelog.Change(null, release.notes, null))
        }))
    }

    private fun checked(now: Long) = save {
        setProperty("source", RELEASES_API)
        setProperty("checked", now.toString())
        setProperty("status", status.name)
    }

    private fun props() = Properties().apply {
        runCatching { file.takeIf { it.exists() }?.inputStream()?.use(::load) }
    }

    @Synchronized
    private fun save(change: Properties.() -> Unit) {
        val p = props().apply(change)
        runCatching { file.parentFile?.mkdirs(); file.outputStream().use { p.store(it, null) } }
    }
}

/** GitHub /latest is a single object, not the releases-list array. Assets are intentionally ignored. */
fun parseLatestRelease(json: String): Update? = runCatching {
    val r = JSONObject(json)
    if (!r.has("draft") || !r.has("prerelease") || r.getBoolean("draft") || r.getBoolean("prerelease")) return null
    val tag = r.getString("tag_name")
    val version = tag.removePrefix("v")
    if (parseVersion(version)?.alpha != null || parseVersion(version) == null) return null
    val page = "$RELEASES/tag/$tag"
    // Never trust an API payload pointing to an upstream, unrelated or lookalike repository.
    if (r.has("html_url") && r.getString("html_url") != page) return null
    Update(version, r.optString("body").takeUnless { it == "null" }.orEmpty().trim(), page)
}.getOrNull()

/** Fixed HTTPS endpoint, bounded metadata and no cross-repository redirects or APK downloads. */
private suspend fun fetchOfficialRelease(url: String): ByteArray = withContext(Dispatchers.IO) {
    require(url == RELEASES_API)
    val c = URL(url).openConnection() as HttpURLConnection
    c.instanceFollowRedirects = false
    c.connectTimeout = 10_000
    c.readTimeout = 10_000
    c.setRequestProperty("Accept", "application/vnd.github+json")
    c.setRequestProperty("User-Agent", "SecretoTVManager")
    val started = System.nanoTime()
    try {
        if (c.responseCode == 404) throw FileNotFoundException("No official published release")
        check(c.responseCode == 200) { "HTTP ${c.responseCode}" }
        c.inputStream.use { input ->
            val out = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                check(out.size() + n <= 1_048_576) { "Release metadata too large" }
                check(System.nanoTime() - started <= 30_000_000_000L) { "Release metadata too slow" }
                out.write(buffer, 0, n)
            }
            out.toByteArray()
        }
    } finally { c.disconnect() }
}

private class Version(val core: List<Int>, val alpha: Int?)

/** Numeric major.minor.patch, optionally with the existing alpha suffix. */
private fun parseVersion(v: String): Version? {
    val m = Regex("""(\d+)\.(\d+)\.(\d+)(?:-alpha\.(\d+))?""").matchEntire(v) ?: return null
    val n = m.groupValues.drop(1).map { it.toIntOrNull() }
    if (n.take(3).any { it == null } || (m.groups[4] != null && n[3] == null)) return null
    return Version(n.take(3).map { it!! }, m.groups[4]?.let { it.value.toIntOrNull() ?: return null })
}

fun compareVersions(a: String, b: String): Int {
    val x = parseVersion(a)
    val y = parseVersion(b)
    if (x == null || y == null) return compareValues(x != null, y != null)
    for (i in 0..2) x.core[i].compareTo(y.core[i]).takeIf { it != 0 }?.let { return it }
    return when {
        x.alpha == y.alpha -> 0
        x.alpha == null -> 1
        y.alpha == null -> -1
        else -> x.alpha.compareTo(y.alpha)
    }
}
