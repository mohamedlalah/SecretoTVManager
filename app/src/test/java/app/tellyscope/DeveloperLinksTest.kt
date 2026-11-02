// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.ui

import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import app.tellyscope.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DeveloperLinksTest {
    private val app = RuntimeEnvironment.getApplication()
    private val youtube = app.getString(R.string.developer_youtube_url)
    private val instagram = app.getString(R.string.developer_instagram_url)
    private val sent = mutableListOf<Intent>()
    private fun context(missingApp: Boolean = false, missingAll: Boolean = false, blockedApp: Boolean = false) =
        object : ContextWrapper(app) {
            override fun startActivity(intent: Intent) {
                sent += intent
                if (blockedApp && intent.`package` != null) throw SecurityException()
                if (missingAll || (missingApp && intent.`package` != null)) throw ActivityNotFoundException()
            }
        }

    @Test fun youtubeUsesConfirmedUrlAndPrefersItsApp() {
        assertEquals("https://www.youtube.com/@SecretofNet", youtube)
        openDeveloperLink(context(), youtube, youtube = true)
        assertEquals(1, sent.size)
        assertEquals("com.google.android.youtube", sent.single().`package`)
        assertEquals(youtube, sent.single().dataString)
    }

    @Test fun instagramUsesConfirmedUrlAndPrefersItsApp() {
        assertEquals("https://www.instagram.com/secretofnet/", instagram)
        openInstagramProfile(context(), instagram)
        assertEquals(1, sent.size)
        assertEquals("com.instagram.android", sent.single().`package`)
        assertEquals(instagram, sent.single().dataString)
    }

    private fun assertBrowserFallback(url: String) {
        assertEquals(2, sent.size)
        assertEquals(null, sent.last().`package`)
        assertEquals(Intent.ACTION_VIEW, sent.last().action)
        assertTrue(sent.last().hasCategory(Intent.CATEGORY_BROWSABLE))
        assertEquals(url, sent.last().dataString)
    }

    @Test fun youtubeWithoutAppFallsBackToHttps() {
        openDeveloperLink(context(missingApp = true), youtube, youtube = true)
        assertBrowserFallback(youtube)
    }

    @Test fun instagramWithoutAppFallsBackToHttps() {
        openInstagramProfile(context(missingApp = true), instagram)
        assertBrowserFallback(instagram)
    }

    @Test fun blockedInstagramFallsBackGracefully() {
        openInstagramProfile(context(blockedApp = true), instagram)
        assertBrowserFallback(instagram)
    }

    @Test fun blockedYouTubeFallsBackGracefully() {
        openDeveloperLink(context(blockedApp = true), youtube, youtube = true)
        assertBrowserFallback(youtube)
    }

    @Test fun noInstagramHandlerShowsFeedback() {
        openInstagramProfile(context(missingAll = true), instagram)
        assertEquals(2, sent.size)
        assertTrue(ShadowToast.getTextOfLatestToast().isNotBlank())
    }

    @Test fun noYouTubeHandlerShowsFeedback() {
        openDeveloperLink(context(missingAll = true), youtube, youtube = true)
        assertEquals(2, sent.size)
        assertTrue(ShadowToast.getTextOfLatestToast().isNotBlank())
    }

    @Test fun unsafeSchemesNeverLaunch() {
        openInstagramProfile(context(), "intent://example")
        openDeveloperLink(context(), "javascript:alert(1)")
        assertTrue(sent.isEmpty())
    }

    @Test fun unrelatedHostIsNeverForcedIntoInstagram() {
        openInstagramProfile(context(), "https://secretotools.com")
        assertEquals(1, sent.size)
        assertEquals(null, sent.single().`package`)
    }
}
