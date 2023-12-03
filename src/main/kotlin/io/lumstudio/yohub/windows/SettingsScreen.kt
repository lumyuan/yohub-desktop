package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.konyaco.fluent.component.NavigationItemSeparator
import com.konyaco.fluent.component.Scrollbar
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.*
import com.lt.load_the_image.rememberImagePainter
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.LocalContext
import io.lumstudio.yohub.common.LocalIOCoroutine
import io.lumstudio.yohub.common.net.api.impl.Repository
import io.lumstudio.yohub.common.net.pojo.YoHubRepos
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.MemoryUtil
import io.lumstudio.yohub.common.utils.*
import io.lumstudio.yohub.lang.LanguageType
import io.lumstudio.yohub.lang.LocalLanguageType
import io.lumstudio.yohub.model.request
import io.lumstudio.yohub.runtime.LocalInstallThemesPath
import io.lumstudio.yohub.theme.*
import io.lumstudio.yohub.ui.component.*
import io.lumstudio.yohub.windows.navigation.NavPage
import io.lumstudio.yohub.windows.navigation.SettingsPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import javax.swing.JFrame

@Composable
fun SettingsScreen(settingsPage: SettingsPage) {
    val languageBasic = LocalLanguageType.value.lang
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
                Text(
                    languageBasic.appCopyright,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

class ThemeSetting : NavPage(isNavigation = false) {

    override fun icon(): () -> Unit = { }
    override fun label(): String = LocalLanguageType.value.lang.labelTheme

    override fun title(): String? = null

    override fun subtitle(): String? = null

    private val gson by lazy { Gson() }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun content() {
        val languageBasic = LocalLanguageType.value.lang
        val themeStore = LocalTheme.current
        val preferencesStore = LocalPreferences.current
        val ioCoroutine = LocalIOCoroutine.current
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Toolbar(label(), enableAnimate = false)
            FluentItem(
                Icons.Default.DarkTheme,
                languageBasic.darkMode
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
                            Text(
                                when (it) {
                                    DarkTheme.SYSTEM -> languageBasic.darkModeSystem
                                    DarkTheme.LIGHT -> languageBasic.darkModeLight
                                    DarkTheme.DARK -> languageBasic.darkModeDark
                                }
                            )
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
                languageBasic.themeColor,
                content = {
                    TextButton(
                        onClick = {
                            generateState.value = true
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(languageBasic.generateTheme)
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
                title = languageBasic.tips,
                visible = uninstallState.value,
                cancelButtonText = languageBasic.cancel,
                confirmButtonText = languageBasic.defined,
                onCancel = {
                    uninstallState.value = false
                },
                onConfirm = {
                    uninstallState.value = false
                    if (targetThemeFileName.value == selectTheme.value) {
                        sendNotice(languageBasic.uninstallFail, languageBasic.uninstallFailMessage)
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            colorLoader.uninstallColorTheme(targetThemeFileName.value, targetThemeName.value)
                            colorThemeItems.clear()
                            colorThemeItems.addAll(colorLoader.loadInstalledColorThemes())
                        }
                    }
                },
                content = {
                    Text(
                        String.format(
                            languageBasic.uninstallThemeTip,
                            targetThemeName.value,
                            targetThemeFileName.value
                        )
                    )
                }
            )

            val themeName = remember { mutableStateOf("") }
            val colorPath = remember { mutableStateOf("") }
            val window = LocalWindowMain.current
            Dialog(
                title = languageBasic.generateTheme,
                visible = generateState.value,
                cancelButtonText = languageBasic.cancel,
                confirmButtonText = languageBasic.generateThemeFile,
                onCancel = {
                    generateState.value = false
                },
                onConfirm = {
                    generateState.value = false
                    if (colorPath.value.trim().isEmpty()) {
                        sendNotice(languageBasic.analysisFail, languageBasic.pleaseInputColorFilePath)
                        return@Dialog
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        val colorFile = File(colorPath.value)
                        if (colorFile.exists()) {
                            val light = HashMap<String, String>()
                            val dark = HashMap<String, String>()
                            val kt = String(readBytes(FileInputStream(colorFile)))
                            val list = kt.split("\n").toList().filter { it.contains("val ") }
                            if (list.isEmpty()) {
                                sendNotice(languageBasic.themeAnalysisFail, languageBasic.isNotEffectiveColorFile)
                            } else if (themeName.value.trim().isEmpty()) {
                                sendNotice(languageBasic.themeCreateFail, languageBasic.themeNameCannotEmpty)
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
                                    val customColorTheme =
                                        CustomColorTheme(name = themeName.value, light = lightColor, dark = darkColor)
                                    val json = gson.toJson(customColorTheme)
                                    val fileDialog = FileDialog(window, "", FileDialog.SAVE)
                                    fileDialog.file = "${themeName.value}.json"
                                    fileDialog.isVisible = true
                                    val path = fileDialog.directory + fileDialog.file
                                    writeBytes(FileOutputStream(path), json.toByteArray())
                                    sendNotice(
                                        languageBasic.themeGenerateSuccess,
                                        String.format(
                                            languageBasic.themeGenerateSuccessMessage,
                                            themeName.value,
                                            fileDialog.directory
                                        )
                                    )
                                    themeName.value = ""
                                    colorPath.value = ""
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    sendNotice(languageBasic.themeGenerateError, "${e.message}")
                                }
                            }
                        } else {
                            sendNotice(
                                languageBasic.themeGenerateFail,
                                String.format(languageBasic.targetFileIsNotExists, colorPath.value)
                            )
                        }
                    }
                },
                content = {
                    GenerateTheme(helpState, themeName, colorPath)
                }
            )

            val contextStore = LocalContext.current
            Dialog(
                title = languageBasic.help,
                visible = helpState.value,
                cancelButtonText = languageBasic.iKnown,
                confirmButtonText = languageBasic.getColorFile,
                onCancel = {
                    helpState.value = false
                },
                onConfirm = {
                    helpState.value = false
                    contextStore.startBrowse("https://m3.material.io/theme-builder")
                },
                content = {
                    Text(languageBasic.generateColorThemeMessage)
                }
            )
        }
    }

    private fun String.name(): String = this.substring(this.lastIndexOf("_") + 1, this.indexOf("=")).trim()

    private fun String.argb(): String =
        this.substring(this.indexOf("(") + 1, this.indexOf(")")).replace("0x", "#").replace("0X", "#")

    private fun HashMap<String, String>.toColorTheme(): CustomColorTheme.LocalhostColorTheme =
        CustomColorTheme.LocalhostColorTheme(
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
        val window = LocalWindowMain.current
        val languageBasic = LocalLanguageType.value.lang
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
                        Text(languageBasic.themeName)
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
                                Text(languageBasic.help)
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
                        Text(languageBasic.colorFilePath)
                    },
                    textStyle = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = {
                        val fileDialog = FileDialog(window, "", FileDialog.LOAD)
                        fileDialog.isVisible = true
                        val path = fileDialog.directory + fileDialog.file
                        if (File(path).exists()) {
                            colorPath.value = path
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(languageBasic.chooseFile)
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
    val languageBasic = LocalLanguageType.value.lang
    val window = LocalWindowMain.current

    TooltipArea(
        tooltip = {
            TooltipText {
                Text(languageBasic.installTheme)
            }
        }
    ) {
        OutlinedCard(
            modifier = Modifier.size(70.dp)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                    CoroutineScope(Dispatchers.IO).launch {
                        colorLoader.installColorTheme(window)
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

class VersionSetting : NavPage(isNavigation = false) {

    override fun icon(): () -> Unit = { }
    override fun label(): String = LocalLanguageType.value.lang.labelVersion

    override fun title(): String? = null

    override fun subtitle(): String? = null

    @Composable
    override fun content() {
        val contextStore = LocalContext.current
        val languageBasic = LocalLanguageType.value.lang
        Column {
            Toolbar(label(), enableAnimate = false)
            FluentItem(
                Icons.Default.Info,
                languageBasic.softVersion,
                subtitle = String.format(languageBasic.appVersion, contextStore.versionTag)
            ) {
                TextButton(
                    onClick = {
                        contextStore.startBrowse("https://github.com/lumyuan/yohub-desktop")
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(languageBasic.openSourceLicenseUrl)
                }
                Spacer(modifier = Modifier.size(16.dp))

                val open = remember { mutableStateOf(false) }
                val hasUpdate = remember { mutableStateOf(false) }
                val repos = remember { mutableStateListOf<YoHubRepos>() }
                val richTextState = rememberRichTextState()
                val data = remember { mutableStateOf<YoHubRepos?>(null) }
                val preferencesStore = LocalPreferences.current
                val simpleDateFormat = remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")) }
                LaunchedEffect(open) {
                    snapshotFlow { open.value }
                        .onEach {
                            if (it) {
                                request(
                                    showError = false,
                                    {
                                        repos.clear()
                                        repos.addAll(Repository.appRepos())
                                        val yoHubRepos = repos.first()
                                        data.value = yoHubRepos
                                        //判断GitHub上的Release版本是否高于本地版本
                                        hasUpdate.value = contextStore.versionTag compareVersions yoHubRepos.tag_name < 0 && (preferencesStore.preference[PreferencesName.IGNORE_VERSION.toString()] != yoHubRepos.tag_name || !yoHubRepos.prerelease)
                                        if (!hasUpdate.value) {
                                            sendNotice(LocalLanguageType.value.lang.tips, LocalLanguageType.value.lang.isLatest)
                                        }
                                        richTextState.setMarkdown(yoHubRepos.body)
                                        open.value = false
                                    }
                                )
                            }
                        }.launchIn(this)
                }

                AnimatedVisibility(
                    !open.value,
                ) {
                    Button(
                        onClick = {
                            open.value = true
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(languageBasic.checkVerion)
                    }
                }

                AnimatedVisibility(
                    open.value,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                }

                Dialog(
                    visible = hasUpdate.value,
                    title = languageBasic.hasNewVersion,
                    confirmButtonText = languageBasic.gotoDownload,
                    onConfirm = {
                        contextStore.startBrowse(repos.first().assets.first().browser_download_url)
                    },
                    cancelButtonText = if (data.value?.prerelease == true) {
                        languageBasic.cancel
                    } else null,
                    onCancel = if (data.value?.prerelease == true) {
                        {
                            CoroutineScope(Dispatchers.IO).launch {
                                preferencesStore.preference[PreferencesName.IGNORE_VERSION.toString()] = data.value?.tag_name
                                preferencesStore.submit()
                                hasUpdate.value = false
                            }
                        }
                    } else null,
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        ) {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier.fillMaxWidth().height(250.dp).weight(1f)
                                    .verticalScroll(scrollState)
                            ) {
                                SelectionContainer {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val imagePainter = rememberImagePainter(
                                                data.value?.assets?.first()?.uploader?.avatar_url ?: "",
                                                R.icon.logoRound
                                            )
                                            Image(
                                                painter = imagePainter,
                                                null,
                                                modifier = Modifier.size(45.dp).clip(RoundedCornerShape(22.5.dp)).clickable {
                                                    contextStore.startBrowse(data.value?.assets?.first()?.uploader?.html_url ?: "")
                                                }
                                            )
                                            Spacer(modifier = Modifier.size(16.dp))
                                            Text(
                                                data.value?.assets?.first()?.uploader?.login ?: "Unknown",
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        }
                                        Spacer(modifier = Modifier.size(16.dp))
                                        Text(
                                            String.format(
                                                languageBasic.updateVersionText,
                                                data.value?.name,
                                                simpleDateFormat.value.format(data.value?.created_at),
                                                MemoryUtil.format(data.value?.assets?.get(0)?.size ?: 0L),
                                                data.value?.assets?.get(0)?.download_count
                                            )
                                        )
                                        Spacer(modifier = Modifier.size(16.dp))
                                        RichText(state = richTextState, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                            Scrollbar(
                                isVertical = true,
                                adapter = androidx.compose.foundation.rememberScrollbarAdapter(scrollState),
                                modifier = Modifier.fillMaxHeight()
                            )
                        }
                    }
                )
            }
        }
    }

}

class OpenSourceLicense : NavPage(isNavigation = false) {

    override fun icon(): () -> Unit = { }
    override fun label(): String = LocalLanguageType.value.lang.labelOpenSourceLicense

    override fun title(): String? = null

    override fun subtitle(): String? = null

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
            LicenseBean(
                title = "Compose-Rich-Editor",
                author = "MohamedRejeb",
                tip = "A Rich text editor library for both Jetpack Compose and Compose Multiplatform, fully customizable and supports the common rich text editor features.",
                url = "https://github.com/MohamedRejeb/Compose-Rich-Editor"
            ),
        )
    }

    @Composable
    override fun content() {
        val contextStore = LocalContext.current
        FluentFold(
            icon = Icons.Default.Code,
            title = label()
        ) {
            NavigationItemSeparator(modifier = Modifier.padding(bottom = 4.dp))

            oss.onEach {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { },
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
                    Text(
                        it.author,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f)
                    )
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

class LanguagePage : NavPage(isNavigation = false) {
    override fun icon(): @Composable () -> Unit = { }

    override fun label(): String = LocalLanguageType.value.lang.language

    override fun title(): String? = null

    override fun subtitle(): String? = null

    @Composable
    override fun content() {
        val languageType = LocalLanguageType.value
        val preferencesStore = LocalPreferences.current
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Toolbar(label(), enableAnimate = false)
            FluentItem(
                icon = {
                    Icon(Icons.Default.LocalLanguage, null, modifier = Modifier.fillMaxSize())
                },
                title = languageType.lang.languageSetting
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    var dropdownMenuState by remember { mutableStateOf(false) }
                    TextButton(
                        onClick = {
                            dropdownMenuState = true
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            when (languageType) {
                                LanguageType.DEFAULT -> languageType.lang.defaultLanguage
                                else -> languageType.languageName
                            }
                        )
                    }
                    DropdownMenu(
                        dropdownMenuState,
                        onDismissRequest = {
                            dropdownMenuState = false
                        },
                        focusable = false
                    ) {
                        LanguageType.values().toList()
                            .onEach {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (it) {
                                                LanguageType.DEFAULT -> languageType.lang.defaultLanguage
                                                else -> it.languageName
                                            }
                                        )
                                    },
                                    onClick = {
                                        LocalLanguageType.value = it
                                        CoroutineScope(Dispatchers.IO).launch {
                                            preferencesStore.preference[PreferencesName.LANGUAGE.toString()] =
                                                it.toString()
                                            preferencesStore.submit()
                                        }
                                        dropdownMenuState = false
                                    }
                                )
                            }
                    }
                }
            }
        }
    }
}


enum class RootCode(val value: String) {
    SU("su"), KSU("su"), SUU("suu")
}

class AdvancedSettingPage : NavPage(isNavigation = false) {
    override fun icon(): @Composable () -> Unit = { }

    override fun label(): String = LocalLanguageType.value.lang.labelAdvancedSetting

    override fun title(): String? = null

    override fun subtitle(): String? = null

    @Composable
    override fun content() {
        val languageBasic = LocalLanguageType.value.lang
        val preferencesStore = LocalPreferences.current
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Toolbar(label(), enableAnimate = false)
            FluentItem(
                icon = {
                    Icon(Icons.Default.ClipboardCode, null, modifier = Modifier.fillMaxSize())
                },
                title = languageBasic.adminCode,
                subtitle = languageBasic.adminCodeSubtitle
            ) {
                val open = remember { mutableStateOf(false) }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(
                        onClick = {
                            open.value = true
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            try {
                                RootCode.valueOf(
                                    preferencesStore.preference[PreferencesName.ROOT_CODE.toString()] ?: ""
                                )
                            } catch (e: Exception) {
                                RootCode.SU
                            }.toString()
                        )
                    }
                }
                DropdownMenu(expanded = open.value, onDismissRequest = { open.value = false }) {
                    RootCode.values().toList()
                        .onEach {
                            DropdownMenuItem(
                                text = {
                                    Text(it.toString())
                                },
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        preferencesStore.preference[PreferencesName.ROOT_CODE.toString()] =
                                            it.toString()
                                        preferencesStore.submit()
                                        open.value = false
                                    }
                                }
                            )
                        }
                }
            }
        }
    }

}