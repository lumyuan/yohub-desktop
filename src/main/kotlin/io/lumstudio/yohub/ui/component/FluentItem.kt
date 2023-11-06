package io.lumstudio.yohub.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.animation.FluentDuration
import com.konyaco.fluent.animation.FluentEasing
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.ChevronDown

@Composable
fun FluentItem(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    leadingIcon: (@Composable RowScope.() -> Unit)? = null
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(.5.dp, DividerDefaults.color)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(70.dp)
                .padding(12.dp),
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

@Composable
fun FluentItem(
    icon: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    leadingIcon: (@Composable RowScope.() -> Unit)? = null
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(.5.dp, DividerDefaults.color)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(70.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(modifier = Modifier.size(24.dp)) { icon() }
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

@Composable
fun FluentFold(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    content: (@Composable RowScope.() -> Unit)? = null,
    foldContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    var expand by remember { mutableStateOf(true) }
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(.5.dp, DividerDefaults.color)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(0.dp))
                    .clickable {
                        expand = !expand
                    }.padding(12.dp),
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
                    content?.let {
                        it()
                        Spacer(modifier = Modifier.size(28.dp))
                    }
                    Box {
                        val rotation by animateFloatAsState(
                            if (expand) {
                                180f
                            } else {
                                00f
                            }
                        )
                        Icon(
                            Icons.Default.ChevronDown,
                            null,
                            modifier = Modifier.width(36.dp)
                                .align(Alignment.CenterEnd)
                                .wrapContentWidth(Alignment.CenterHorizontally).size(12.dp)
                                .graphicsLayer {
                                    rotationZ = rotation
                                    alpha = 1f
                                }
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = expand,
                enter = fadeIn(
                    animationSpec = tween(FluentDuration.ShortDuration, easing = FluentEasing.FastInvokeEasing)
                ) + expandVertically(
                    animationSpec = tween(FluentDuration.ShortDuration, easing = FluentEasing.FastInvokeEasing)
                ),
                exit = fadeOut(
                    animationSpec = tween(FluentDuration.ShortDuration, easing = FluentEasing.FastInvokeEasing)
                ) + shrinkVertically(
                    animationSpec = tween(FluentDuration.ShortDuration, easing = FluentEasing.FastInvokeEasing)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    foldContent?.invoke(this)
                }
            }
        }
    }
}