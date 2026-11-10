// SPDX-License-Identifier: GPL-3.0-only
package app.tellyscope.data

import java.security.MessageDigest

/** Fork-owned trust anchor; upstream signed catalogs are never edited or allowed to replace this entry. */
object SecretoLauncher {
    const val PACKAGE = "com.secreto.tvlauncher"
    const val REPOSITORY = "https://github.com/mohamedlalah/SecretoTVLauncher"
    // From the existing Launcher's production UpdateEnvironment.java, independently checked against its public APK.
    const val SIGNER = "0ad4b9391dd36dc4428d98936be21d4e205b2b375989b44aa582676a3c7a13a1"
    // Enable only after independently verifying a public listing for PACKAGE, not Play Console approval alone.
    const val PLAY_STORE_VERIFIED = false
    val icon = ListFile("secreto-launcher/icon.png", "0befbf74e6619ebf61a8edfa271fa06713b1477fcd6d98eab4dd508e5551abf7")
    val screenshot = ListFile("secreto-launcher/home.jpeg", "e0b13672f91854114e11ff055950e5d7d9243a03dd2da0f9d247705ab37dfd07")
    val entry = KnownLauncher(
        PACKAGE, "Secreto TV Launcher", "Customizable Android TV and Google TV home screen.",
        signers = setOf(SIGNER), releases = "$REPOSITORY/releases", icon = icon, screenshot = screenshot,
        // No accessibility service is asserted or enabled; Fire OS compatibility is not assumed.
    )
    fun merge(upstream: List<KnownLauncher>): List<KnownLauncher> =
        listOf(entry) + upstream.filterNot { it.pkg == PACKAGE }.distinctBy { it.pkg }

    fun isBundled(file: ListFile) = file == icon || file == screenshot
    fun image(file: ListFile): ByteArray? {
        if (!isBundled(file)) return null
        val bytes = SecretoLauncher::class.java.getResourceAsStream("/${file.file}")?.use { input ->
            val out = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                if (out.size() + n > Picture.MAX_BYTES) return null
                out.write(buffer, 0, n)
            }
            out.toByteArray()
        } ?: return null
        return bytes.takeIf { it.size <= Picture.MAX_BYTES && launcherSha256(it) == file.sha256 && Picture.ok(it) }
    }
}

internal fun launcherSha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

/** Read from the connected TV, never from the phone. */
data class LauncherTarget(val sdk: Int, val abis: List<String>, val features: Set<String> = emptySet(), val glEs: Int = 0)
data class LauncherRequirements(val minSdk: Int, val abis: Set<String>, val features: Set<String> = emptySet(), val glEs: Int = 0) {
    fun compatible(target: LauncherTarget) = target.sdk >= minSdk && (abis.isEmpty() || target.abis.any { it in abis }) && target.features.containsAll(features) && target.glEs >= glEs
}
