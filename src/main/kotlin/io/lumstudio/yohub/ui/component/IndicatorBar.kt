package io.lumstudio.yohub.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun PreviewIndicator() {
    IndicatorBar(foregroundSweepAngle = 50f)
}

/**
 * 弧形进度条
 * @author lumyuan
 */
@Composable
fun IndicatorBar(
    modifier: Modifier = Modifier,
    componentSize: Dp = 300.dp,
    maxIndicatorNum: Float = 100f,
    backgroundIndicatorColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    backgroundIndicatorStrokeWidth: Dp = 8.dp,
    foregroundSweepAngle: Float,
    foregroundColor: Color = MaterialTheme.colorScheme.primary,
    text: @Composable (ColumnScope) -> Unit = {}
) {

    val targetIndicatorValue = remember {
        Animatable(initialValue = 0f)
    }

    val sweepAngle = if (foregroundSweepAngle <= .5f) .5f else foregroundSweepAngle

    val legalSweepAngle = sweepAngle in 0f..maxIndicatorNum

    LaunchedEffect(sweepAngle){
        if (legalSweepAngle) {
            targetIndicatorValue.animateTo(
                targetValue = sweepAngle * 2.4f, animationSpec = tween(
                    durationMillis = 750, easing = FastOutSlowInEasing
                )
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .size(componentSize)
            .drawBehind {
                val indicatorComponentSize = size * 0.8f
                drawBackgroundIndicator(
                    componentSize = indicatorComponentSize,
                    stroke = backgroundIndicatorStrokeWidth.toPx(),
                    color = backgroundIndicatorColor
                )
                drawForegroundIndicator(
                    componentSize = indicatorComponentSize,
                    stroke = backgroundIndicatorStrokeWidth.toPx(),
                    sweepAngle = targetIndicatorValue.value,
                    color = foregroundColor
                )

            }
    ) {
        text(this)
    }
}

fun DrawScope.drawBackgroundIndicator(componentSize: Size, stroke: Float, color: Color) {
    drawArc(
        size = componentSize,
        color = color,
        startAngle = 150f,
        sweepAngle = 240f,
        useCenter = false,
        style = Stroke(cap = StrokeCap.Round, width = stroke),
        topLeft = Offset(
            x = ((size.width - componentSize.width) / 2),
            y = ((size.height - componentSize.height) / 2)
        )
    )
}


fun DrawScope.drawForegroundIndicator(
    componentSize: Size,
    stroke: Float,
    sweepAngle: Float,
    color: Color
) {

    drawArc(
        size = componentSize,
        color = color,
        startAngle = 150f,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(cap = StrokeCap.Round, width = stroke),
        topLeft = Offset(
            x = ((size.width - componentSize.width) / 2),
            y = ((size.height - componentSize.height) / 2)
        )
    )
}