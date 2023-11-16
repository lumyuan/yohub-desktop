package io.lumstudio.yohub.windows

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.konyaco.fluent.component.NavigationItemSeparator
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.Search
import io.appoutlet.karavel.Karavel
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.LocalApplication
import io.lumstudio.yohub.common.LocalIOCoroutine
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.common.utils.BrandLogoUtil
import io.lumstudio.yohub.lang.LanguageType
import io.lumstudio.yohub.lang.LocalLanguageType
import io.lumstudio.yohub.runtime.*
import io.lumstudio.yohub.theme.MicaTheme
import io.lumstudio.yohub.ui.component.LocalExpand
import io.lumstudio.yohub.ui.component.SideNav
import io.lumstudio.yohub.ui.component.SideNavItem
import io.lumstudio.yohub.ui.component.TooltipText
import io.lumstudio.yohub.windows.navigation.NavPage
import io.lumstudio.yohub.windows.navigation.PageNav
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.Dimension
import kotlin.system.exitProcess

val selectPage by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    mutableStateOf<NavPage?>(null)
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainUI() {
    val applicationScope = LocalApplication.current
    val adbStore = LocalAdbRuntime.current
    val keepShellStore = LocalKeepShell.current
    val ioCoroutine = LocalIOCoroutine.current
    val windowState = rememberWindowState(
        position = WindowPosition(Alignment.Center),
        size = DpSize(1000.dp, 750.dp)
    )
    val lang = LocalLanguageType.value.lang
    Window(
        title = lang.appName,
        icon = painterResource(R.icon.logoRound),
        onCloseRequest = {
            ioCoroutine.ioScope.launch {
                keepShellStore cmd adbStore.adbDevice(null, "kill-server")
            }
            applicationScope.exitApplication()
            exitProcess(0)
        },
        state = windowState,
    ) {
        MicaTheme {
            window.minimumSize = Dimension(800, 600)
            val navs = PageNav.values().toList().filter { it.page.isNavigation }
            val karavel by remember { mutableStateOf(Karavel(navs.first().page)) }
            Row(modifier = Modifier.fillMaxSize()) {
                var expanded by remember { mutableStateOf(true) }
                SideNav(
                    modifier = Modifier.fillMaxHeight(),
                    expanded = expanded,
                    onExpandStateChange = { expanded = it },
                    autoSuggestionBox = {
                        val searchFuns = remember { mutableStateOf("") }
                        var dropdownMenuState by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = searchFuns.value,
                                onValueChange = {
                                    if (it.trim().isEmpty()) {
                                        dropdownMenuState = false
                                        searchFuns.value = ""
                                    }else {
                                        searchFuns.value = it
                                        dropdownMenuState = true
                                    }
                                },
                                label = {
                                    Text(lang.searchFunctions, softWrap = false)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, null)
                                },
                                trailingIcon = {
                                    if (searchFuns.value.trim().isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    dropdownMenuState = false
                                                    delay(100)
                                                    searchFuns.value = ""
                                                }
                                            }
                                        ) {
                                            Icon(androidx.compose.material.icons.Icons.Default.Close, null)
                                        }
                                    }
                                },
                                singleLine = true
                            )
                            DropdownMenu(
                                dropdownMenuState,
                                onDismissRequest = {
                                    dropdownMenuState = false
                                },
                                focusable = false
                            ) {
                                PageNav.values().toList()
                                    .sortedBy { it.page.label() }
                                    .filter { it.page.label().lowercase().contains(searchFuns.value.trim().lowercase()) }
                                    .onEach {
                                        DropdownMenuItem(
                                            modifier = Modifier.width(280.dp),
                                            leadingIcon = {
                                                Box(
                                                    modifier = Modifier.size(16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    it.page.icon().invoke()
                                                }
                                            },
                                            text = {
                                                Text(it.page.label(), style = MaterialTheme.typography.labelMedium)
                                            },
                                            onClick = {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    dropdownMenuState = false
                                                    selectPage.value = it.page.parent
                                                    karavel.navigate(it.page)
                                                    delay(100)
                                                    searchFuns.value = ""
                                                    selectPage.value = it.page
                                                }
                                            }
                                        )
                                    }
                            }
                        }
                    },
                    footer = {
                        NavigationItem(karavel, PageNav.Settings.page, false, selectPage = selectPage)
                    }
                ) {
                    DevicesItem()
                    navs.forEach { navItem ->
                        NavigationItem(karavel, navItem.page, selectPage = selectPage)
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
    val lang = LocalLanguageType.value.lang
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
                    Text(lang.tooltipTextDevice)
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
            icon = { Icon(androidx.compose.material.icons.Icons.Default.TabletAndroid, null) },
            content = { Text(lang.tooltipTextDevice, style = MaterialTheme.typography.labelLarge, softWrap = false) },
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
                                lang.unlinkDevice,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                lang.refreshDeviceList,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    } else {
                        NavigationItemSeparator(modifier = Modifier.padding(bottom = 4.dp))
                        it.forEach { device ->
                            DeviceItem(device)
                        }
                        Column(
                            modifier = modifier,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                lang.refreshDeviceList,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        )
    }
}

