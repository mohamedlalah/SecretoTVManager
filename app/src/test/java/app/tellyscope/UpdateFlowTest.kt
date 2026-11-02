// SPDX-License-Identifier: GPL-3.0-only
package app.tellyscope

import androidx.lifecycle.viewModelScope
import app.tellyscope.data.FakeDeviceSource
import app.tellyscope.data.UpdateCheck
import app.tellyscope.data.UpdateStatus
import app.tellyscope.data.RELEASES
import app.tellyscope.ui.TellyscopeViewModel
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UpdateFlowTest {
    @get:Rule val tmp = TemporaryFolder()
    private lateinit var vm: TellyscopeViewModel
    @Before fun start() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun stop() { if (::vm.isInitialized) vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    private fun viewModel(check: UpdateCheck): TellyscopeViewModel {
        vm = TellyscopeViewModel(FakeDeviceSource(FakeDeviceSource.Scenario.Empty), updates = check)
        return vm
    }
    private suspend fun await(condition: () -> Boolean) = withTimeout(5_000) { while (!condition()) delay(10) }

    @Test fun noReleaseIsVisibleInUiStateWithoutAnOffer() = runBlocking {
        viewModel(UpdateCheck(tmp.newFile(), "1.0.0", false) { throw FileNotFoundException("404") })
        vm.checkForUpdate()
        await { vm.state.value.updateStatus == UpdateStatus.NoPublishedUpdates }
        assertNull(vm.state.value.update)
        assertNull(vm.state.value.available)
    }

    @Test fun disablingChecksClearsOffersAndPreventsRequests() = runBlocking {
        var calls = 0
        val check = UpdateCheck(tmp.newFile(), "1.0.0", false) {
            calls++
            """{"tag_name":"v1.1.0","draft":false,"prerelease":false,"body":"Notes"}""".toByteArray()
        }
        viewModel(check)
        await { vm.state.value.checkUpdates == true }
        vm.checkForUpdate()
        await { vm.state.value.update != null }
        assertEquals("$RELEASES/tag/v1.1.0", vm.state.value.update?.url)
        vm.setCheckUpdates(false)
        await { !check.enabled }
        assertNull(vm.state.value.update)
        assertNull(vm.state.value.available)
        assertEquals(UpdateStatus.Disabled, vm.state.value.updateStatus)
        vm.checkForUpdate()
        delay(30)
        assertEquals(1, calls)
    }
}
