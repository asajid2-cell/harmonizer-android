package cc.harmonizerlabs.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cc.harmonizerlabs.app.model.HarmonizerMode
import cc.harmonizerlabs.app.ui.theme.*

@Composable
fun ModeCard(
    mode: HarmonizerMode,
    selected: Boolean,
    onSelect: (HarmonizerMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Web mode-card border is a flat full `#FF00FF` (modern.css .mode-card) — no fade, no glow.
    val border by animateColorAsState(
        NeonMagenta,
        animationSpec = tween(180),
        label = "mode_border",
    )
    val bg by animateColorAsState(
        if (selected) NeonMagenta else Color.Transparent,
        animationSpec = tween(180),
        label = "mode_bg",
    )
    val textColor by animateColorAsState(
        if (selected) Black else NeonMagenta,
        animationSpec = tween(180),
        label = "mode_text",
    )

    Surface(
        shape  = RectangleShape,
        color  = bg,
        border = BorderStroke(2.dp, border),
        modifier = modifier
            .clickable { onSelect(mode) }
            .height(112.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(
                text  = mode.icon,
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text      = mode.displayName,
                style     = MaterialTheme.typography.titleSmall,
                color     = textColor,
                textAlign = TextAlign.Center,
                maxLines  = 1,
            )
            Spacer(Modifier.height(3.dp))
            // Description — web shows a short blurb under each mode card
            Text(
                text      = mode.description,
                style     = MaterialTheme.typography.bodySmall,
                color     = if (selected) Black.copy(alpha = 0.7f) else TextMuted,
                textAlign = TextAlign.Center,
                maxLines  = 2,
            )
            if (mode.isExperimental) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "EXP",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                )
            }
        }
    }
}
