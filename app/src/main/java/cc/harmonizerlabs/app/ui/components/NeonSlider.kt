package cc.harmonizerlabs.app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cc.harmonizerlabs.app.ui.theme.*

/**
 * Neon slider — track gradient from cyan to purple, glowing thumb.
 * Matches the .neon-range component in harmonizer.css.
 */
@Composable
fun NeonSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    modifier: Modifier = Modifier,
    valueLabel: String = value.toInt().toString(),
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.weight(1f),
            )
            // Cyan circular value badge
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = NeonCyan,
                modifier = Modifier.size(28.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text  = valueLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Black,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Slider(
            value            = value,
            onValueChange    = onValueChange,
            valueRange       = valueRange,
            steps            = steps,
            interactionSource = remember { MutableInteractionSource() },
            colors = SliderDefaults.colors(
                thumbColor            = Color(0xFFCE5BFF),     // purple thumb
                activeTrackColor      = NeonCyan,
                inactiveTrackColor    = NeonCyan.copy(alpha = 0.2f),
                activeTickColor       = Color.Transparent,
                inactiveTickColor     = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
