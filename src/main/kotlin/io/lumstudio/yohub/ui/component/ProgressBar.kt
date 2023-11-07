package io.lumstudio.yohub.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    color: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 4.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
) {
    val progressWidthAnimation = remember {
        Animatable(initialValue = 0f)
    }

    val realProgress = if (progress <= .5f) .5f else progress

    LaunchedEffect(realProgress){
        if (realProgress in 0f .. 100f){
            progressWidthAnimation.animateTo(
                targetValue = realProgress, animationSpec = tween(
                    durationMillis = 750, easing = FastOutSlowInEasing
                )
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        modifier = modifier
            .height(cornerRadius * 2)
            .clip(RoundedCornerShape(cornerRadius)) // 裁剪矩形区域为圆角矩形，将超出圆角矩形的部分绘制去掉
            .drawWithContent {
                drawContent() // 先绘制内容后绘制自定义图形，这样我们绘制的图形将显示在内容区域上方
                val progressWidth = drawContext.size.width * progressWidthAnimation.value / 100f
                drawRoundRect(
                    color = color,
                    size = drawContext.size.copy(width = progressWidth),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            },
        content = {}
    )
}
