package cc.harmonizerlabs.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.harmonizerlabs.app.model.HarmonizerMode

/**
 * Simple 2-column static grid for mode cards.
 * Replaces LazyVerticalGrid to avoid nested-scroll conflicts inside scrollable columns.
 */
@Composable
fun ModeGrid(
    modes: List<HarmonizerMode>,
    selected: HarmonizerMode,
    onSelect: (HarmonizerMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        modes.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { mode ->
                    ModeCard(
                        mode     = mode,
                        selected = selected == mode,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Pad the last row if odd number of items
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
