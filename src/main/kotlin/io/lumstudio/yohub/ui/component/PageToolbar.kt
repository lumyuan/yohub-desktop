package io.lumstudio.yohub.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.Toolbar(
    label: String,
    enableAnimate: Boolean = true,
    count: Int = 1,
    style: TextStyle = MaterialTheme.typography.titleLarge,
) {
    val expand = LocalExpand.current
    val offset = if (count > 1) {
        if (expand) 1 else 0
    } else {
        0
    }
    val modifier = Modifier.padding(bottom = 28.dp, start = 48.dp * (count - 1 - offset))
    if (enableAnimate) {
        AnimatedVisibility(!expand) {
            Text(
                label,
                style = style,
                modifier = modifier
            )
        }
    }else {
        Text(
            label,
            style = style,
            modifier = modifier
        )
    }
}