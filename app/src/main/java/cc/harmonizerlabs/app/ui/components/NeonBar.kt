package cc.harmonizerlabs.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import cc.harmonizerlabs.app.ui.theme.*

/**
 * Animated neon top bar — the rainbow gradient strip atop every harmonizer.html screen,
 * including its pulsing magenta box-shadow glow (CSS `bar-glow 3s ease-in-out`).
 */
@Composable
fun NeonBar(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "neon")
    val glow by inf.animateFloat(
        initialValue = 0.4f,
        targetValue  = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "neon_glow",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .neonGlow(NeonMagenta, glowRadius = 12.dp, intensity = glow)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        NeonMagenta,
                        NeonOrange,
                        NeonCyan,
                        NeonLime,
                        NeonMagenta,
                    )
                )
            )
    )
}
