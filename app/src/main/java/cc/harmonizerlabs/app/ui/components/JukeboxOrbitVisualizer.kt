package cc.harmonizerlabs.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import cc.harmonizerlabs.app.api.models.Beat
import cc.harmonizerlabs.app.api.models.LoopCandidate
import cc.harmonizerlabs.app.api.models.Section
import cc.harmonizerlabs.app.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val orbitSectionPalette = listOf(
    Color(0xFF2E6BFF), Color(0xFFB14CFF), Color(0xFF18C7B0), Color(0xFFFF5C8A),
    Color(0xFF6E7BFF), Color(0xFFFFA94C), Color(0xFF4CE0FF), Color(0xFFFF7AD1),
)

/**
 * Circular "Infinite Jukebox" orbit — the visualization the web uses for jukebox/eternal.
 * Beats sit on a ring (coloured by section); quadratic chords bend through the centre to
 * connect jump-candidate beats (opacity by similarity); a glowing cyan playhead orbits the
 * current beat and the chords leaving it light up as the engine prepares to jump.
 */
@Composable
fun JukeboxOrbitVisualizer(
    beats: List<Beat>,
    sections: List<Section>,
    loopCandidates: List<LoopCandidate>?,
    currentBeatIndex: Int,
    modifier: Modifier = Modifier,
    segments: List<cc.harmonizerlabs.app.api.models.Segment> = emptyList(),
) {
    val energies = remember(beats, segments) { beatEnergies(beats, segments) }
    val pulse by rememberInfiniteTransition(label = "orbit").animateFloat(
        initialValue  = 0.55f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "orbit_pulse",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val n = beats.size
        if (n == 0) return@Canvas

        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = min(size.width, size.height) / 2f * 0.84f
        val center = Offset(cx, cy)

        fun pointFor(i: Int, r: Float = radius): Offset {
            val a = -PI.toFloat() / 2f + 2f * PI.toFloat() * (i.toFloat() / n)
            return Offset(cx + r * cos(a), cy + r * sin(a))
        }

        // Per-beat section lookup
        val beatSection = IntArray(n) { bi ->
            val s = beats[bi].start
            sections.indexOfLast { it.start <= s }.takeIf { it >= 0 } ?: 0
        }

        // ── Jump chords (behind the ring) ────────────────────────────────────────
        // Strongest similarities drawn last (on top); cap to keep it readable on huge tracks.
        val cands = loopCandidates.orEmptySortedTopBySimilarity(280)
        cands.forEach { c ->
            if (c.source !in 0 until n || c.target !in 0 until n) return@forEach
            val p1 = pointFor(c.source)
            val p2 = pointFor(c.target)
            // Control point pulled most of the way to the centre → classic inward bend
            val mid  = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
            val control = Offset(
                mid.x + (center.x - mid.x) * 0.85f,
                mid.y + (center.y - mid.y) * 0.85f,
            )
            val near = c.source == currentBeatIndex || c.target == currentBeatIndex
            val sim  = c.similarity.toFloat().coerceIn(0f, 1f)
            val baseAlpha = (0.06f + sim * 0.28f)
            val color =
                if (near) NeonCyan.copy(alpha = (0.5f + 0.5f * sim) * pulse)
                else      NeonCyan.copy(alpha = baseAlpha)
            val path = Path().apply {
                moveTo(p1.x, p1.y)
                quadraticBezierTo(control.x, control.y, p2.x, p2.y)
            }
            drawPath(path, color = color, style = Stroke(width = if (near) 2.4f else 1.1f))
        }

        // ── Beat ring (coloured ticks by section, brightened by energy) ──────────
        val inner = radius * 0.93f
        for (i in 0 until n) {
            val sec   = orbitSectionPalette[beatSection[i] % orbitSectionPalette.size]
            val energy = energies.getOrElse(i) { 0.6f }
            val a = -PI.toFloat() / 2f + 2f * PI.toFloat() * (i.toFloat() / n)
            val tickInner = if (i == currentBeatIndex) radius * 0.86f else inner
            val pIn  = Offset(cx + tickInner * cos(a), cy + tickInner * sin(a))
            val pOut = Offset(cx + radius * cos(a), cy + radius * sin(a))
            drawLine(
                color       = sec.copy(alpha = 0.5f + energy * 0.45f),
                start       = pIn,
                end         = pOut,
                strokeWidth = if (n > 400) 2f else 3f,
            )
        }

        // ── Playhead ─────────────────────────────────────────────────────────────
        val head = pointFor(currentBeatIndex)
        // radar line from centre
        drawLine(
            color = NeonCyan.copy(alpha = 0.18f * pulse),
            start = center,
            end   = head,
            strokeWidth = 1.5f,
        )
        // glow + dot
        drawCircle(NeonCyan.copy(alpha = 0.25f * pulse), radius = 11f, center = head)
        drawCircle(NeonCyan.copy(alpha = pulse), radius = 5f, center = head)
        drawCircle(TextPrimary, radius = 2f, center = head)
    }
}

/** Sort loop candidates by similarity (desc) and keep the strongest [limit]. */
private fun List<LoopCandidate>?.orEmptySortedTopBySimilarity(limit: Int): List<LoopCandidate> {
    if (this.isNullOrEmpty()) return emptyList()
    return if (size <= limit) sortedBy { it.similarity }       // weak first, strong drawn last
    else sortedByDescending { it.similarity }.take(limit).sortedBy { it.similarity }
}
