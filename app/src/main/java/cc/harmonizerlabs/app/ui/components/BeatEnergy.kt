package cc.harmonizerlabs.app.ui.components

import cc.harmonizerlabs.app.api.models.Beat
import cc.harmonizerlabs.app.api.models.Segment

/**
 * Per-beat energy (0..1) derived from segment loudness. The server's beats carry no loudness,
 * but its segments carry `loudness_max` (dB) — so map each beat to the segment it falls in and
 * normalise. Beats and segments are both time-ordered, so a single monotone sweep does it.
 *
 * Without segments (degenerate), returns a flat mid-energy so callers still render sensibly.
 */
fun beatEnergies(beats: List<Beat>, segments: List<Segment>): FloatArray {
    if (beats.isEmpty()) return FloatArray(0)
    if (segments.isEmpty()) return FloatArray(beats.size) { 0.6f }
    val out = FloatArray(beats.size)
    var s = 0
    for (i in beats.indices) {
        val t = beats[i].start
        while (s + 1 < segments.size && segments[s + 1].start <= t) s++
        // loudness_max is dB, typically ~-35..0 for music; map that span to 0..1 for visible contrast.
        val loud = segments[s].loudnessMax
        out[i] = (((loud + 35.0) / 35.0).toFloat()).coerceIn(0f, 1f)
    }
    return out
}
