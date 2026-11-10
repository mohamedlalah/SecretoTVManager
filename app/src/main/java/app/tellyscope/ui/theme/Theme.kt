// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.ui.theme

import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Ocean Blue is the default on every Android version, independent of wallpaper colors.
// Light surfaces and deep blue accents keep controls and text readable.
// Every role is set, tonal-spot tones from that seed: a role left out falls back to
// Material's stock purple.
private val LightBrand = lightColorScheme(
    primary = Color(0xFF0874BA),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF17344C),
    inversePrimary = Color(0xFF9ECAFF),
    secondary = Color(0xFF465F7B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E7FF),
    onSecondaryContainer = Color(0xFF0B1C30),
    tertiary = Color(0xFF4B607C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD3E4FF),
    onTertiaryContainer = Color(0xFF041C35),
    background = Color(0xFFE4F1FA),
    onBackground = Color(0xFF17344C),
    surface = Color(0xFFE4F1FA),
    onSurface = Color(0xFF17344C),
    surfaceVariant = Color(0xFFDDECF6),
    onSurfaceVariant = Color(0xFF456177),
    inverseSurface = Color(0xFF2C3541),
    inverseOnSurface = Color(0xFFEDF2FA),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF637F94),
    outlineVariant = Color(0xFFBED5E5),
    surfaceBright = Color(0xFFE4F1FA),
    surfaceContainer = Color(0xFFF5FAFE),
    surfaceContainerHigh = Color(0xFFEBF5FC),
    surfaceContainerHighest = Color(0xFFDCECF8),
    surfaceContainerLow = Color(0xFFECF6FD),
    surfaceContainerLowest = Color(0xFFF8FCFF),
    surfaceDim = Color(0xFFD6EAF7),
    primaryFixed = Color(0xFFD1E4FF),
    primaryFixedDim = Color(0xFF9ECAFF),
    onPrimaryFixed = Color(0xFF17344C),
    onPrimaryFixedVariant = Color(0xFF00497D),
    secondaryFixed = Color(0xFFD6E7FF),
    secondaryFixedDim = Color(0xFFB6CBE4),
    onSecondaryFixed = Color(0xFF0B1C30),
    onSecondaryFixedVariant = Color(0xFF2F4861),
    tertiaryFixed = Color(0xFFD3E4FF),
    tertiaryFixedDim = Color(0xFFB3C8E8),
    onTertiaryFixed = Color(0xFF041C35),
    onTertiaryFixedVariant = Color(0xFF334863),
)

private val DarkBrand = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    inversePrimary = Color(0xFF0061A4),
    secondary = Color(0xFFB6CBE4),
    onSecondary = Color(0xFF193149),
    secondaryContainer = Color(0xFF2F4861),
    onSecondaryContainer = Color(0xFFD6E7FF),
    tertiary = Color(0xFFB3C8E8),
    onTertiary = Color(0xFF1C314B),
    tertiaryContainer = Color(0xFF334863),
    onTertiaryContainer = Color(0xFFD3E4FF),
    background = Color(0xFF0E141D),
    onBackground = Color(0xFFDCE5F0),
    surface = Color(0xFF0E141D),
    onSurface = Color(0xFFDCE5F0),
    surfaceVariant = Color(0xFF414C59),
    onSurfaceVariant = Color(0xFFC1CBD9),
    inverseSurface = Color(0xFFDCE5F0),
    inverseOnSurface = Color(0xFF2C3541),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8B96A5),
    outlineVariant = Color(0xFF414C59),
    surfaceBright = Color(0xFF333C48),
    surfaceContainer = Color(0xFF19212C),
    surfaceContainerHigh = Color(0xFF242D38),
    surfaceContainerHighest = Color(0xFF2E3743),
    surfaceContainerLow = Color(0xFF17202B),
    surfaceContainerLowest = Color(0xFF090F17),
    surfaceDim = Color(0xFF0E141D),
    primaryFixed = Color(0xFFD1E4FF),
    primaryFixedDim = Color(0xFF9ECAFF),
    onPrimaryFixed = Color(0xFF001D36),
    onPrimaryFixedVariant = Color(0xFF00497D),
    secondaryFixed = Color(0xFFD6E7FF),
    secondaryFixedDim = Color(0xFFB6CBE4),
    onSecondaryFixed = Color(0xFF0B1C30),
    onSecondaryFixedVariant = Color(0xFF2F4861),
    tertiaryFixed = Color(0xFFD3E4FF),
    tertiaryFixedDim = Color(0xFFB3C8E8),
    onTertiaryFixed = Color(0xFF041C35),
    onTertiaryFixedVariant = Color(0xFF334863),
)

/** Numbers come first: the app font with tabular figures, so values line up and don't jitter as they update. */
val StatNumber = TextStyle(fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 34.sp, fontFeatureSettings = "tnum")
val SmallNumber = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontFeatureSettings = "tnum")

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TellyscopeTheme(
    dark: Boolean = false,
    dynamic: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkBrand
        else -> LightBrand
    }
    MaterialExpressiveTheme(
        colorScheme = colors,
        motionScheme = MotionScheme.expressive(),
        typography = Typography(),
        content = content,
    )
}
