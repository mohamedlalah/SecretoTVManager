// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.data

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import app.tellyscope.adb.ApkInfo
import app.tellyscope.adb.Progress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/** A launcher's latest release: [page] is shown before anything downloads, [apk] is its one APK. */
data class LauncherRelease(val version: String, val page: String, val apk: String, val size: Long, val sha256: String? = null)

/**
 * The file [launcher] is saved as: its list name with spaces as `-` and only `[A-Za-z0-9._-]` kept,
 * so no `/` or `..` path, at most 64 characters; its package name when nothing is left.
 */
fun apkName(launcher: KnownLauncher): String =
    launcher.name.replace(' ', '-').filter { it.isLetterOrDigit() && it.code < 128 || it in "._-" }.take(64)
        .ifEmpty { launcher.pkg } + ".apk"

/**
 * GitHub's API turned the lookup down: it allows 60 an hour per network without a login. [until] is
 * when it takes them again, epoch millis, when it said.
 */
class RateLimited(val until: Long?) : IOException("rate-limited")

/** The APK isn't the launcher it should be ([KnownLauncher.accepts]): another package, or not signed by its developer. */
class WrongApk : IOException("not the launcher")

class ReleaseUnavailable(val messageRes: Int) : IOException("No compatible verified release")

/** A launcher's APK the user downloaded and picked, checked before anything reaches the TV. Faked in tests. */
interface LauncherPick {
    /** Throws [WrongApk], or IOException when it can't be read. */
    suspend fun open(uri: Uri, launcher: KnownLauncher): PickedApp
}

/**
 * Fire OS's way to its launcher: there's no Play Store, so Tellyscope fetches it from its developer's
 * [KnownLauncher.releases] (`AndroidLauncherDownload`, GitHub build only). Faked in tests.
 */
interface LauncherDownload : LauncherPick {
    /** Throws on no network, no [KnownLauncher.releases], or a release that doesn't parse. */
    suspend fun latest(launcher: KnownLauncher): LauncherRelease
    suspend fun latest(launcher: KnownLauncher, target: LauncherTarget): LauncherRelease = latest(launcher)

    /** Downloads [release]'s APK and checks it. Throws [WrongApk], or IOException on a failed download. */
    suspend fun fetch(launcher: KnownLauncher, release: LauncherRelease, onProgress: (Progress) -> Unit): PickedApp
}

/** The pick and the check, which both builds have; the GitHub build's [LauncherDownload] adds the download. */
open class AndroidLauncherPick(context: Context) : LauncherPick {
    protected val ctx: Context = context.applicationContext
    protected val dir get() = File(ctx.cacheDir, "launcher")

    override suspend fun open(uri: Uri, launcher: KnownLauncher): PickedApp = withContext(Dispatchers.IO) {
        dir.deleteRecursively()
        dir.mkdirs()
        val file = File(dir, apkName(launcher))
        val input = ctx.contentResolver.openInputStream(uri) ?: throw IOException("can't read $uri")
        input.use { s ->
            file.outputStream().use { out ->
                val buf = ByteArray(64 * 1024)
                var done = 0L
                while (true) {
                    ensureActive()
                    val n = s.read(buf).takeIf { it >= 0 } ?: break
                    done += n
                    // Far past the launcher's size: some other file, never read whole.
                    if (done > launcher.maxBytes) throw WrongApk()
                    out.write(buf, 0, n)
                }
            }
        }
        read(file, launcher)
    }

    /** Parse identity, then verify APK content signatures independently with AOSP apksig. */
    protected fun read(file: File, launcher: KnownLauncher): PickedApp {
        // Package archive parsing alone is not proof that APK content signatures are valid.
        val verified = runCatching { com.android.apksig.ApkVerifier.Builder(file).setMinCheckedPlatformVersion(26).build().verify() }.getOrNull() ?: throw WrongApk()
        if (!verified.isVerified) throw WrongApk()
        val signers = verified.signerCertificates.map { launcherSha256(it.encoded) }.toSet()
        if (signers.isEmpty() || !launcher.signers.containsAll(signers)) throw WrongApk()
        val pm = ctx.packageManager
        val pkg = if (Build.VERSION.SDK_INT >= 28) {
            @Suppress("DEPRECATION") // the flags overload is API 33+
            pm.getPackageArchiveInfo(file.path, PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_CONFIGURATIONS)
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(file.path, PackageManager.GET_SIGNATURES or PackageManager.GET_CONFIGURATIONS)
        } ?: throw WrongApk()
        if (!launcher.accepts(pkg.packageName, signers)) throw WrongApk()
        if (launcher.pkg == SecretoLauncher.PACKAGE) {
            val flags = pkg.applicationInfo?.flags ?: throw WrongApk()
            val debugOrTest = android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE or android.content.pm.ApplicationInfo.FLAG_TEST_ONLY
            if (flags and debugOrTest != 0) throw WrongApk()
        }
        return PickedApp(
            files = listOf(file),
            info = ApkInfo(pkg.packageName, PackageInfoCompat.getLongVersionCode(pkg), pkg.versionName),
            label = launcher.name,
            launcherRequirements = LauncherRequirements(
                pkg.applicationInfo?.minSdkVersion ?: throw WrongApk(),
                java.util.zip.ZipFile(file).use { zip -> zip.entries().asSequence()
                    .map { it.name }.filter { it.startsWith("lib/") && it.endsWith(".so") }
                    .map { it.split('/')[1] }.toSet() },
                pkg.reqFeatures.orEmpty().filter { it.flags and android.content.pm.FeatureInfo.FLAG_REQUIRED != 0 }.mapNotNull { it.name }.toSet(),
                pkg.reqFeatures.orEmpty().filter { it.name == null }.maxOfOrNull { it.reqGlEsVersion } ?: 0,
            ),
        )
    }
}
