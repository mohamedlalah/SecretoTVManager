// SPDX-License-Identifier: GPL-3.0-only
package app.tellyscope

import android.net.Uri
import androidx.lifecycle.viewModelScope
import app.tellyscope.adb.*
import app.tellyscope.data.*
import app.tellyscope.data.ScanHit
import app.tellyscope.data.DeviceState
import app.tellyscope.ui.*
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SecretoLauncherFlowTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)
    private val tv = object : DeviceSource by FakeDeviceSource(FakeDeviceSource.Scenario.Empty) {
        var installs = 0
        var storeOpens = 0
        var homeChanges = 0
        override suspend fun known() = listOf(Device("tv", ScanHit("tv", 5555, ScanKind.Classic, "TV", null), DeviceState.Online, null, null, "11", 0))
        override fun live(deviceId: String): Flow<Snapshot> = emptyFlow()
        override suspend fun apps(deviceId: String) = emptyList<AppInfo>()
        override suspend fun suggestions(deviceId: String, apps: suspend () -> List<AppInfo>) = Suggestions.Unavailable
        override suspend fun launcherTarget(deviceId: String) = LauncherTarget(30, listOf("arm64-v8a"))
        override suspend fun install(deviceId: String, apks: List<File>, apk: ApkInfo, allowDowngrade: Boolean, onProgress: (Progress) -> Unit): InstallResult {
            assertFalse(allowDowngrade); installs++; return InstallResult.Done
        }
        override suspend fun openStore(deviceId: String, packageName: String): Boolean { storeOpens++; return false }
        override suspend fun makeHome(deviceId: String, packageName: String, service: Boolean): HistoryEntry { homeChanges++; error("Must never switch Home during install") }
    }
    private val download = object : LauncherDownload {
        var next = LauncherRelease("v99.4.8", "${SecretoLauncher.REPOSITORY}/releases/tag/v99.4.8", "https://example.invalid/fixture.apk", 123456, "a".repeat(64))
        var fetched: LauncherRelease? = null
        // Fake the output of the checked Android file reader; these tests exercise the confirmation state machine.
        val checked = PickedApp(listOf(File("fixture.apk")), ApkInfo(SecretoLauncher.PACKAGE, 99, "99.4.8"), "Secreto", LauncherRequirements(26, emptySet()))
        override suspend fun latest(launcher: KnownLauncher) = next
        override suspend fun fetch(launcher: KnownLauncher, release: LauncherRelease, onProgress: (Progress) -> Unit): PickedApp { fetched = release; return checked }
        override suspend fun open(uri: Uri, launcher: KnownLauncher) = checked
    }
    private lateinit var vm: TellyscopeViewModel
    @Before fun start() { Dispatchers.setMain(dispatcher) }
    @After fun stop() { if (::vm.isInitialized) vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    private fun TestScope.started() {
        vm = TellyscopeViewModel(tv, launcherDownload = download, unverified = { "https://unverified.example/" })
        runCurrent(); vm.openLauncherFlow(); runCurrent()
    }
    @Test fun onlyExplicitConfirmationDownloadsTheFrozenAssetAndInstallsWithoutHomeChanges() = with(scope) {
        started()
        assertEquals("", vm.state.value.device.launcher?.pkg)
        vm.checkLauncherApk(SecretoLauncher.PACKAGE); runCurrent()
        val confirmed = vm.state.value.device.launcher?.fire!!
        assertEquals(FireInstall.Step.Confirm, confirmed.step)
        assertNull(confirmed.list) // Independent bundled trust, not approval of an unsigned upstream catalog.
        assertEquals(0, tv.installs); assertNull(download.fetched)
        val original = download.next
        download.next = original.copy(version = "v100.0.0")
        vm.installLauncherApk(); runCurrent()
        assertSame(original, download.fetched)
        assertEquals(1, tv.installs); assertEquals(0, tv.homeChanges)
    }
    @Test fun unverifiedPlayListingCannotBeOpenedEvenByDirectViewModelCall() = with(scope) {
        started(); vm.installLauncher(SecretoLauncher.PACKAGE); runCurrent()
        assertEquals(0, tv.storeOpens)
    }
    @Test fun pickedFileWaitsForExplicitInstallationConfirmation() = with(scope) {
        started(); vm.checkLauncherApk(SecretoLauncher.PACKAGE); runCurrent()
        // The manual-file recovery is offered from the failed download step.
        vm.installLauncherApk(); runCurrent()
        // A new flow with a fake download that refuses the network enters the recovery picker.
        vm.closeLauncherFlow()
        vm.viewModelScope.cancel()
        vm = TellyscopeViewModel(tv, launcherDownload = object : LauncherDownload {
            override suspend fun latest(launcher: KnownLauncher): LauncherRelease = throw java.io.IOException("offline")
            override suspend fun open(uri: Uri, launcher: KnownLauncher) = download.open(uri, launcher)
            override suspend fun fetch(launcher: KnownLauncher, release: LauncherRelease, onProgress: (Progress) -> Unit) = download.fetch(launcher, release, onProgress)
        })
        runCurrent(); vm.openLauncherFlow(); vm.checkLauncherApk(SecretoLauncher.PACKAGE); runCurrent()
        val before = tv.installs
        vm.pickedLauncherApk(Uri.parse("content://test/app")); runCurrent()
        assertEquals(before, tv.installs)
        assertEquals(FireInstall.Step.Confirm, vm.state.value.device.launcher?.fire?.step)
        vm.installLauncherApk(); runCurrent()
        assertEquals(before + 1, tv.installs)
    }
}
