package io.lumstudio.yohub.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TooltipText(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit
) {
    OutlinedCard(
        modifier = Modifier,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(width = 1.dp, DividerDefaults.color)
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.labelMedium) {
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                text()
            }
        }
    }
}