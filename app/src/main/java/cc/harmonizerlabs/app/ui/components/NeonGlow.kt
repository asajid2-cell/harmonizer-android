package cc.harmonizerlabs.app.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Soft outer neon bloom drawn behind a (usually sharp-cornered) element — the look the web
 * gets for free from CSS `box-shadow: 0 0 Npx <color>`. Compose has no colored box-shadow on
 * every API level, so we approximate it with a few concentric strokes that expand outward and
 * fade, which reads as a believable glow halo without RenderEffect (works on minSdk 26).
 *
 * Cheap (a handful of strokes) and static-friendly; pass an animated [intensity] to breathe it.
 */
fun Modifier.neonGlow(
    color: Color,
    glowRadius: Dp = 10.dp,
    intensity: Float = 1f,
    steps: Int = 6,
): Modifier = this.drawBehind {
    if (intensity <= 0.01f) return@drawBehind
    val rPx = glowRadius.toPx()
    for (i in 1..steps) {
        val frac   = i / steps.toFloat()        // 0..1 outward
        val spread = rPx * frac
        val a      = intensity * (1f - frac) * 0.35f
        if (a <= 0.004f) continue
        drawRect(
            color   = color.copy(alpha = a),
            topLeft = Offset(-spread, -spread),
            size    = Size(size.width + spread * 2, size.height + spread * 2),
            style   = Stroke(width = rPx * frac * 0.9f + 1.5f),
        )
    }
}
