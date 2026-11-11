// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope

import app.tellyscope.data.AdbDeviceSource
import app.tellyscope.data.SharedLists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.rules.ExternalResource

/**
 * Keeps [SharedLists] on the test fixtures. A new [AdbDeviceSource] starts downloading the live launcher, label and
 * streaming lists in the background; left running, that download can replace the fixtures in whichever test runs next
 * (upstream's live list, for one, lets FLauncher download directly). Before each test the fixtures are restored; after
 * it, every [track]ed source's background work is cancelled and waited for, so nothing is still in flight, and the
 * fixtures are restored again.
 */
class SharedListsIsolation : ExternalResource() {
    private val sources = mutableListOf<AdbDeviceSource>()

    fun <T : AdbDeviceSource> track(source: T): T = source.also { synchronized(sources) { sources += it } }

    override fun before() = SharedLists.reseed()

    override fun after() {
        val tracked = synchronized(sources) { sources.toList().also { sources.clear() } }
        tracked.forEach { it.stopBackgroundWork() }
        SharedLists.reseed()
    }
}

/** Cancels the source's own coroutine scope and waits until its jobs (the list download among them) have finished. */
private fun AdbDeviceSource.stopBackgroundWork() {
    val field = AdbDeviceSource::class.java.getDeclaredField("scope").apply { isAccessible = true }
    val job = checkNotNull((field.get(this) as CoroutineScope).coroutineContext[Job]) { "AdbDeviceSource scope has no Job" }
    runBlocking { withTimeout(60_000) { job.cancelAndJoin() } }
}
