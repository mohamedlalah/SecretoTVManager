// SPDX-License-Identifier: GPL-3.0-only
package app.tellyscope

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import app.tellyscope.data.*
import app.tellyscope.ui.*
import app.tellyscope.ui.theme.TellyscopeTheme
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
class SecretoLauncherUiTest {
    @get:Rule val compose = createComposeRule()
    private val device = Device("tv", ScanHit("192.0.2.1", 5555, ScanKind.Classic, "Test TV", "Test"), DeviceState.Online, "Test TV", null, "11", 0)
    private val installed = AppInfo(SecretoLauncher.PACKAGE, "Secreto TV Launcher", null, false, true, null, "1.3.1", homeInstead = "com.test.home", answersHome = true, listedLauncher = true)
    private fun text(id: Int) = RuntimeEnvironment.getApplication().getString(id)
    @OptIn(ExperimentalRoborazziApi::class)
    private fun show(name: String, apps: List<AppInfo> = emptyList(), flow: LauncherFlow = LauncherFlow(pkg = SecretoLauncher.PACKAGE)) {
        compose.mainClock.autoAdvance = false
        compose.setContent { TellyscopeTheme { LauncherScreen(device, flow, apps, LauncherActions(), direct = true) } }
        compose.mainClock.advanceTimeBy(1500)
        if (flow.pkg == SecretoLauncher.PACKAGE && flow.fire == null) {
            val description = RuntimeEnvironment.getApplication().getString(R.string.copy_home_screen_of, "Secreto TV Launcher")
            compose.waitUntil(timeoutMillis = 8000) {
                compose.mainClock.advanceTimeByFrame()
                compose.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty()
            }
            compose.mainClock.advanceTimeBy(1000)
        }
        compose.waitForIdle()
        captureScreenRoboImage("build/outputs/roborazzi/secreto_launcher_$name.png")
    }
    @Test fun absentPlayDisabledAndDownloadAvailable() {
        show("absent_en")
        compose.onNodeWithText(text(R.string.copy_get_from_store)).assertIsNotEnabled()
        compose.onNodeWithText(text(R.string.copy_download_directly)).assertIsEnabled()
        compose.onNodeWithText(text(R.string.secreto_launcher_play_pending)).assertIsDisplayed()
    }
    @Test @Config(qualifiers = "ar-w411dp-h891dp-xxhdpi")
    fun arabicRtlPicker() { show("absent_ar"); compose.onNodeWithText(text(R.string.copy_get_from_store)).assertIsNotEnabled() }
    @Test @Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
    fun frenchPicker() { show("absent_fr"); compose.onNodeWithText(text(R.string.copy_download_directly)).assertIsEnabled() }
    @Test fun noAutomaticSelection() {
        show("unselected", flow = LauncherFlow())
        compose.onNodeWithText(text(R.string.copy_get_from_store)).assertIsNotEnabled()
        compose.onNodeWithText(text(R.string.copy_download_directly)).assertDoesNotExist()
    }
    @Test fun installedOpenAndHomeAreSeparate() {
        show("installed", listOf(installed, AppInfo("com.test.home", "TV Home", null, true, true, null, "1", home = true, builtInLauncher = true)))
        compose.onNodeWithText(text(R.string.copy_open)).assertHasClickAction()
        compose.onNodeWithText(text(R.string.copy_make_home)).assertHasClickAction()
        compose.onAllNodesWithText("Secreto TV Launcher").assertCountEquals(1)
    }
    @Test fun currentIsLabelledAndNotRecommendedAgain() {
        show("current", listOf(installed.copy(home = true)))
        compose.onNodeWithText(text(R.string.copy_current)).assertExists()
        compose.onAllNodesWithText("Secreto TV Launcher").assertCountEquals(1)
    }
    @Test @Config(qualifiers = "ar-w360dp-h800dp-xxhdpi")
    fun largerArabicFontsRemainScrollable() {
        RuntimeEnvironment.setFontScale(1.3f)
        show("ar_large")
        compose.onNodeWithText(text(R.string.copy_download_directly)).assertIsDisplayed()
    }
    @Test fun confirmationShowsFixedVersionAndSize() {
        val release = LauncherRelease("v99.4.8", "${SecretoLauncher.REPOSITORY}/releases/tag/v99.4.8", "", 123456)
        show("confirmation", flow = LauncherFlow(pkg = SecretoLauncher.PACKAGE, fire = FireInstall(FireInstall.Step.Confirm, release, store = true)))
        compose.onNodeWithText(text(R.string.copy_download_and_install)).assertIsEnabled()
        compose.onNode(hasText("99.4.8", substring = true)).assertExists()
    }
}
