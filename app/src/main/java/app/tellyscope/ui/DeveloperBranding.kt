// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.tellyscope.R

/** Shared, voluntary social links. Platform marks keep their colors and never mirror in RTL. */
@Composable
internal fun DeveloperSocialLinks(showHeading: Boolean = false, compact: Boolean = false) {
    val context = LocalContext.current
    val youtube = stringResource(R.string.developer_youtube_url)
    val instagram = stringResource(R.string.developer_instagram_url)
    val fontScale = LocalDensity.current.fontScale
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showHeading) Text(stringResource(R.string.social_follow), style = MaterialTheme.typography.titleSmall)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val horizontal = maxWidth >= (if (compact) 256.dp else 320.dp) && fontScale <= 1.3f
            val youtubeButton: @Composable (Modifier) -> Unit = { modifier ->
                SocialButton(R.drawable.social_youtube, R.string.social_youtube, R.string.social_open_youtube, modifier, compact) {
                    openDeveloperLink(context, youtube, youtube = true)
                }
            }
            val instagramButton: @Composable (Modifier) -> Unit = { modifier ->
                SocialButton(R.drawable.social_instagram, R.string.social_instagram, R.string.social_open_instagram, modifier, compact) {
                    openInstagramProfile(context, instagram)
                }
            }
            if (horizontal) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                youtubeButton(Modifier.weight(1f))
                instagramButton(Modifier.weight(1f))
            } else Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                youtubeButton(Modifier.fillMaxWidth())
                instagramButton(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SocialButton(icon: Int, label: Int, description: Int, modifier: Modifier, compact: Boolean, onClick: () -> Unit) {
    val spoken = stringResource(description)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = if (compact) 48.dp else 56.dp).semantics { contentDescription = spoken },
        shape = MaterialTheme.shapes.large,
        contentPadding = if (compact) PaddingValues(horizontal = 8.dp, vertical = 8.dp) else ButtonDefaults.ContentPadding,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Image(painterResource(icon), contentDescription = null, Modifier.size(if (compact) 24.dp else 32.dp))
            Spacer(Modifier.width(if (compact) 6.dp else 10.dp))
            Text(stringResource(label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