val DeviceName by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    mutableStateOf("")
}

val DeviceBrand by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    mutableStateOf("")
}

@Composable
private fun DeviceItem(
    navItem: Device
) {
    val deviceStore = LocalDevice.current
    val keepShellStore = LocalKeepShell.current
    val adbStore = LocalAdbRuntime.current
    val lang = LocalLanguageType.value.lang

    var brand by remember { mutableStateOf(lang.checkIndexDevice) }
    var label by remember { mutableStateOf(lang.checkIndexDevice) }
    var sub by remember { mutableStateOf(lang.checkIndexDevice) }

    LaunchedEffect(label, lang) {
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
                                "shell getprop ro.product.model"
                            )).replace("\n", "")
                            val name = if (model.contains("inaccessible or not found")) {
                                navItem.id
                            } else {
                                model.replace("$brand ", "")
                            }
                            label = (marketName.ifEmpty { "$brand $name" })
                            sub = String.format(lang.deviceType, navItem.type)
                        }

                        ClientState.UNAUTHORIZED -> {
                            label = String.format(lang.deviceTypeUnAuthorization, navItem.id)
                            sub = String.format(lang.deviceType, navItem.type)
                        }

                        ClientState.FASTBOOT -> {}
                    }
                }

                ClientType.FASTBOOT -> {
                    label = navItem.id
                    sub = String.format(lang.deviceType, navItem.type)
                }

                ClientType.UNKNOWN -> {
                    label = lang.unknownDevice
                    sub = String.format(lang.deviceType, lang.unknown)
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
            DeviceName.value = label
            DeviceBrand.value = brand
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
    hasItems: Boolean = true,
    selectPage: MutableState<NavPage?>
) {
    val expandedItems = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(selectPage) {
        snapshotFlow { selectPage.value }
            .onEach {
                if (it?.label() == navItem.label()) expandedItems.value = true
            }.launchIn(this)
    }

    TooltipArea(
        tooltipPlacement = TooltipPlacement.CursorPoint(
            offset = DpOffset(50.dp, 0.dp),
            alignment = Alignment.Center,
        ),
        tooltip = {
            if (!LocalExpand.current) {
                TooltipText {
                    Text(navItem.label())
                }
            }
        }
    ) {
        SideNavItem(
            (karavel.currentPage() as NavPage).label() == navItem.label(),
            onClick = {
                selectPage.value = navItem
                if (karavel.currentPage() == navItem) {
                    expandedItems.value = !expandedItems.value
                } else {
                    expandedItems.value = true
                }
                navItem.karavel?.navigate(navItem)
                karavel.navigate(navItem)
            },
            icon = navItem.icon(),
            content = { Text(navItem.label(), style = MaterialTheme.typography.labelLarge, softWrap = false) },
            expandItems = expandedItems.value,
            items = if (hasItems) {
                navItem.nestedItems?.let {
                    {
                        it.forEach { nestedItem ->
                            NavigationItem(
                                karavel, nestedItem, selectPage = selectPage
                            )
                        }
                    }
                }
            } else null
        )
    }
}