// SPDX-License-Identifier: GPL-3.0-only
package app.tellyscope.data

const val PROJECT_REPO = "https://github.com/mohamedlalah/SecretoTVManager"
const val UPSTREAM_REPO = "https://github.com/man15h/tellyscope"
const val RELEASES = "$PROJECT_REPO/releases"
const val RELEASES_API = "https://api.github.com/repos/mohamedlalah/SecretoTVManager/releases/latest"

/** Metadata only; updates open the official release page, never an installer. */
data class Update(val version: String, val notes: String, val url: String)
enum class UpdateStatus { NotChecked, NoPublishedUpdates, UpToDate, Available, Failed, Disabled }
interface Updates {
    val alpha: Boolean
    val status: UpdateStatus
    var enabled: Boolean
    suspend fun check(): Update?
    suspend fun newer(): List<Changelog.Release>?
}
