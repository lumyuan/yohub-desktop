package io.lumstudio.yohub.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun FlowButton(
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.height(65.dp).padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(.5.dp, DividerDefaults.color.copy(alpha = .5f))
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) Box(Modifier.padding(start = 16.dp).size(28.dp), Alignment.Center) {
                icon()
            }
            Row(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 16.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content()
            }
        }
    }
}