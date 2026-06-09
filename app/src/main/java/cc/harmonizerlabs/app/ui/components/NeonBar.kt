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

/** Animated cycling neon top bar — matches the CSS gradient bar on harmonizer.html */
@Composable
fun NeonBar(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "neon")
    val offset by inf.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "neon_offset",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
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
