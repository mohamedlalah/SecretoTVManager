// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope

import androidx.compose.ui.text.font.FontWeight
import app.tellyscope.data.RELEASES
import app.tellyscope.data.UpdateCheck
import app.tellyscope.data.compareVersions
import app.tellyscope.data.parseLatestRelease
import app.tellyscope.data.UpdateStatus
import app.tellyscope.data.RELEASES_API
import app.tellyscope.data.PROJECT_REPO
import app.tellyscope.data.UPSTREAM_REPO
import java.io.FileNotFoundException
import java.util.Properties
import app.tellyscope.ui.boldSpans
import app.tellyscope.ui.dialogNotes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/** org.json is Android's, so this runs under Robolectric. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UpdateCheckTest {
    @get:Rule val tmp = TemporaryFolder()

    private val MORE = Strings.string(R.string.update_more_notes)

    private val minute = 60 * 1000L
    private val hour = 60 * minute
    private val day = 24 * hour

    private fun release(tag: String = "v1.1.0", notes: String = "Official notes") =
        """{"tag_name":"$tag","html_url":"$RELEASES/tag/$tag","draft":false,"prerelease":false,"body":"$notes"}"""

    @Test fun officialRepositoryAndApiAreIndependentFromUpstream() {
        assertEquals("https://github.com/mohamedlalah/SecretoTVManager", PROJECT_REPO)
        assertEquals("$PROJECT_REPO/releases", RELEASES)
        assertEquals("https://api.github.com/repos/mohamedlalah/SecretoTVManager/releases/latest", RELEASES_API)
        assertEquals("https://github.com/man15h/tellyscope", UPSTREAM_REPO)
    }

    @Test fun versionsCompareNumericallyAndAlphasSortBeforeStable() {
        assertTrue(compareVersions("1.10.0", "1.9.99") > 0)
        assertTrue(compareVersions("1.0.0-alpha.2", "1.0.0-alpha.10") < 0)
        assertTrue(compareVersions("1.0.0-alpha.10", "1.0.0") < 0)
        assertTrue(compareVersions("1.0.0", "1.0.1-alpha.1") < 0)
        assertEquals(0, compareVersions("1.0.0", "1.0.0"))
        assertTrue(compareVersions("invalid", "1.0.0") < 0)
        assertTrue(compareVersions("1.0.0-alpha.999999999999999999", "1.0.0") < 0)
    }

    @Test fun latestObjectParsesNotesAndExactOfficialTagPage() {
        val u = parseLatestRelease(release())!!
        assertEquals("1.1.0", u.version)
        assertEquals("Official notes", u.notes)
        assertEquals("$RELEASES/tag/v1.1.0", u.url)
        assertEquals("$RELEASES/tag/1.2.0", parseLatestRelease(release("1.2.0"))?.url)
    }

    @Test fun arraysDraftsPrereleasesOddTagsAndForeignPagesAreRejected() {
        assertNull(parseLatestRelease("[${release()}]"))
        assertNull(parseLatestRelease("not json"))
        assertNull(parseLatestRelease("{}"))
        assertNull(parseLatestRelease(release().replace("\"draft\":false", "\"draft\":true")))
        assertNull(parseLatestRelease(release().replace("\"prerelease\":false", "\"prerelease\":true")))
        for (tag in listOf("nightly", "v1.2.0-beta.1", "v1.2.0-alpha.1", "v1.2.0/../escape")) {
            assertNull(parseLatestRelease(release(tag)))
        }
        assertNull(parseLatestRelease(release().replace(PROJECT_REPO, UPSTREAM_REPO)))
        assertNull(parseLatestRelease(release().replace(PROJECT_REPO, "https://github.com/mohamedlalah/SecretoTVManager-evil")))
    }

    @Test fun arbitraryApkAssetsAreNeverOffered() {
        val withAssets = release().dropLast(1) + """, "assets":[{"name":"app.apk","browser_download_url":"https://evil.example/app.apk","size":1234}]}"""
        val u = parseLatestRelease(withAssets)!!
        assertEquals("$RELEASES/tag/v1.1.0", u.url)
        assertTrue(u.javaClass.declaredFields.none { it.name == "apk" })
    }

    @Test fun onlyLatestOfficialEndpointIsEverRequested() = runTest {
        val urls = mutableListOf<String>()
        val check = UpdateCheck(tmp.newFile(), "1.0.0", false) { urls += it; release().toByteArray() }
        assertEquals("1.1.0", check.check()?.version)
        assertEquals(UpdateStatus.Available, check.status)
        assertEquals(listOf(RELEASES_API), urls)
    }

    @Test fun http404IsNoPublishedUpdatesWithNoFallback() = runTest {
        val urls = mutableListOf<String>()
        val check = UpdateCheck(tmp.newFile(), "1.0.0", false) { urls += it; throw FileNotFoundException("404") }
        assertNull(check.check())
        assertEquals(UpdateStatus.NoPublishedUpdates, check.status)
        assertEquals(listOf(RELEASES_API), urls)
        assertEquals(emptyList<Any>(), check.newer())
    }

    @Test fun noReleaseIsThrottledAndRestoredAcrossRestart() = runTest {
        val file = tmp.newFile()
        var now = 1_000_000L
        var calls = 0
        val fetch: suspend (String) -> ByteArray = { calls++; throw FileNotFoundException("404") }
        val check = UpdateCheck(file, "1.0.0", false, { now }, fetch)
        check.check()
        now += 29 * minute
        check.check()
        val restarted = UpdateCheck(file, "1.0.0", false, { now }, fetch)
        assertEquals(UpdateStatus.NoPublishedUpdates, restarted.status)
        restarted.check()
        assertEquals(1, calls)
        now += minute
        restarted.check()
        assertEquals(2, calls)
    }

    @Test fun stableIntervalAndFiveMinuteAlphaIntervalArePreserved() = runTest {
        for ((alpha, interval) in listOf(false to 30 * minute, true to 5 * minute)) {
            var now = 1_000_000L
            var calls = 0
            val check = UpdateCheck(tmp.newFile(), "1.0.0-alpha.1", alpha, { now }) { calls++; release().toByteArray() }
            assertEquals("1.1.0", check.check()?.version)
            now += interval - 1
            assertNull(check.check())
            assertEquals(1, calls)
            now++
            assertEquals("1.1.0", check.check()?.version)
            assertEquals(2, calls)
        }
    }

    @Test fun equalAndOlderVersionsDoNotNotify() = runTest {
        for (tag in listOf("v1.0.0", "v0.9.0")) {
            val check = UpdateCheck(tmp.newFile(), "1.0.0", false) { release(tag).toByteArray() }
            assertNull(check.check())
            assertEquals(UpdateStatus.UpToDate, check.status)
        }
    }

    @Test fun failedRequestsAreDistinctFromNoPublishedReleaseAndNeverUseUpstream() = runTest {
        var fail = true
        val urls = mutableListOf<String>()
        val check = UpdateCheck(tmp.newFile(), "1.0.0", false) {
            urls += it
            if (fail) throw IOException("offline") else release().toByteArray()
        }
        assertNull(check.check())
        assertEquals(UpdateStatus.Failed, check.status)
        fail = false
        assertEquals("1.1.0", check.check()?.version)
        assertTrue(urls.all { it == RELEASES_API })
    }

    @Test fun malformedPayloadIsNotMisreportedAsNoRelease() = runTest {
        val check = UpdateCheck(tmp.newFile(), "1.0.0", false) { "{}".toByteArray() }
        assertNull(check.check())
        assertEquals(UpdateStatus.Failed, check.status)
    }

    @Test fun switchPersistsAndOffMakesNoRequests() = runTest {
        val file = tmp.newFile()
        var calls = 0
        val check = UpdateCheck(file, "1.0.0", false) { calls++; release().toByteArray() }
        assertTrue(check.enabled)
        check.enabled = false
        assertNull(check.check())
        assertEquals(UpdateStatus.Disabled, check.status)
        assertFalse(UpdateCheck(file, "1.0.0", false).enabled)
        assertEquals(0, calls)
    }

    @Test fun oldUpstreamTimestampDoesNotSuppressForkCheckButOffIsPreserved() = runTest {
        val file = tmp.newFile()
        Properties().apply { setProperty("checked", "1000000") }.also { p -> file.outputStream().use { p.store(it, null) } }
        var calls = 0
        val check = UpdateCheck(file, "1.0.0", false, { 1_000_001L }) { calls++; release().toByteArray() }
        assertEquals("1.1.0", check.check()?.version)
        assertEquals(1, calls)
        check.enabled = false
        assertFalse(UpdateCheck(file, "1.0.0", false).enabled)
    }

    @Test fun whatsNewUsesOnlyAlreadyFetchedForkReleaseNotes() = runTest {
        val urls = mutableListOf<String>()
        val check = UpdateCheck(tmp.newFile(), "1.0.0", false) { urls += it; release(notes = "- **Fixed:** Fork update source.").toByteArray() }
        check.check()
        assertEquals(listOf("1.1.0"), check.newer()?.map { it.version })
        assertEquals("Fork update source.", check.newer()?.single()?.changes?.single()?.text)
        assertEquals(listOf(RELEASES_API), urls)
    }

    /** v0.1.0's body as release.yml wrote it, commit list cut short. */
    private val prodBody = """

        - **New:** A new app icon: a telescope with a TV screen for a lens. [#28](../../pull/28)
        - **New:** Install apps on the TV from an APK, split APKs or a bundle. [#9](../../pull/9)
        - **New:** Send files to the TV's Download or Movies folder, and save them back. [#9](../../pull/9)
        - **New:** Suggested apps to disable, from a tested list for your TV. [#11](../../pull/11)
        - **Fixed:** Processes, screen state, connections and data used now show on real TVs. [#17](../../pull/17)
        - **Fixed:** A TV on Wi-Fi no longer shows its connection as Ethernet. [#17](../../pull/17)
        - **New:** An icon shows whether each device is a TV, projector, Fire TV or stick. [#18](../../pull/18)
        - **Improved:** The confirm before disabling suggested apps names every app. [#19](../../pull/19)
        - **Improved:** System updaters, Wi-Fi resources and Chrome's library are now protected. [#19](../../pull/19)
        - **Fixed:** Done and error messages stay on screen long enough to read. [#20](../../pull/20)
        - **Fixed:** Undo and results always go to the TV the action ran on. [#20](../../pull/20)
        - **New:** Debloat in two steps: pick your TV, then review every app it turns off. [#23](../../pull/23)
        - **Improved:** The TV page leads with the TV, its vitals and big action buttons. [#23](../../pull/23)
        - **Fixed:** Adding a TV with a port above 65535 shows a message, not a crash. [#25](../../pull/25)
        - **Fixed:** Losing Wi-Fi while pairing shows a message, not a crash. [#25](../../pull/25)
        - **Fixed:** A broken suggestions list can no longer crash the app. [#25](../../pull/25)
        - **Improved:** Undoing a Turn on in History asks before disabling the app. [#25](../../pull/25)

        ### Commits since the first commit

        - Changelog: 0.1.0 — 2026-10-23 (c9a1fe7)
        - README: the icon and a real heading, in place of the logo header (ef66937)
    """.trimIndent()

    /** v0.2.0-alpha.1's body as release.yml wrote it, commit list cut short. */
    private val alphaBody = """
        Alpha build v0.2.0-alpha.1 from `abdb1a8` on dev. Untested and may break; it installs as a separate app, `app.tellyscope.alpha`.

        ### Commits since v0.1.0-alpha.3

        - CHANGELOG: home screen switch (abdb1a8)
        - Start 0.2.0 (4afecde)
    """.trimIndent()

    @Test fun `prod notes keep the changelog lines, plain, capped at twelve`() {
        val lines = dialogNotes(prodBody, MORE).lines()
        assertEquals(13, lines.size)
        assertEquals("- **New:** A new app icon: a telescope with a TV screen for a lens.", lines.first())
        assertEquals("…and more on GitHub", lines.last())
        assertTrue(lines.none { "](" in it || "Commits" in it })
    }

    @Test fun `alpha notes are just the alpha line`() {
        assertEquals(
            "Alpha build v0.2.0-alpha.1 from abdb1a8 on dev. Untested and may break; it installs as a separate app, app.tellyscope.alpha.",
            dialogNotes(alphaBody, MORE),
        )
    }

    @Test fun `short notes are not cut`() {
        assertEquals("- **Fixed:** one thing.", dialogNotes("- **Fixed:** one thing. [#9](../../pull/9)\n\n### Commits since v1\n\n- x (abc)", MORE))
    }

    @Test fun `New is bold with the stars gone`() {
        val first = boldSpans(dialogNotes(prodBody, MORE)).let { it.subSequence(0, it.text.indexOf('\n')) }
        assertEquals("- New: A new app icon: a telescope with a TV screen for a lens.", first.text)
        assertFalse('*' in boldSpans(dialogNotes(prodBody, MORE)).text)
        val bold = first.spanStyles.single()
        assertEquals(FontWeight.Bold, bold.item.fontWeight)
        assertEquals("New:", first.text.substring(bold.start, bold.end))
    }

    @Test fun `an unmatched pair of stars is dropped`() {
        val s = boldSpans("a **b** c ** d")
        assertEquals("a b c  d", s.text)
        assertEquals("b", s.spanStyles.single().let { s.text.substring(it.start, it.end) })
    }
}
