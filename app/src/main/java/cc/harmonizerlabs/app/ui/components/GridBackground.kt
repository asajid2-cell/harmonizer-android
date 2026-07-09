package cc.harmonizerlabs.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cc.harmonizerlabs.app.ui.theme.Black

/**
 * Dot-grid "starfield" background — matches the CSS radial-gradient dot pattern on the web
 * (`body` in modern.css / `.desktop` in index.html). `bright = true` reproduces the index
 * starfield (near-full-white dots); the default keeps the dim lab grid.
 */
@Composable
fun GridBackground(modifier: Modifier = Modifier, bright: Boolean = false) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val dot1Color = Color.White.copy(alpha = if (bright) 0.55f else 0.06f)
        val dot2Color = Color.White.copy(alpha = if (bright) 0.28f else 0.03f)
        val step1 = 50.dp.toPx()
        val step2 = 80.dp.toPx()
        val dotR = 1.5f

        // First grid
        var x = 0f
        while (x < size.width + step1) {
            var y = 0f
            while (y < size.height + step1) {
                drawCircle(dot1Color, dotR, Offset(x, y))
                y += step1
            }
            x += step1
        }
        // Second grid — offset by half step
        x = step2 * 0.5f
        while (x < size.width + step2) {
            var y = step2 * 0.5f
            while (y < size.height + step2) {
                drawCircle(dot2Color, dotR, Offset(x, y))
                y += step2
            }
            x += step2
        }
    }
}
