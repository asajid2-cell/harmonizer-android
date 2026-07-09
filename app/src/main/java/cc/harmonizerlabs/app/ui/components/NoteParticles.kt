package cc.harmonizerlabs.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import cc.harmonizerlabs.app.ui.theme.NoteColors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Floating music-note glyphs that drift upward while audio plays — a port of the web
 * visualizer's orbiting `--note-color` particles (rgba rose / sky / peach / cyan-green /
 * hot-pink / periwinkle, each with a soft drop-shadow glow).
 *
 * Renders nothing when paused, so it costs nothing off the playback path.
 */
private data class Note(
    val glyph: String,
    val xFrac: Float,   // base horizontal position (0..1)
    val phase: Float,   // 0..1 offset so particles don't reset together
    val speed: Float,   // vertical speed multiplier
    val size: Float,    // base text size in px-ish (scaled by canvas)
    val colorIdx: Int,
)

private val GLYPHS = listOf("♫", "♪", "♭", "♯") // ♫ ♪ ♭ ♯

private val NOTES = List(11) { i ->
    Note(
        glyph    = GLYPHS[i % GLYPHS.size],
        xFrac    = ((i * 0.6180339f) % 1f),          // golden-ratio spread
        phase    = ((i * 0.382f) % 1f),
        speed    = 0.5f + (i % 4) * 0.18f,
        size     = 26f + (i % 3) * 10f,
        colorIdx = i % NoteColors.size,
    )
}

@Composable
fun NoteParticles(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isPlaying) return

    var elapsedMs by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        var start = 0L
        while (true) {
            withFrameMillis { ms ->
                if (start == 0L) start = ms
                elapsedMs = ms - start
            }
        }
    }

    Canvas(modifier = modifier) {
        val seconds = elapsedMs / 1000f
        NOTES.forEach { n ->
            // progress 0..1 cycling upward; staggered by phase
            val t = ((seconds * n.speed * 0.12f) + n.phase) % 1f
            val y = size.height * (1f - t)
            val drift = sin((t + n.phase) * 2f * PI).toFloat() * size.width * 0.05f
            val x = size.width * n.xFrac + drift
            // fade in/out, peaking mid-flight
            val alpha = (sin(t * PI).toFloat()).coerceIn(0f, 1f) * 0.85f
            if (alpha <= 0.01f) return@forEach

            val base = NoteColors[n.colorIdx]
            val argb = base.copy(alpha = alpha).toArgb()
            val glowArgb = base.copy(alpha = alpha * 0.7f).toArgb()
            val px = n.size * (size.minDimension / 360f).coerceIn(0.6f, 1.6f)

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = argb
                    textSize = px
                    setShadowLayer(px * 0.45f, 0f, 0f, glowArgb)
                }
                val rot = sin((t * 2f + n.phase) * PI).toFloat() * 18f
                save()
                rotate(rot, x, y)
                drawText(n.glyph, x, y, paint)
                restore()
            }
        }
    }
}
