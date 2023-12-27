package io.lumstudio.yohub.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.konyaco.fluent.FluentTheme
import com.konyaco.fluent.animation.FluentDuration
import com.konyaco.fluent.animation.FluentEasing
import com.konyaco.fluent.background.Layer
import com.konyaco.fluent.component.NavigationItemSeparator
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.ChevronDown
import com.konyaco.fluent.icons.regular.Navigation
import com.konyaco.fluent.icons.regular.Search
import io.lumstudio.yohub.common.utils.LocalPreferences
import io.lumstudio.yohub.common.utils.PreferencesName
import io.lumstudio.yohub.lang.LocalLanguageType
import io.lumstudio.yohub.model.configurations.NavBarWidth
import io.lumstudio.yohub.windows.LocalWindowMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Cursor

val LocalExpand = compositionLocalOf { false }
private val LocalNavigationLevel = compositionLocalOf { 0 }
private val LocalSelectedItemPosition = compositionLocalOf<MutableTransitionState<Float>?> { null }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SideNav(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandStateChange: (Boolean) -> Unit,
    autoSuggestionBox: (@Composable () -> Unit)? = null,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val preferencesStore = LocalPreferences.current
    val minWidth = 220.dp
    val maxWidth = 320.dp

    val window = LocalWindowMain.current

    val languageType = LocalLanguageType.value

    val widthState = rememberSaveable {
        mutableStateOf(320.dp)
    }

    //加载导航栏宽度
    preferencesStore.preference[PreferencesName.NAV_BAR_WIDTH.toString()]?.also {
        try {
            val gson = Gson()
            val navBarWidth = gson.fromJson(it, NavBarWidth::class.java)
            val dp = navBarWidth.dp.dp
            widthState.value = if (dp < minWidth) minWidth else if (dp > maxWidth) maxWidth else dp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var dragState by remember { mutableStateOf(false) }

    val width by animateDpAsState(
        if (expanded) widthState.value else 48.dp,
        tween(durationMillis = if (dragState) 0 else 400)
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = modifier.width(width)) {
            Spacer(Modifier.height(8.dp))
            val languageBasic = languageType.lang
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp).height(42.dp)
            ) {
                TooltipArea(
                    tooltipPlacement = TooltipPlacement.CursorPoint(
                        offset = DpOffset(50.dp, 0.dp),
                        alignment = Alignment.Center,
                    ),
                    tooltip = {
                        TooltipText {
                            Text(if (expanded) languageBasic.collapseNav else languageBasic.openNav)
                        }
                    }
                ) {
                    SubtleIconButton(
                        modifier = Modifier.padding(vertical = 4.dp).size(38.dp, 34.dp),
                        onClick = { onExpandStateChange(!expanded) },
                    ) {
                        Icon(Icons.Default.Navigation, "Expand")
                    }
                }
            }

            CompositionLocalProvider(
                LocalExpand provides expanded,
                LocalNavigationLevel provides 0
            ) {
                autoSuggestionBox?.let {
                    if (!expanded) {
                        TooltipArea(
                            tooltipPlacement = TooltipPlacement.CursorPoint(
                                offset = DpOffset(50.dp, 0.dp),
                                alignment = Alignment.Center,
                            ),
                            tooltip = {
                                TooltipText {
                                    Text(languageBasic.searchFuns)
                                }
                            }
                        ) {
                            Box(
                                contentAlignment = Alignment.TopStart,
                                modifier = Modifier
                            ) {
                                SideNavItem(
                                    selected = false,
                                    onClick = {
                                        onExpandStateChange(true)
                                    },
                                    icon = {
                                        Icon(Icons.Default.Search, null)
                                    },
                                    content = {}
                                )
                            }
                        }
                    } else {
                        Box(
                            contentAlignment = Alignment.TopStart,
                            modifier = Modifier
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 2.dp)) {
                                it()
                            }
                        }
                    }
                }
                val scrollState = rememberScrollState()
                ScrollbarContainer(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight().verticalScroll(scrollState).padding(
                            bottom = 8.dp
                        )
                    ) {
                        content()
                    }
                }
                footer?.let {
                    // Divider
                    NavigationItemSeparator(modifier = Modifier.padding(bottom = 4.dp))
                    it()
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        val color = DividerDefaults.color
        AnimatedVisibility(expanded) {
            Box(
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                    .fillMaxHeight()
                    .width(8.dp)
                    .drawWithContent {
                        val height = this.size.height
                        this.drawRoundRect(
                            color,
                            topLeft = Offset(1.dp.value, 0f),
                            size = Size(6.dp.value, height - 16.dp.value),
                            cornerRadius = CornerRadius(4.dp.value)
                        )
                        this.drawContent()
                    }
                    .onDrag(
                        enabled = expanded,
                        onDragStart = {
                            dragState = true
                        },
                        onDragEnd = {
                            dragState = false
                        }
                    ) {
                        val offset = (it.x / density.density).dp
                        widthState.value += offset
                        if (widthState.value < minWidth) {
                            widthState.value = minWidth
                        } else if (widthState.value > maxWidth) {
                            widthState.value = maxWidth
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            val gson = Gson()
                            val json = gson.toJson(NavBarWidth(widthState.value.value))
                            preferencesStore.preference[PreferencesName.NAV_BAR_WIDTH.toString()] = json
                            preferencesStore.submit()
                        }
                    }.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (expanded) {
                                    when (event.type) {
                                        PointerEventType.Enter -> {
                                            window.components.first().cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)
                                        }

                                        PointerEventType.Exit -> {
                                            window.components.first().cursor = Cursor.getDefaultCursor()
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {}
        }
    }
}

@Composable
fun SideNavItem(
    selected: Boolean,
    expand: Boolean = LocalExpand.current,
    onClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    expandItems: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
    items: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()

    val color = when {
        selected && hovered -> FluentTheme.colors.subtleFill.tertiary
        selected -> FluentTheme.colors.subtleFill.secondary
        pressed -> FluentTheme.colors.subtleFill.tertiary
        hovered -> FluentTheme.colors.subtleFill.secondary
        else -> FluentTheme.colors.subtleFill.transparent
    }
    var currentPosition by remember {
        mutableStateOf(0f)
    }
    val selectedState = LocalSelectedItemPosition.current
    LaunchedEffect(selected, currentPosition) {
        if (selected) {
            selectedState?.targetState = currentPosition
        }
    }
    Column(
        modifier = modifier.onGloballyPositioned {
            currentPosition = it.positionInRoot().y
        }
    ) {
        Box(Modifier.height(50.dp).fillMaxWidth().padding(4.dp, 2.dp)) {
            val navigationLevelPadding = 28.dp * LocalNavigationLevel.current
            Layer(
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(4.dp),
                color = animateColorAsState(
                    color, tween(FluentDuration.QuickDuration, easing = FluentEasing.FastInvokeEasing)
                ).value,
                contentColor = FluentTheme.colors.text.text.primary,
                outsideBorder = false,
                cornerRadius = 4.dp,
                border = null
            ) {
                Box(
                    Modifier.clickable(
                        onClick = { onClick(!selected) },
                        interactionSource = interaction,
                        indication = null
                    ).padding(start = navigationLevelPadding),
                    Alignment.CenterStart
                ) {
                    if (icon != null) Box(Modifier.padding(start = 12.dp).size(18.dp), Alignment.Center) {
                        icon()
                    }
                    val fraction by animateFloatAsState(
                        targetValue = if (expand) 1f else 0f,
                        tween(FluentDuration.ShortDuration, easing = FluentEasing.FastInvokeEasing)
                    )
                    if (expand) {
                        Row(
                            modifier = Modifier.padding(
                                start = if (icon != null) 44.dp else 16.dp,
                                end = if (items != null) 44.dp else 0.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            content()
                        }
                        if (items != null) {
                            val rotation by animateFloatAsState(
                                if (expandItems) {
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
                                        alpha = if (fraction == 1f) {
                                            1f
                                        } else {
                                            0f
                                        }
                                    }
                            )
                        }
                    }
                }
            }
            Indicator(Modifier.align(Alignment.CenterStart).padding(start = navigationLevelPadding), selected)
        }

        if (items != null) {
            AnimatedVisibility(
                visible = expandItems && expand,
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
                CompositionLocalProvider(
                    LocalNavigationLevel provides LocalNavigationLevel.current + 1
                ) {
                    Column {
                        items()
                    }
                }
            }
        }
    }
}

@Composable
private fun Indicator(modifier: Modifier, display: Boolean) {
    val height by updateTransition(display).animateDp(transitionSpec = {
        if (targetState) tween(FluentDuration.ShortDuration, easing = FluentEasing.FastInvokeEasing)
        else tween(FluentDuration.QuickDuration, easing = FluentEasing.SoftDismissEasing)
    }, targetValueByState = { if (it) 16.dp else 0.dp })
    Box(modifier.size(3.dp, height).background(MaterialTheme.colorScheme.primary))
}