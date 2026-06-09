package cc.harmonizerlabs.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import cc.harmonizerlabs.app.api.models.Beat
import cc.harmonizerlabs.app.api.models.CanonAlignment
import cc.harmonizerlabs.app.api.models.Section
import cc.harmonizerlabs.app.model.HarmonizerMode
import cc.harmonizerlabs.app.ui.theme.*
import kotlin.math.min

private val sectionPalette = listOf(
    Color(0xFF1A2848),  // verse — dark blue
    Color(0xFF2A1840),  // chorus — dark purple
    Color(0xFF122830),  // bridge — dark teal
    Color(0xFF281420),  // outro  — dark rose
    Color(0xFF181828),  // intro  — dark indigo
)

/**
 * Beat visualizer canvas matching the Harmonizer web grid layout.
 * Draws beats as colored squares; highlights current beat in cyan.
 * In canon mode draws arc lines between paired beats.
 */
@Composable
fun BeatVisualizer(
    beats: List<Beat>,
    sections: List<Section>,
    canonAlignment: CanonAlignment?,
    currentBeatIndex: Int,
    mode: HarmonizerMode,
    modifier: Modifier = Modifier,
) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue   = 0.6f,
        targetValue    = 1.0f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        if (beats.isEmpty()) return@Canvas

        val cols   = 32
        val rows   = (beats.size + cols - 1) / cols
        val margin = 2f
        val cellW  = (size.width  - margin * (cols + 1)) / cols
        val cellH  = min((size.height - margin * (rows + 1)) / rows, cellW)

        // build beat-index → section-index lookup once
        val beatSection = IntArray(beats.size) { bi ->
            val beatStart = beats[bi].start
            sections.indexOfLast { it.start <= beatStart }.takeIf { it >= 0 } ?: 0
        }

        // draw beat squares
        beats.forEachIndexed { i, beat ->
            val col = i % cols
            val row = i / cols
            val x   = margin + col * (cellW + margin)
            val y   = margin + row * (cellH + margin)

            val sectionIdx  = beatSection[i].coerceIn(0, sectionPalette.lastIndex)
            val baseColor   = sectionPalette[sectionIdx % sectionPalette.size]
            val energy      = beat.medianVolume.toFloat().coerceIn(0f, 1f)
            val energyColor = baseColor.copy(alpha = 0.3f + energy * 0.55f)

            val fillColor = when {
                i == currentBeatIndex -> NeonCyan.copy(alpha = pulse)
                canonAlignment != null && i < canonAlignment.pairs.size &&
                        canonAlignment.pairs[i] == currentBeatIndex -> NeonMagenta.copy(alpha = 0.6f * pulse)
                else -> energyColor
            }

            drawRect(color = fillColor, topLeft = Offset(x, y), size = Size(cellW, cellH))

            // border for current beat
            if (i == currentBeatIndex) {
                drawRect(
                    color   = NeonCyan,
                    topLeft = Offset(x, y),
                    size    = Size(cellW, cellH),
                    style   = Stroke(width = 1.5f),
                )
            }
        }

        // canon arcs between paired beats (only when mode == CANON)
        if (mode == HarmonizerMode.CANON && canonAlignment != null) {
            drawCanonArcs(beats, canonAlignment, currentBeatIndex, cols, cellW, cellH, margin)
        }
    }
}

private fun DrawScope.drawCanonArcs(
    beats: List<Beat>,
    ca: CanonAlignment,
    currentBeat: Int,
    cols: Int,
    cellW: Float,
    cellH: Float,
    margin: Float,
) {
    // Draw only arcs near the current beat to keep it readable
    val window = 16
    val start  = (currentBeat - window).coerceAtLeast(0)
    val end    = (currentBeat + window).coerceAtMost(ca.pairs.size - 1)

    for (i in start..end) {
        val pairIdx = ca.pairs.getOrElse(i) { -1 }
        if (pairIdx < 0 || pairIdx >= beats.size) continue

        val sim      = ca.pairSimilarity.getOrElse(i) { 0.0 }.toFloat()
        val arcColor = NeonMagenta.copy(alpha = (sim * 0.4f).coerceIn(0.05f, 0.4f))

        val x1 = margin + (i % cols) * (cellW + margin) + cellW / 2
        val y1 = margin + (i / cols) * (cellH + margin) + cellH / 2
        val x2 = margin + (pairIdx % cols) * (cellW + margin) + cellW / 2
        val y2 = margin + (pairIdx / cols) * (cellH + margin) + cellH / 2

        drawLine(
            color       = arcColor,
            start       = Offset(x1, y1),
            end         = Offset(x2, y2),
            strokeWidth = 1f,
        )
    }
}
