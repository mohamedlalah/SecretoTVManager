// SPDX-License-Identifier: GPL-3.0-only
package app.tellyscope

import app.tellyscope.data.*
import app.tellyscope.ui.*
import org.json.JSONObject
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SecretoLauncherTest {
    private val hash = "a".repeat(64)
    private val base = "${SecretoLauncher.REPOSITORY}/releases"
    private fun asset(name: String, tag: String = "v99.4.8") = JSONObject().put("name", name).put("size", 123456)
        .put("browser_download_url", "$base/download/$tag/$name").put("digest", "sha256:$hash")
    private fun release(vararg names: String) = JSONObject().put("tag_name", "v99.4.8").put("html_url", "$base/tag/v99.4.8")
        .put("draft", false).put("prerelease", false).put("assets", JSONArray(names.map { asset(it) }))
    private fun rejects(json: JSONObject, expected: Int, target: LauncherTarget? = null) {
        try { resolveSecretoRelease(json.toString(), target); fail("Accepted invalid release") }
        catch (e: ReleaseUnavailable) { assertEquals(expected, e.messageRes) }
    }
    @Test fun futureVersionsAndArbitraryReleaseFilenamesWork() {
        val resolved = resolveSecretoRelease(release("new-launcher-name-v99.apk").toString())
        assertEquals("v99.4.8", resolved.version)
        assertEquals(123456, resolved.size.toInt())
        assertEquals(hash, resolved.sha256)
        assertEquals("$base/download/v99.4.8/new-launcher-name-v99.apk", resolved.apk)
    }
    @Test fun draftsAndPrereleasesNeverResolve() {
        for (key in listOf("draft", "prerelease")) rejects(release("app.apk").put(key, true), R.string.secreto_launcher_invalid_release)
        rejects(release("app.apk").apply { remove("draft") }, R.string.secreto_launcher_invalid_release)
    }
    @Test fun debugTestAndUnsignedArtifactsAreExcluded() {
        assertTrue(resolveSecretoRelease(release("app-debug.apk", "app-test.apk", "app-unsigned.apk", "official.apk").toString()).apk.endsWith("/official.apk"))
        rejects(release("app-debug.apk"), R.string.secreto_launcher_missing_apk)
        rejects(release(), R.string.secreto_launcher_missing_apk)
    }
    @Test fun ambiguityDoesNotPickTheFirstAsset() = rejects(release("one.apk", "two.apk"), R.string.secreto_launcher_ambiguous_apk)
    @Test fun universalWinsOtherwiseTheTvsAbiOrderWins() {
        val target = LauncherTarget(30, listOf("arm64-v8a", "armeabi-v7a"))
        assertTrue(resolveSecretoRelease(release("app-arm64-v8a.apk", "app-universal.apk").toString(), target).apk.endsWith("/app-universal.apk"))
        assertTrue(resolveSecretoRelease(release("app-armeabi-v7a.apk", "app-arm64-v8a.apk").toString(), target).apk.endsWith("/app-arm64-v8a.apk"))
        rejects(release("app-x86.apk"), R.string.secreto_launcher_incompatible, target)
    }
    @Test fun missingChecksumAndForeignLinksFailClosed() {
        val r = release("app.apk"); r.getJSONArray("assets").getJSONObject(0).remove("digest")
        rejects(r, R.string.secreto_launcher_invalid_release)
        val other = release("app.apk"); other.getJSONArray("assets").getJSONObject(0).put("browser_download_url", "https://example.com/app.apk")
        rejects(other, R.string.secreto_launcher_invalid_release)
        rejects(release("app.apk").put("html_url", "https://github.com/man15h/tellyscope/releases/tag/v99.4.8"), R.string.secreto_launcher_invalid_release)
    }
    @Test fun invalidSizesAndMalformedMetadataFailClosed() {
        for (size in listOf(0L, SecretoLauncher.entry.maxBytes + 1)) {
            val r = release("app.apk");r.getJSONArray("assets").getJSONObject(0).put("size", size)
            rejects(r, R.string.secreto_launcher_invalid_release)
        }
        rejects(JSONObject(), R.string.secreto_launcher_invalid_release)
    }
    @Test fun trustedEntryIsFirstAndCannotBeReplacedOrDuplicated() {
        val hostile = SecretoLauncher.entry.copy(signers = setOf("b".repeat(64)), accessibility = "1.0")
        val other = KnownLauncher("com.spocky.projengmenu", "Projectivy", "About")
        val merged = SecretoLauncher.merge(listOf(other, hostile, other))
        assertEquals(listOf(SecretoLauncher.entry, other), merged)
        assertNull(merged.first().accessibility)
        assertFalse(launcherPlayAvailable(SecretoLauncher.PACKAGE))
        assertTrue(launcherPlayAvailable(other.pkg))
        assertTrue(SecretoLauncher.entry.accepts(SecretoLauncher.PACKAGE, setOf(SecretoLauncher.SIGNER)))
        assertFalse(SecretoLauncher.entry.accepts(SecretoLauncher.PACKAGE, setOf("b".repeat(64))))
    }
    private fun installed(home: Boolean = false) = AppInfo(SecretoLauncher.PACKAGE, "Secreto TV Launcher", null, false, true, null, "1.3.1", home = home, answersHome = true, listedLauncher = true)
    @Test fun installedAndCurrentStatesRemainAccurateWithoutDuplicateRecommendations() {
        for (home in listOf(false, true)) {
            val a = installed(home); val apps = listOf(a, a)
            assertEquals(a, installedLauncher(apps, a.packageName))
            assertEquals(listOf(a), installedChoices(apps))
            assertFalse(recommendedAbsent(apps).any { it.pkg == a.packageName })
            assertEquals(home, installedLauncher(apps, a.packageName)?.home)
        }
        assertEquals(SecretoLauncher.PACKAGE, recommendedAbsent(emptyList()).first().pkg)
        assertNotNull(installedLauncher(listOf(installed().copy(enabled = false)), SecretoLauncher.PACKAGE))
        assertEquals("", LauncherFlow().pkg)
    }
    @Test fun bundledImagesPassTheSameHashAndPictureValidation() {
        for (file in listOf(SecretoLauncher.icon, SecretoLauncher.screenshot)) assertNotNull(SecretoLauncher.image(file))
        assertNull(SecretoLauncher.image(SecretoLauncher.icon.copy(sha256 = "b".repeat(64))))
    }
    @Test fun apkRequirementsUseTheTvsSdkAndCpu() {
        assertFalse(LauncherRequirements(26, emptySet()).compatible(LauncherTarget(25, emptyList())))
        assertTrue(LauncherRequirements(26, emptySet()).compatible(LauncherTarget(30, listOf("x86"))))
        assertFalse(LauncherRequirements(26, emptySet(), setOf("android.software.leanback")).compatible(LauncherTarget(30, emptyList())))
        assertTrue(LauncherRequirements(26, emptySet(), setOf("android.software.leanback")).compatible(LauncherTarget(30, emptyList(), setOf("android.software.leanback"))))
        assertFalse(LauncherRequirements(26, emptySet(), glEs = 196608).compatible(LauncherTarget(30, emptyList(), glEs = 131072)))
        assertFalse(LauncherRequirements(26, setOf("arm64-v8a")).compatible(LauncherTarget(30, listOf("armeabi-v7a"))))
    }
}
