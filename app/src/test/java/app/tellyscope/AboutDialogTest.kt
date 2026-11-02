// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import app.tellyscope.ui.SITE
import app.tellyscope.ui.DEMO_VIDEO
import app.tellyscope.ui.AboutDialog
import app.tellyscope.ui.theme.TellyscopeTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xxhdpi")
class AboutDialogTest {
    @get:Rule val compose = createComposeRule()
    private val app get() = RuntimeEnvironment.getApplication()
    private fun text(id: Int) = app.getString(id)
    private fun show(fontScale: Float = 1f, rtl: Boolean = false, dismiss: () -> Unit = {}) {
        RuntimeEnvironment.setFontScale(fontScale)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            ) {
                TellyscopeTheme(dark = false, dynamic = false) {
                    // Robolectric omits native dialog margins. Reserve the usual phone inset
                    // so visibility assertions exercise the smaller real content width.
                    AboutDialog("4f1c2a9e7b3d", dismiss, modifier = Modifier.widthIn(
                        max = (LocalConfiguration.current.screenWidthDp - 56).coerceAtMost(560).dp,
                    ))
                }
            }
        }
    }
    private fun node(id: Int) = compose.onNodeWithText(text(id))
    private fun scrollTo(id: Int) = node(id).performScrollTo().assertIsDisplayed()
    @OptIn(ExperimentalRoborazziApi::class)
    private fun capture(name: String) = captureScreenRoboImage("build/outputs/roborazzi/about_refined_$name.png")
    private fun socialVisible() {
        node(R.string.social_youtube).assertIsDisplayed().assertHasClickAction()
        node(R.string.social_instagram).assertIsDisplayed().assertHasClickAction()
        node(R.string.lists_about_close).assertIsDisplayed()
    }
    private fun horizontal(rtl: Boolean = false) {
        val youtube = node(R.string.social_youtube).fetchSemanticsNode().boundsInRoot
        val instagram = node(R.string.social_instagram).fetchSemanticsNode().boundsInRoot
        assertEquals(youtube.top, instagram.top, 1f)
        assertEquals(rtl, youtube.left > instagram.left)
    }
    private fun stacked() {
        val youtube = node(R.string.social_youtube).fetchSemanticsNode().positionInRoot
        val instagram = node(R.string.social_instagram).fetchSemanticsNode().positionInRoot
        assertTrue("Expected stacked links: YouTube=$youtube Instagram=$instagram", instagram.y > youtube.y)
    }
    @Test fun phone_primaryLinksVisibleAndCreditsCollapsed() {
        var closed = false
        show(dismiss = { closed = true })
        socialVisible()
        // A normal phone may stack at the native dialog width; both links must be visible.
        node(R.string.developer_website).assertIsDisplayed()
        node(R.string.developer_source).assertIsDisplayed()
        node(R.string.developer_attribution).assertDoesNotExist()
        compose.onNodeWithText("Mohamed LALAH | محمد لالاح").assertIsDisplayed()
        capture("phone")
        node(R.string.lists_about_close).performClick()
        assertTrue(closed)
    }
    @Config(qualifiers = "w320dp-h640dp-xhdpi")
    @Test fun narrowPhone_socialStillVisible() {
        show()
        socialVisible()
        stacked()
        capture("narrow")
    }
    @Config(qualifiers = "w800dp-h1280dp-xhdpi")
    @Test fun tablet_socialSideBySide() {
        show()
        socialVisible()
        horizontal()
        capture("tablet")
    }
    @Config(qualifiers = "ar-w411dp-h891dp-xxhdpi")
    @Test fun arabic_linksFollowRtl() {
        show(rtl = true)
        socialVisible()
        horizontal(rtl = true)
        capture("arabic")
    }
    @Config(qualifiers = "w320dp-h640dp-xhdpi")
    @Test fun largeFonts_contentScrollableAndCloseAccessible() {
        show(fontScale = 1.6f)
        node(R.string.lists_about_close).assertIsDisplayed()
        scrollTo(R.string.social_youtube)
        scrollTo(R.string.social_instagram)
        stacked()
        capture("large_font")
        scrollTo(R.string.about_credits).performClick()
        scrollTo(R.string.lists_about_libraries)
        node(R.string.lists_about_close).assertIsDisplayed()
    }
    @Config(qualifiers = "w800dp-h360dp-xhdpi")
    @Test fun landscape_creditsAndCloseRemainReachable() {
        show()
        node(R.string.lists_about_close).assertIsDisplayed()
        scrollTo(R.string.social_youtube)
        scrollTo(R.string.social_instagram)
        capture("landscape")
        scrollTo(R.string.about_credits).performClick()
        scrollTo(R.string.lists_about_libraries)
        node(R.string.lists_about_close).assertIsDisplayed()
    }
    @Test fun fullOriginalLegalContentAccessibleAndCollapsible() {
        show()
        scrollTo(R.string.about_credits).performClick()
        listOf(R.string.developer_original, R.string.developer_attribution,
            R.string.developer_upstream, R.string.lists_about_website, R.string.lists_about_demo,
            R.string.developer_license, R.string.developer_notice, R.string.lists_about_licence,
            R.string.lists_about_libraries).forEach { scrollTo(it) }
        compose.onNodeWithText(app.getString(R.string.lists_about_lists_commit, "4f1c2a9e7b3d")).performScrollTo().assertIsDisplayed()
        capture("credits")
        scrollTo(R.string.about_credits).performClick()
        node(R.string.developer_attribution).assertDoesNotExist()
        node(R.string.lists_about_close).assertIsDisplayed()
    }
    @Test fun projectAndLegalLinksKeepExactDestinations() {
        show()
        val shadow = org.robolectric.Shadows.shadowOf(app)
        fun check(id: Int, url: String) {
            scrollTo(id).performClick()
            assertEquals(url, shadow.nextStartedActivity.dataString)
        }
        check(R.string.developer_website, "https://secretotools.com")
        check(R.string.developer_source, "https://github.com/mohamedlalah/SecretoTVManager")
        scrollTo(R.string.about_credits).performClick()
        check(R.string.developer_upstream, "https://github.com/man15h/tellyscope")
        check(R.string.lists_about_website, SITE)
        check(R.string.lists_about_demo, DEMO_VIDEO)
        check(R.string.developer_license, "https://www.gnu.org/licenses/gpl-3.0.html")
        check(R.string.developer_notice, "https://github.com/man15h/tellyscope/blob/main/NOTICE")
    }
    @Test fun versionAndBuildComeFromInstalledPackage() {
        show()
        @Suppress("DEPRECATION")
        val info = app.packageManager.getPackageInfo(app.packageName, 0)
        val code = androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(info)
        compose.onNodeWithText(app.getString(R.string.developer_version, info.versionName.orEmpty(), code)).assertIsDisplayed()
    }
}
