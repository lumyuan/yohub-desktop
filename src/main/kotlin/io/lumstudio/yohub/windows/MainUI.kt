package io.lumstudio.yohub.windows

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.konyaco.fluent.component.NavigationItemSeparator
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.*
import io.appoutlet.karavel.Karavel
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.LocalApplication
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.common.utils.BrandLogoUtil
import io.lumstudio.yohub.runtime.*
import io.lumstudio.yohub.theme.MicaTheme
import io.lumstudio.yohub.ui.component.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Dimension
import kotlin.system.exitProcess

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainUI() {
    val applicationScope = LocalApplication.current
    val adbStore = LocalAdbRuntime.current
    val keepShellStore = LocalKeepShell.current
    val windowState = rememberWindowState(
        position = WindowPosition(Alignment.Center),
        size = DpSize(1000.dp, 750.dp)
    )
    Window(
        title = "优画工具箱桌面版",
        icon = painterResource(R.icon.logoRound),
        onCloseRequest = {
            CoroutineScope(Dispatchers.IO).launch {
                keepShellStore cmd adbStore.adbDevice(null, "kill-server")
                applicationScope.exitApplication()
                exitProcess(0)
            }
        },
        state = windowState,
    ) {
        window.minimumSize = Dimension(800, 600)
        val navs = PageNav.values().toList().filter { it != PageNav.Settings }
        val karavel by remember { mutableStateOf(Karavel(navs.first().page)) }
        MicaTheme {
            Row(modifier = Modifier.fillMaxSize()) {
                var expanded by remember { mutableStateOf(true) }
                SideNav(
                    modifier = Modifier.fillMaxHeight(),
                    expanded = expanded,
                    onExpandStateChange = { expanded = it },
                    footer = {
                        NavigationItem(karavel, PageNav.Settings.page, false)
                    }
                ) {
                    DevicesItem()
                    navs.forEach { navItem ->
                        NavigationItem(karavel, navItem.page)
                    }
                }
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
                        border = BorderStroke(.5.dp, DividerDefaults.color)
                    ) {
                        DeviceScreen()
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    OutlinedCard(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.elevatedCardColors(),
                        border = BorderStroke(.5.dp, DividerDefaults.color)
                    ) {
                        CompositionLocalProvider(
                            LocalExpand provides expanded,
                        ) {
                            AnimatedContent(
                                karavel.currentPage(),
                                Modifier.fillMaxHeight().weight(1f),
                                transitionSpec = {
                                    fadeIn(tween()) +
                                            slideInVertically(tween()) { it / 6 } with
                                            fadeOut(tween())
                                }) {
                                it.content()
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DevicesItem() {
    val devicesStore = LocalDevices.current
    val expandedItems = remember {
        mutableStateOf(true)
    }
    TooltipArea(
        tooltipPlacement = TooltipPlacement.CursorPoint(
            offset = DpOffset(50.dp, 0.dp),
            alignment = Alignment.Center,
        ),
        tooltip = {
            if (!LocalExpand.current) {
                TooltipText {
                    Text("设备")
                }
            }
        }
    ) {
        val keepShellStore = LocalKeepShell.current
        val adbRuntimeStore = LocalAdbRuntime.current
        val deviceStore = LocalDevice.current
        val driverStore = LocalDriver.current
        SideNavItem(
            false,
            onClick = {
                expandedItems.value = !expandedItems.value
            },
            icon = { Icon(androidx.compose.material.icons.Icons.Default.TabletAndroid, "设备") },
            content = { Text("设备", style = MaterialTheme.typography.labelLarge, softWrap = false) },
            expandItems = expandedItems.value,
            items = devicesStore.devices.let {
                {
                    var enabled by remember { mutableStateOf(true) }
                    val modifier = Modifier.fillMaxWidth().height(50.dp).padding(start = 4.dp, end = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            enabled = enabled
                        ) {
                            enabled = false
                            loadAndroidDevices(
                                delay = 0,
                                keepShellStore = keepShellStore,
                                adbRuntimeStore = adbRuntimeStore,
                                deviceStore = deviceStore,
                                devicesStore = devicesStore,
                                driverStore = driverStore
                            ) {
                                enabled = true
                            }
                        }
                    if (it.isEmpty()) {
                        Column(
                            modifier = modifier,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "未连接设备",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                "点击刷新设备列表",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    } else {
                        NavigationItemSeparator(modifier = Modifier.padding(bottom = 4.dp))
                        it.forEach { device ->
                            DeviceItem(
                                device
                            )
                        }
                        Column(
                            modifier = modifier,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "点击刷新设备列表",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun DeviceItem(
    navItem: Device
) {
    val deviceStore = LocalDevice.current
    val keepShellStore = LocalKeepShell.current
    val adbStore = LocalAdbRuntime.current

    var brand by remember { mutableStateOf("检测中...") }
    var label by remember { mutableStateOf("检测中...") }
    var sub by remember { mutableStateOf("检测中...") }

    LaunchedEffect(label) {
        withContext(Dispatchers.IO) {
            when (navItem.type) {
                ClientType.ADB, ClientType.ADB_AB, ClientType.ADB_VAB -> {
                    when (navItem.state) {
                        ClientState.DEVICE, ClientState.RECOVERY -> {
                            brand = (keepShellStore cmd adbStore.adbDevice(
                                navItem.id,
                                "shell getprop ro.product.brand"
                            )).replace("\n", "")

                            val marketName =
                                (keepShellStore cmd adbStore.adbDevice(
                                    navItem.id,
                                    "shell getprop ro.product.marketname"
                                )).replace("\n", "")
                            val model = (keepShellStore cmd adbStore.adbDevice(
                                navItem.id,
                                "shell ro.product.model"
                            )).replace("\n", "")

                            val name = if (model.contains("inaccessible or not found")) {
                                navItem.id
                            } else {
                                model
                            }
                            label = (marketName.ifEmpty { "$brand $name" })
                            sub = "设备类型：${navItem.type}"
                        }

                        ClientState.UNAUTHORIZED -> {
                            label = "${navItem.id}（未授权）"
                            sub = "设备类型：${navItem.type}"
                        }

                        ClientState.FASTBOOT -> {}
                    }
                }

                ClientType.FASTBOOT -> {
                    label = navItem.id
                    sub = "设备类型：${navItem.type}"
                }

                ClientType.UNKNOWN -> {
                    label = "未知设备"
                    sub = "设备类型：未知"
                }
            }
        }
    }

    SideNavItem(
        deviceStore.device == navItem,
        onClick = {
            if (deviceStore.device == navItem) {
                deviceStore.device = null
            } else {
                deviceStore.device = navItem
            }
        },
        icon = {
            Image(BrandLogoUtil.getLogoPainterByBrand(brand.replace("\n", "")), navItem.id)
        },
        content = {
            Column {
                Text(label, style = MaterialTheme.typography.labelLarge, softWrap = false)
                Text(sub, style = MaterialTheme.typography.labelSmall, softWrap = false)
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NavigationItem(
    karavel: Karavel,
    navItem: NavPage,
    hasItems: Boolean = true
) {
    TooltipArea(
        tooltipPlacement = TooltipPlacement.CursorPoint(
            offset = DpOffset(50.dp, 0.dp),
            alignment = Alignment.Center,
        ),
        tooltip = {
            if (!LocalExpand.current) {
                TooltipText {
                    Text(navItem.label)
                }
            }
        }
    ) {
        SideNavItem(
            karavel.currentPage() == navItem,
            onClick = {
                navItem.karavel?.navigate(navItem)
                karavel.navigate(navItem)
            },
            icon = navItem.icon?.let { { Icon(it, navItem.label) } },
            content = { Text(navItem.label, style = MaterialTheme.typography.labelLarge, softWrap = false) },
            expandItems = karavel.currentPage() == navItem,
            items = if (hasItems) {
                navItem.nestedItems?.let {
                    {
                        it.forEach { nestedItem ->
                            NavigationItem(
                                karavel, nestedItem
                            )
                        }
                    }
                }
            }else null
        )
    }
}