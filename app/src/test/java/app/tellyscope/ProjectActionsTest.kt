// SPDX-License-Identifier: GPL-3.0-only
package app.tellyscope

import app.tellyscope.adb.AppFacts
import app.tellyscope.adb.Reports
import app.tellyscope.data.Changelog
import app.tellyscope.data.PROJECT_REPO
import app.tellyscope.data.UPSTREAM_REPO
import app.tellyscope.ui.OWN_LISTS_GUIDE
import app.tellyscope.ui.REPO
import app.tellyscope.ui.SHARE_TEXT
import app.tellyscope.data.resolve
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProjectActionsTest {
    @Test fun shareAndProblemReportsReferToFork() {
        val share = SHARE_TEXT.resolve(RuntimeEnvironment.getApplication())
        assertTrue(share.endsWith(PROJECT_REPO))
        assertFalse(share.contains(UPSTREAM_REPO))
        assertEquals(PROJECT_REPO.removePrefix("https://github.com/"), Reports.OURS)
        val report = Reports.problem(AppFacts("1.0.0", "Phone"), null,
            Reports.Connection.COULD_NOT_CONNECT, Reports.Outcome.COULD_NOT_CONNECT, "Details", null)
        assertTrue(report.link.startsWith("$PROJECT_REPO/issues/new?"))
        assertFalse(report.link.contains("man15h"))
    }

    @Test fun appChangelogLinksAreForkLinksAndUpstreamCreditsRemainSeparate() {
        assertEquals("$PROJECT_REPO/pull/123", Changelog.pullUrl(123))
        assertEquals(UPSTREAM_REPO, REPO)
        assertTrue(OWN_LISTS_GUIDE.startsWith(UPSTREAM_REPO))
    }

    @Test fun packagedLegalMaterialsPreserveOriginalNoticesAndDependencyTexts() {
        val assets = RuntimeEnvironment.getApplication().assets
        val license = assets.open("legal/LICENSE").bufferedReader().use { it.readText() }
        val notice = assets.open("legal/NOTICE").bufferedReader().use { it.readText() }
        assertTrue(license.contains("GNU GENERAL PUBLIC LICENSE"))
        assertTrue(notice.contains("Copyright (C) 2026 Tellyscope contributors"))
        assertTrue(notice.contains("The MIT License text"))
        assertTrue(assets.open("legal/licenses/Apache-2.0.txt").bufferedReader().use { it.readText() }.contains("END OF TERMS AND CONDITIONS"))
        val files = assets.list("legal/dependency-notices").orEmpty()
        for (module in listOf("bcprov", "bcpkix", "bcutil")) {
            val directory = files.map { it.substringBefore('/') }.distinct().single { it.startsWith(module + "-") }
            assertTrue(assets.open("legal/dependency-notices/$directory/META-INF_LICENSE.md").bufferedReader().use { it.readText() }.contains("Copyright"))
        }
    }

    @Test fun bundledForkChangelogDoesNotAdvertiseOriginalReleases() {
        val text = RuntimeEnvironment.getApplication().assets.open(Changelog.ASSET).bufferedReader().use { it.readText() }
        assertTrue(text.startsWith("# Secreto TV Manager changelog"))
        val releases = Changelog.parse(text)
        assertEquals(listOf("1.1.0", "1.0.0"), releases.map { it.version })
        assertEquals("in development", releases.first().date)
        assertEquals("2026-11-02", releases.last().date)
        assertFalse(text.contains("## 0.14.0"))
    }
}
