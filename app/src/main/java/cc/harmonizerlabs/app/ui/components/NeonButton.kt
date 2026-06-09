package cc.harmonizerlabs.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.harmonizerlabs.app.ui.theme.*

/**
 * Sharp-cornered neon button matching the Harmonizer web design.
 * When [active] is true the border color fills the background (toggle style).
 */
@Composable
fun NeonButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = NeonLime,
    active: Boolean = false,
    enabled: Boolean = true,
    borderWidth: Dp = 2.dp,
) {
    val bg by animateColorAsState(
        if (active) borderColor else Color.Transparent,
        animationSpec = tween(150),
        label = "btn_bg",
    )
    val textColor by animateColorAsState(
        if (active) Black else borderColor,
        animationSpec = tween(150),
        label = "btn_text",
    )

    OutlinedButton(
        onClick    = onClick,
        enabled    = enabled,
        shape      = RectangleShape,
        border     = BorderStroke(borderWidth, if (enabled) borderColor else borderColor.copy(alpha = 0.35f)),
        colors     = ButtonDefaults.outlinedButtonColors(
            containerColor         = bg,
            contentColor           = textColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor   = borderColor.copy(alpha = 0.35f),
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier   = modifier,
    ) {
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color     = textColor,
        )
    }
}

/** Full-width CTA — lime border, solid fill */
@Composable
fun NeonCtaButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RectangleShape,
        colors   = ButtonDefaults.buttonColors(
            containerColor         = NeonLime,
            contentColor           = Black,
            disabledContainerColor = NeonLime.copy(alpha = 0.25f),
            disabledContentColor   = Black.copy(alpha = 0.4f),
        ),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) Black else Black.copy(alpha = 0.5f),
        )
    }
}
