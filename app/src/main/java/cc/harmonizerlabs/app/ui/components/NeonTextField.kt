package cc.harmonizerlabs.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.harmonizerlabs.app.ui.theme.*

/** Input field: black bg, 2dp cyan border, orange focus ring — matches the CSS input style */
@Composable
fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value          = value,
        onValueChange  = onValueChange,
        placeholder    = {
            Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        },
        singleLine     = singleLine,
        shape          = RectangleShape,
        colors         = OutlinedTextFieldDefaults.colors(
            focusedBorderColor    = NeonOrange,
            unfocusedBorderColor  = NeonCyan,
            cursorColor           = NeonCyan,
            focusedTextColor      = TextPrimary,
            unfocusedTextColor    = TextPrimary,
            focusedContainerColor = Black,
            unfocusedContainerColor = Black,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier  = modifier.fillMaxWidth(),
    )
}
