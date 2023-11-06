package io.lumstudio.yohub.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberCursorPositionProvider
import com.konyaco.fluent.FluentTheme
import com.konyaco.fluent.ProvideTextStyle
import com.konyaco.fluent.animation.FluentDuration
import com.konyaco.fluent.animation.FluentEasing
import com.konyaco.fluent.component.AccentButton
import com.konyaco.fluent.component.Button
import com.konyaco.fluent.component.ButtonColor
import com.konyaco.fluent.component.ButtonColors

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Dialog(
    title: String,
    visible: Boolean,
    content: @Composable () -> Unit,
    cancelButtonText: String,
    onCancel: () -> Unit,
    confirmButtonText: String,
    onConfirm: () -> Unit
) {
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(visible) {
        visibleState.targetState = visible
    }

    if (visibleState.currentState || visibleState.targetState) Popup(
        popupPositionProvider = rememberCursorPositionProvider(
            windowMargin = 0.dp,
            alignment = Alignment.Center,
        ),
        focusable = true
    ) {
        Box(
            Modifier.fillMaxSize()
                .background(Color.Black.copy(0.3f))
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {

            val tween = tween<Float>(
                easing = FluentEasing.FastInvokeEasing,
                durationMillis = FluentDuration.QuickDuration
            )

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween) + scaleIn(tween, initialScale = 1.1f),
                exit = fadeOut(tween) + scaleOut(tween, targetScale = 1.1f)
            ) {
                Box(
                    Modifier.width(500.dp).clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Column(Modifier.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .3f))) {
                        Column(Modifier.background(MaterialTheme.colorScheme.background).padding(24.dp).fillMaxWidth()) {
                            Text(
                                style = MaterialTheme.typography.titleLarge,
                                text = title,
                            )
                            Spacer(Modifier.height(12.dp))
                            ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                                content()
                            }
                        }
                        // Divider
                        Box(Modifier.height(1.dp).background(FluentTheme.colors.stroke.surface.default))
                        // Button Grid
                        Box(Modifier.height(80.dp).padding(horizontal = 25.dp), Alignment.Center) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AccentButton(modifier = Modifier.weight(1f), onClick = onConfirm, buttonColors = accentButtonColors()) {
                                    Text(confirmButtonText, color = MaterialTheme.colorScheme.background)
                                }
                                Button(modifier = Modifier.weight(1f), onClick = onCancel) {
                                    Text(cancelButtonText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun accentButtonColors(): ButtonColors {
    val colors = FluentTheme.colors
    val colorScheme = MaterialTheme.colorScheme
    return remember(colors) {
        ButtonColors(
            default = ButtonColor(
                colorScheme.primary,
                colors.text.onAccent.primary,
                SolidColor(colorScheme.primary.copy(alpha = .8f))
            ),
            hovered = ButtonColor(
                colorScheme.primary.copy(alpha = .8f),
                colors.text.onAccent.primary,
                SolidColor(colorScheme.primary.copy(alpha = .6f))
            ),
            pressed = ButtonColor(
                colorScheme.primary.copy(alpha = .7f),
                colors.text.onAccent.secondary,
                SolidColor(colorScheme.primary.copy(alpha = .5f))
            ),
            disabled = ButtonColor(
                colors.fillAccent.disabled,
                colors.text.onAccent.disabled,
                SolidColor(Color.Transparent) // Disabled accent button does not have border
            )
        )
    }
}