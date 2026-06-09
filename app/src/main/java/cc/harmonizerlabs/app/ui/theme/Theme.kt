package cc.harmonizerlabs.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HarmonizerColorScheme = darkColorScheme(
    primary          = NeonCyan,
    onPrimary        = Black,
    primaryContainer = CyanGlow25,
    secondary        = NeonLime,
    onSecondary      = Black,
    tertiary         = NeonMagenta,
    onTertiary       = Black,
    background       = Black,
    onBackground     = TextPrimary,
    surface          = SurfaceDark,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceMid,
    onSurfaceVariant = TextMuted,
    outline          = NeonLime,
    error            = NeonOrange,
    onError          = Black,
)

@Composable
fun HarmonizerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HarmonizerColorScheme,
        typography  = HarmonizerTypography,
        content     = content,
    )
}
