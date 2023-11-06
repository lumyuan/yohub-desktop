package io.lumstudio.yohub.windows

import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.konyaco.fluent.component.NavigationItemSeparator
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.*
import io.lumstudio.yohub.common.LocalContext
import io.lumstudio.yohub.common.LocalIOCoroutine
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.utils.*
import io.lumstudio.yohub.runtime.LocalInstallThemesPath
import io.lumstudio.yohub.theme.*
import io.lumstudio.yohub.ui.component.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.swing.JFrame

@Composable
fun SettingsScreen(settingsPage: SettingsPage) {
    val scrollState = rememberScrollState()
    ScrollbarContainer(
        adapter = rememberScrollbarAdapter(scrollState),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(scrollState).padding(16.dp)
        ) {
            settingsPage.nestedItems?.onEach {
                it.content()
            }
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Copyright @ 2023 优檀云网络科技 All Rights Reserved", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

class ThemeSetting : NavPage("主题设置", isNavigation = false) {

    override fun icon(): () -> Unit = {  }

    private val gson by lazy { Gson() }

    @OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun content() {
        val themeStore = LocalTheme.current
        val preferencesStore = LocalPreferences.current
        val ioCoroutine = LocalIOCoroutine.current
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Toolbar(label, enableAnimate = false)
            FluentItem(
                Icons.Default.DarkTheme,
                "深色模式"
            ) {
                DarkTheme.values().onEach {
                    Row(
                        modifier = Modifier.padding(start = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                themeStore.theme = it
                                preferencesStore.preference[PreferencesName.DARK_MODEL.toString()] = gson.toJson(it)
                                ioCoroutine.ioScope.launch {
                                    preferencesStore.submit()
                                }
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeStore.theme == it,
                                onClick = null,
                                enabled = false
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(it.annotation)
                        }
                    }
                }
            }

            val colorThemeItems = remember { mutableStateListOf<ColorLoader.ColorThemeItem>() }
            val selectTheme = remember { mutableStateOf("") }
            selectTheme.value = preferencesStore.preference[PreferencesName.COLOR_THEME.toString()] ?: "YoHub Color"
            val targetThemeFileName = remember { mutableStateOf("") }
            val targetThemeName = remember { mutableStateOf("") }
            val installThemesPathStore = LocalInstallThemesPath.current
            val colorThemeStore = LocalColorTheme.current
            val uninstallState = remember { mutableStateOf(false) }
            val generateState = remember { mutableStateOf(false) }
            val helpState = remember { mutableStateOf(false) }
            val colorLoader by remember {
                mutableStateOf(
                    ColorLoader(
                        preferencesStore,
                        installThemesPathStore,
                        colorThemeStore
                    )
                )
            }

            LaunchedEffect(Unit) {
                colorThemeItems.clear()
                colorThemeItems.addAll(colorLoader.loadInstalledColorThemes())
            }

            FluentFold(
                Icons.Default.Color,
                "主题颜色",
                content = {
                    TextButton(
                        onClick = {
                            generateState.value = true
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("制作主题")
                    }
                }
            ) {
                NavigationItemSeparator(modifier = Modifier.padding(bottom = 4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    ColorThemeItemDefault(colorThemeStore, selectTheme, preferencesStore)
                    colorThemeItems.onEach {
                        ColorThemeItem(
                            colorThemeStore,
                            preferencesStore,
                            selectTheme,
                            it,
                            uninstallState,
                            targetThemeFileName,
                            targetThemeName
                        )
                    }
                    ColorThemeItemInstall(colorLoader, colorThemeItems)
                }
            }

            Dialog(
                title = "提示",
                visible = uninstallState.value,
                cancelButtonText = "取消",
                confirmButtonText = "确定",
                onCancel = {
                    uninstallState.value = false
                },
                onConfirm = {
                    uninstallState.value = false
                    if (targetThemeFileName.value == selectTheme.value) {
                        sendNotice("卸载失败！", "无法卸载当前已在使用的主题")
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            colorLoader.uninstallColorTheme(targetThemeFileName.value, targetThemeName.value)
                            colorThemeItems.clear()
                            colorThemeItems.addAll(colorLoader.loadInstalledColorThemes())
                        }
                    }
                },
                content = {
                    Text("是否卸载主题【${targetThemeName.value}】\n文件名：${targetThemeFileName.value}?")
                }
            )

            val themeName = remember { mutableStateOf("") }
            val colorPath = remember { mutableStateOf("") }
            Dialog(
                title = "主题制作",
                visible = generateState.value,
                cancelButtonText = "取消",
                confirmButtonText = "生成主题文件",
                onCancel = {
                    generateState.value = false
                },
                onConfirm = {
                    generateState.value = false
                    if (colorPath.value.trim().isEmpty()) {
                        sendNotice("解析失败！", "请输入Color文件路径")
                        return@Dialog
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        val colorFile = File(colorPath.value)
                        if (colorFile.exists()) {
                            val light = HashMap<String, String>()
                            val dark = HashMap<String, String>()
                            val kt = String(readBytes(FileInputStream(colorFile)))
                            val list = kt.split("\n").toList().filter { it.contains("val md_theme_") }
                            if (list.isEmpty()) {
                                sendNotice("主题解析失败！", "不是有效的Color文件")
                            } else if (themeName.value.trim().isEmpty()) {
                                sendNotice("主题创建失败！", "主题名称不能为空！")
                            } else {
                                list.onEach {
                                    when {
                                        it.contains("light") -> {
                                            light[it.name()] = it.argb()
                                        }
                                        it.contains("dark") -> {
                                            dark[it.name()] = it.argb()
                                        }
                                    }
                                }
                                try {
                                    val lightColor = light.toColorTheme()
                                    val darkColor = dark.toColorTheme()
                                    val customColorTheme = CustomColorTheme(name = themeName.value, light = lightColor, dark = darkColor)
                                    val json = gson.toJson(customColorTheme)
                                    val fileDialog = FileDialog(JFrame())
                                    fileDialog.file = "${themeName.value}.json"
                                    fileDialog.mode = FileDialog.SAVE
                                    fileDialog.isVisible = true
                                    val path = fileDialog.directory + fileDialog.file
                                    writeBytes(FileOutputStream(path), json.toByteArray())
                                    sendNotice("主题生成成功！", "已将您的主题【${themeName.value}】保存至${fileDialog.directory}目录下")
                                    themeName.value = ""
                                    colorPath.value = ""
                                }catch (e: Exception) {
                                    e.printStackTrace()
                                    sendNotice("主题解析错误！", "${e.message}")
                                }
                            }
                        }else {
                            sendNotice("主题生成失败！", "目标文件【${colorPath.value}】不存在")
                        }
                    }
                },
                content = {
                    GenerateTheme(helpState, themeName, colorPath)
                }
            )

            val contextStore = LocalContext.current
            Dialog(
                title = "帮助",
                visible = helpState.value,
                cancelButtonText = "知道啦",
                confirmButtonText = "获取Colors文件",
                onCancel = {
                    helpState.value = false
                },
                onConfirm = {
                    helpState.value = false
                    contextStore.startBrowse("https://m3.material.io/theme-builder")
                },
                content = {
                    Text("本软件主题配色架构采用Material Design 3（MD3）设计规范进行设计，为了方便主题设计者创建主题配置文件，本软件将直接解析由【Material Theme Builder】生成的“Color.kt”文件，并自动转换成主题配置文件。\n\n获取Color.kt：点击下方获取按钮，然后在网站中设计你的主题配色，最后导出为“Jetpack Compose”，最后将下载的文件解压到目录并使用本软件导入Color.kt文件即可。\n\n小贴士：在【Figma】中使用“Material Theme Builder”插件设计配色刚方便哦~")
                }
            )
        }
    }

    private fun String.name(): String = this.substring(this.lastIndexOf("_") + 1, this.indexOf("=")).trim()

    private fun String.argb(): String = this.substring(this.indexOf("(") + 1, this.indexOf(")")).replace("0x", "#").replace("0X", "#")

    private fun HashMap<String, String>.toColorTheme(): CustomColorTheme.LocalhostColorTheme = CustomColorTheme.LocalhostColorTheme(
        primary = this["primary"] ?: throw NullPointerException("颜色解析失败"),
        onPrimary = this["onPrimary"] ?: throw NullPointerException("颜色解析失败"),
        primaryContainer = this["primaryContainer"] ?: throw NullPointerException("颜色解析失败"),
        onPrimaryContainer = this["onPrimaryContainer"] ?: throw NullPointerException("颜色解析失败"),
        secondary = this["secondary"] ?: throw NullPointerException("颜色解析失败"),
        onSecondary = this["onSecondary"] ?: throw NullPointerException("颜色解析失败"),
        secondaryContainer = this["secondaryContainer"] ?: throw NullPointerException("颜色解析失败"),
        onSecondaryContainer = this["onSecondaryContainer"] ?: throw NullPointerException("颜色解析失败"),
        tertiary = this["tertiary"] ?: throw NullPointerException("颜色解析失败"),
        onTertiary = this["onTertiary"] ?: throw NullPointerException("颜色解析失败"),
        tertiaryContainer = this["tertiaryContainer"] ?: throw NullPointerException("颜色解析失败"),
        onTertiaryContainer = this["onTertiaryContainer"] ?: throw NullPointerException("颜色解析失败"),
        error = this["error"] ?: throw NullPointerException("颜色解析失败"),
        errorContainer = this["errorContainer"] ?: throw NullPointerException("颜色解析失败"),
        onError = this["onError"] ?: throw NullPointerException("颜色解析失败"),
        onErrorContainer = this["onErrorContainer"] ?: throw NullPointerException("颜色解析失败"),
        background = this["background"] ?: throw NullPointerException("颜色解析失败"),
        onBackground = this["onBackground"] ?: throw NullPointerException("颜色解析失败"),
        outline = this["outline"] ?: throw NullPointerException("颜色解析失败"),
        inverseOnSurface = this["inverseOnSurface"] ?: throw NullPointerException("颜色解析失败"),
        inverseSurface = this["inverseSurface"] ?: throw NullPointerException("颜色解析失败"),
        inversePrimary = this["inversePrimary"] ?: throw NullPointerException("颜色解析失败"),
        surfaceTint = this["surfaceTint"] ?: throw NullPointerException("颜色解析失败"),
        outlineVariant = this["outlineVariant"] ?: throw NullPointerException("颜色解析失败"),
        scrim = this["scrim"] ?: throw NullPointerException("颜色解析失败"),
        surface = this["surface"] ?: throw NullPointerException("颜色解析失败"),
        onSurface = this["onSurface"] ?: throw NullPointerException("颜色解析失败"),
        surfaceVariant = this["surfaceVariant"] ?: throw NullPointerException("颜色解析失败"),
        onSurfaceVariant = this["onSurfaceVariant"] ?: throw NullPointerException("颜色解析失败"),
    )

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun GenerateTheme(
        helpState: MutableState<Boolean>,
        themeName: MutableState<String>,
        colorPath: MutableState<String>
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = themeName.value,
                    onValueChange = { themeName.value = it },
                    label = {
                        Text("*主题名称")
                    },
                    textStyle = MaterialTheme.typography.labelMedium,
                    singleLine = true
                )
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TooltipArea(
                        tooltip = {
                            TooltipText {
                                Text("帮助")
                            }
                        }
                    ) {
                        IconButton(
                            onClick = {
                                helpState.value = true
                            }
                        ) {
                            Icon(Icons.Default.Info, null)
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = colorPath.value,
                    onValueChange = { colorPath.value = it },
                    label = {
                        Text("*输入“Color.kt”文件路径")
                    },
                    textStyle = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = {
                        val fileDialog = FileDialog(JFrame())
                        fileDialog.mode = FileDialog.LOAD
                        fileDialog.isVisible = true
                        val path = fileDialog.directory + fileDialog.file
                        if (File(path).exists()) {
                            colorPath.value = path
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("选择文件")
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ColorThemeItemDefault(
        colorThemeStore: ColorThemeStore,
        selectTheme: MutableState<String>,
        preferencesStore: PreferencesStore,
    ) {
        val darkStore = LocalDark.current
        TooltipArea(
            tooltip = {
                TooltipText {
                    Text("YoHub Color（sRGB）")
                }
            }
        ) {
            Card(
                modifier = Modifier.size(70.dp)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        selectTheme.value = "YoHub Color"
                        colorThemeStore.colorSchemes = ColorThemeStore.ColorTheme(LightColorScheme, DarkColorScheme)
                        CoroutineScope(Dispatchers.IO).launch {
                            preferencesStore.preference[PreferencesName.COLOR_THEME.toString()] = "YoHub Color"
                            preferencesStore.submit()
                        }
                    },
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (darkStore.darkMode) DarkColorScheme.primary else LightColorScheme.primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Checkbox(
                        checked = selectTheme.value == "YoHub Color",
                        onCheckedChange = null,
                        enabled = false,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ColorThemeItem(
        colorThemeStore: ColorThemeStore,
        preferencesStore: PreferencesStore,
        selectTheme: MutableState<String>,
        colorThemeItem: ColorLoader.ColorThemeItem,
        uninstallState: MutableState<Boolean>,
        targetTheme: MutableState<String>,
        targetThemeName: MutableState<String>
    ) {
        val darkStore = LocalDark.current
        TooltipArea(
            tooltip = {
                TooltipText {
                    Text("${colorThemeItem.customColorTheme.name}（${colorThemeItem.customColorTheme.type}）")
                }
            }
        ) {
            Card(
                modifier = Modifier.size(70.dp)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {}
                    .clickable(
                        left = {
                            selectTheme.value = colorThemeItem.fileName
                            colorThemeStore.colorSchemes = ColorThemeStore.ColorTheme(
                                colorThemeItem.customColorTheme.getLightColorScheme(),
                                colorThemeItem.customColorTheme.getDarkColorScheme()
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                preferencesStore.preference[PreferencesName.COLOR_THEME.toString()] =
                                    colorThemeItem.fileName
                                preferencesStore.submit()
                            }
                        },
                        right = {
                            uninstallState.value = true
                            targetTheme.value = colorThemeItem.fileName
                            targetThemeName.value = colorThemeItem.customColorTheme.name
                        }
                    ),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (darkStore.darkMode) colorThemeItem.customColorTheme.getDarkColorScheme().primary else colorThemeItem.customColorTheme.getLightColorScheme().primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Checkbox(
                        checked = selectTheme.value == colorThemeItem.fileName,
                        onCheckedChange = null,
                        enabled = false,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColorThemeItemInstall(
    colorLoader: ColorLoader,
    colorThemeItems: SnapshotStateList<ColorLoader.ColorThemeItem>
) {
    TooltipArea(
        tooltip = {
            TooltipText {
                Text("安装主题")
            }
        }
    ) {
        OutlinedCard(
            modifier = Modifier.size(70.dp)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                    CoroutineScope(Dispatchers.IO).launch {
                        colorLoader.installColorTheme()
                        colorThemeItems.clear()
                        colorThemeItems.addAll(colorLoader.loadInstalledColorThemes())
                    }
                },
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp).align(Alignment.Center))
            }
        }
    }
}

class VersionSetting : NavPage("版本", isNavigation = false) {

    override fun icon(): () -> Unit = {  }

    @Composable
    override fun content() {
        val contextStore = LocalContext.current
        Column {
            Toolbar(label, enableAnimate = false)
            FluentItem(
                Icons.Default.Open,
                "开源地址"
            ) {
                TextButton(
                    onClick = {
                        contextStore.startBrowse("https://github.com/lumyuan/yohub-desktop")
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("去围观")
                }
            }
        }
    }

}

class OpenSourceLicense: NavPage("开源许可", isNavigation = false) {

    override fun icon(): () -> Unit = {  }

    data class LicenseBean(var title: String, var author: String, var tip: String, var url: String)

    private val oss by lazy {
        arrayListOf(
            LicenseBean(
                title = "Compose for Desktop",
                author = "JetBrains",
                tip = "Compose Multiplatform is a declarative framework for sharing UIs across multiple platforms with Kotlin. It is based on Jetpack Compose and developed by JetBrains and open-source contributors.",
                url = "https://github.com/JetBrains/compose-multiplatform"
            ),
            LicenseBean(
                title = "vtools（Scene 4）",
                author = "helloklf",
                tip = "一个集高级重启、应用安装自动点击、CPU调频等多项功能于一体的工具箱。",
                url = "https://github.com/helloklf/vtools"
            ),
            LicenseBean(
                title = "ComposeWindowStyler",
                author = "MayakaApps",
                tip = "Compose Window Styler is a library that lets you style your Compose for Desktop window to have more native and modern UI. This includes styling the window to use acrylic, mica ...etc.",
                url = "https://github.com/MayakaApps/ComposeWindowStyler"
            ),
            LicenseBean(
                title = "compose-fluent-ui",
                author = "Konyaco",
                tip = "Fluent Design UI library for Compose Multiplatform",
                url = "https://github.com/Konyaco/compose-fluent-ui"
            ),
            LicenseBean(
                title = "gson",
                author = "google",
                tip = "A Java serialization/deserialization library to convert Java Objects into JSON and back",
                url = "https://github.com/google/gson"
            ),
            LicenseBean(
                title = "jna",
                author = "java-native-access",
                tip = "Java Native Access (JNA)",
                url = "https://github.com/java-native-access/jna"
            ),
            LicenseBean(
                title = "karavel",
                author = "AppOutlet",
                tip = "Lightweight navigation library for Compose for Desktop",
                url = "https://github.com/AppOutlet/karavel"
            ),
            LicenseBean(
                title = "okhttp",
                author = "square",
                tip = "Square’s meticulous HTTP client for the JVM, Android, and GraalVM.",
                url = "https://github.com/square/okhttp"
            ),
            LicenseBean(
                title = "retrofit",
                author = "square",
                tip = "A type-safe HTTP client for Android and the JVM",
                url = "https://github.com/square/retrofit"
            ),
            LicenseBean(
                title = "load-the-image",
                author = "ltttttttttttt",
                tip = "桌面端图片加载框架, load-the-image Apply to compose-jb(desktop), Used to load network and local pictures, supports caching.",
                url = "https://github.com/ltttttttttttt/load-the-image"
            ),
        )
    }

    @Composable
    override fun content() {
        val contextStore = LocalContext.current
        FluentFold(
            icon = Icons.Default.Code,
            title = label
        ) {
            NavigationItemSeparator(modifier = Modifier.padding(bottom = 4.dp))

            oss.onEach {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {  },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp)
                    ) {
                        Text(it.title, style = MaterialTheme.typography.bodyLarge)
                        Divider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                        Text(it.tip, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(it.author, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f))
                    Spacer(modifier = Modifier.size(8.dp))
                    IconButton(
                        onClick = {
                            contextStore.startBrowse(it.url)
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }
    }

}