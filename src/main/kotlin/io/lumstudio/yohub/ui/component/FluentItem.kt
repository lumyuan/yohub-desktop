package io.lumstudio.yohub.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FluentItem(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    leadingIcon: (@Composable RowScope.() -> Unit)? = null
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(.5.dp, DividerDefaults.color)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.size(16.dp))
            }
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Text(title, style = MaterialTheme.typography.bodyLarge, softWrap = false)
                subtitle?.also {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        softWrap = false,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f)
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.invoke(this)
            }
        }
    }
}