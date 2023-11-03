package io.lumstudio.yohub.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import java.awt.event.MouseEvent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.clickable(
    right: () -> Unit = {},
    left: () -> Unit = {}
): Modifier = this.onPointerEvent(
    eventType = PointerEventType.Release
) {
    if ((it.nativeEvent as MouseEvent).button == 1) {
        left()
    }else if ((it.nativeEvent as MouseEvent).button == 3) {
        right()
    }
}