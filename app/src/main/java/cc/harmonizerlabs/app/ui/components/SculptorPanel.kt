package cc.harmonizerlabs.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cc.harmonizerlabs.app.api.models.Section
import cc.harmonizerlabs.app.ui.theme.*

private val palette = listOf(
    Color(0xFF2E6BFF), Color(0xFFB14CFF), Color(0xFF18C7B0), Color(0xFFFF5C8A),
    Color(0xFF6E7BFF), Color(0xFFFFA94C), Color(0xFF4CE0FF), Color(0xFFFF7AD1),
)

private fun secColor(i: Int) = palette[i % palette.size]

private fun fmtDur(sec: Double): String {
    val s = sec.toInt()
    return if (s >= 60) "%d:%02d".format(s / 60, s % 60) else "${s}s"
}

/**
 * Section Sculptor arrangement UI — the web's sculptor-controls, adapted for touch:
 * tap a section in the palette to append it to the timeline, tap a timeline clip to remove it,
 * plus Reset / Clear / Shuffle. The timeline order drives [SculptorEngine] playback.
 */
@Composable
fun SculptorPanel(
    sections: List<Section>,
    arrangement: List<Int>,
    onAdd: (Int) -> Unit,
    onRemoveAt: (Int) -> Unit,
    onReset: () -> Unit,
    onClear: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .neonGlow(NeonCyan, glowRadius = 6.dp, intensity = 0.22f)
            .border(1.dp, NeonCyan.copy(alpha = 0.25f))
            .background(SurfaceDark)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("SECTION TIMELINE", style = MaterialTheme.typography.labelSmall, color = NeonCyan, modifier = Modifier.weight(1f))
            SmallGhost("RESET", onReset)
            Spacer(Modifier.width(6.dp))
            SmallGhost("SHUFFLE", onShuffle)
            Spacer(Modifier.width(6.dp))
            SmallGhost("CLEAR", onClear)
        }
        Spacer(Modifier.height(8.dp))

        // ── Timeline ─────────────────────────────────────────────────────────────
        if (arrangement.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, NeonCyan.copy(alpha = 0.2f)),
            ) {
                Text("Tap sections below to build your arrangement…",
                    style = MaterialTheme.typography.bodySmall, color = TextMuted, textAlign = TextAlign.Center)
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(arrangement) { pos, secIdx ->
                    SectionClip(
                        index    = secIdx,
                        dur      = sections.getOrNull(secIdx)?.duration ?: 0.0,
                        filled   = true,
                        trailing = "✕",
                        onClick  = { onRemoveAt(pos) },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("AVAILABLE SECTIONS", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
        Spacer(Modifier.height(6.dp))

        // ── Palette ──────────────────────────────────────────────────────────────
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(sections) { idx, _ ->
                SectionClip(
                    index    = idx,
                    dur      = sections[idx].duration,
                    filled   = false,
                    trailing = "+",
                    onClick  = { onAdd(idx) },
                )
            }
        }
    }
}

@Composable
private fun SectionClip(index: Int, dur: Double, filled: Boolean, trailing: String, onClick: () -> Unit) {
    val c = secColor(index)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(1.5.dp, c)
            .background(if (filled) c.copy(alpha = 0.28f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("S${index + 1}", style = MaterialTheme.typography.labelLarge, color = c)
            Spacer(Modifier.width(6.dp))
            Text(trailing, style = MaterialTheme.typography.labelSmall, color = c.copy(alpha = 0.8f))
        }
        Text(fmtDur(dur), style = MaterialTheme.typography.bodySmall, color = TextMuted)
    }
}

@Composable
private fun SmallGhost(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = NeonCyan,
        modifier = Modifier
            .border(BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)), RectangleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
    )
}
