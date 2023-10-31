package io.lumstudio.yohub.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SubtleIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {

    Box(
        modifier = modifier.clip(RoundedCornerShape(4.dp))
            .clickable(
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }

}