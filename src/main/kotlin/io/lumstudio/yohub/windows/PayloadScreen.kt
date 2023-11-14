package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.ArrowDownload
import com.konyaco.fluent.icons.regular.Search
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.LocalContext
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.common.shell.MemoryUtil
import io.lumstudio.yohub.common.utils.FileCopyUtils
import io.lumstudio.yohub.lang.LocalLanguageType
import io.lumstudio.yohub.runtime.LocalPayloadDumperRuntime
import io.lumstudio.yohub.runtime.LocalRuntime
import io.lumstudio.yohub.runtime.PayloadDumperStore
import io.lumstudio.yohub.runtime.RuntimeStore
import io.lumstudio.yohub.ui.component.Toolbar
import io.lumstudio.yohub.ui.component.TooltipText
import io.lumstudio.yohub.windows.navigation.PayloadPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.FileDialog
import java.io.File
import java.io.FilenameFilter
import javax.swing.JFrame

@Composable
fun PayloadScreen(payloadPage: PayloadPage) {
    val languageBasic = LocalLanguageType.value.lang
    val keepShellStore = LocalKeepShell.current
    val payloadDumperStore = LocalPayloadDumperRuntime.current

    val targetPath = remember { mutableStateOf("") }
    val outPath = remember { mutableStateOf("") }
    val fileDialog = remember { FileDialog(JFrame()) }

    val loadState = remember { mutableStateOf(false) }
    val imageList = remember { mutableStateListOf<Image>() }

    val scrollState = rememberScrollState()
    ScrollbarContainer(
        adapter = com.konyaco.fluent.component.rememberScrollbarAdapter(scrollState),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(scrollState).padding(16.dp)
        ) {
            Toolbar(payloadPage.label())
            MiuiDownload()
            TargetPathEditor(targetPath, outPath, fileDialog)
            Spacer(modifier = Modifier.size(8.dp))
            OutputPathEditor(targetPath, outPath)
            Spacer(modifier = Modifier.size(28.dp))
            Text(languageBasic.imageList, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.size(16.dp))

            val searchText = remember { mutableStateOf("") }
            val exists = targetPath.value.endsWith(".bin") && File(targetPath.value).exists()

            SearchLayout(searchText, targetPath, loadState, imageList, payloadDumperStore, keepShellStore)
            Spacer(modifier = Modifier.size(16.dp))
            AnimatedVisibility(exists) {
                Images(keepShellStore, payloadDumperStore, targetPath, outPath, searchText, loadState, imageList)
            }
            AnimatedVisibility(!exists) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {},
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        languageBasic.notChoosePayloadFile,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MiuiDownload() {
    val languageBasic = LocalLanguageType.value.lang
    val contextStore = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(languageBasic.downloadMIUIRom, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = {
                    contextStore.startBrowse("https://xiaomirom.com/")
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("XiaomiROM.com")
            }
            TextButton(
                onClick = {
                    contextStore.startBrowse("https://miuiver.com/")
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("MIUIVer.com")
            }
            TextButton(
                onClick = {
                    contextStore.startBrowse("https://roms.miuier.com/mobile/zh-cn/")
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("MIUIROMS")
            }
            TextButton(
                onClick = {
                    contextStore.startBrowse("https://miui.511i.cn/")
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("PureSky ROM")
            }
        }
    }
}

@Stable
data class Image(
    val name: String,
    val size: String,
    val hash: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetPathEditor(targetPath: MutableState<String>, outPath: MutableState<String>, fileDialog: FileDialog) {
    val languageBasic = LocalLanguageType.value.lang
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = targetPath.value,
            onValueChange = { targetPath.value = it },
            label = {
                Text(languageBasic.inputPayloadPath)
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily(Font(R.font.jetBrainsMonoRegular))),
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = {
                fileDialog.filenameFilter = FilenameFilter { _, name -> name.endsWith(".bin") }
                fileDialog.mode = FileDialog.LOAD
                fileDialog.isVisible = true
                if (fileDialog.file?.endsWith(".bin") == true) {
                    targetPath.value = fileDialog.directory + fileDialog.file
                    outPath.value = fileDialog.directory + "images"
                } else if (fileDialog.file != null) {
                    sendNotice(languageBasic.chooseFail, String.format(languageBasic.chooseFailMessage, fileDialog.file))
                }
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(languageBasic.chooseFile)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.OutputPathEditor(targetPath: MutableState<String>, outPath: MutableState<String>) {
    val languageBasic = LocalLanguageType.value.lang
    AnimatedVisibility(targetPath.value.endsWith(".bin") && File(targetPath.value).exists()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = outPath.value,
                    onValueChange = {},
                    label = {
                        Text(languageBasic.imageOutputPath)
                    },
                    singleLine = true,
                    readOnly = true,
                    textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily(Font(R.font.jetBrainsMonoRegular))),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = {
                        val file = File(outPath.value)
                        if (!file.exists()) {
                            file.mkdirs()
                        }
                        Desktop.getDesktop().open(file)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(languageBasic.openFileManager)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SearchLayout(
    searchText: MutableState<String>,
    targetPath: MutableState<String>,
    loadState: MutableState<Boolean>,
    imageList: SnapshotStateList<Image>,
    payloadDumperStore: PayloadDumperStore,
    keepShellStore: KeepShellStore
) {
    val languageBasic = LocalLanguageType.value.lang
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.width(300.dp),
            value = searchText.value,
            onValueChange = {
                searchText.value = it
            },
            textStyle = MaterialTheme.typography.labelMedium,
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, null)
            },
            trailingIcon = {
                if (searchText.value.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchText.value = ""
                        }
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Outlined.Close, null)
                    }
                }
            },
            label = {
                Text(languageBasic.searchImage)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            AnimatedVisibility(targetPath.value.endsWith(".bin") && File(targetPath.value).exists()) {
                TooltipArea(
                    tooltip = {
                        TooltipText {
                            Text(languageBasic.refreshList)
                        }
                    }
                ) {
                    IconButton(
                        onClick = {
                            searchText.value = ""
                            CoroutineScope(Dispatchers.IO).launch {
                                zipPayload(
                                    loadState,
                                    imageList,
                                    keepShellStore,
                                    payloadDumperStore,
                                    targetPath.value
                                )
                            }
                        }
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Outlined.Refresh, null)
                    }
                }
            }
        }
    }
}

@Composable
private fun Images(
    keepShellStore: KeepShellStore,
    payloadDumperStore: PayloadDumperStore,
    targetPath: MutableState<String>,
    outPath: MutableState<String>,
    searchText: MutableState<String>,
    loadState: MutableState<Boolean>,
    imageList: SnapshotStateList<Image>
) {
    val languageBasic = LocalLanguageType.value.lang
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedVisibility(!loadState.value) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                Spacer(modifier = Modifier.size(8.dp))
                Text(languageBasic.loading, style = MaterialTheme.typography.labelMedium)
            }
        }

        LaunchedEffect(imageList) {
            zipPayload(loadState, imageList, keepShellStore, payloadDumperStore, targetPath.value)
        }

        if (imageList.isNotEmpty()) {
            Text(String.format(languageBasic.findImageCount, imageList.size), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.size(16.dp))
        }

        imageList.filter { it.name.lowercase().contains(searchText.value.lowercase().trim()) }.onEach {
            ImageItem(it, targetPath, outPath, keepShellStore, payloadDumperStore)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageItem(
    image: Image,
    targetPath: MutableState<String>,
    outPath: MutableState<String>,
    keepShellStore: KeepShellStore,
    payloadDumperStore: PayloadDumperStore
) {
    val languageBasic = LocalLanguageType.value.lang
    val runtimeStore = LocalRuntime.current
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(.5.dp, DividerDefaults.color)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${image.name[0]}".uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Text(image.name, style = MaterialTheme.typography.bodyLarge, softWrap = false)
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    "hash: ${image.hash}",
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = false,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    image.size,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f)
                )
                Spacer(modifier = Modifier.size(16.dp))
                val extractState = remember { mutableStateOf(true) }
                if (extractState.value) {
                    TooltipArea(
                        tooltip = {
                            TooltipText {
                                Text(String.format(languageBasic.pickImage, image.name))
                            }
                        }
                    ) {
                        IconButton(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    extractImage(
                                        image,
                                        extractState,
                                        runtimeStore,
                                        keepShellStore,
                                        payloadDumperStore,
                                        targetPath,
                                        outPath
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.ArrowDownload, null)
                        }
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            }
        }
    }
}

private suspend fun zipPayload(
    loadState: MutableState<Boolean>,
    imageList: SnapshotStateList<Image>,
    keepShellStore: KeepShellStore,
    payloadDumperStore: PayloadDumperStore,
    path: String
) = withContext(Dispatchers.IO) {
    loadState.value = false
    val out = keepShellStore cmd payloadDumperStore.payload("-l \"$path\"")
    imageList.clear()
    out.split("\n\n").onEach {
        var name = ""
        var size = "0MB"
        var hash = ""
        if (it.replace("\n", "").trim().isNotBlank()) {
            it.split("\n").onEach { item ->
                when {
                    item.lowercase().contains("name") -> {
                        name = item.substring(item.lastIndexOf(" ") + 1)
                    }

                    item.lowercase().contains("size") -> {
                        size = try {
                            val byte = item.substring(item.lastIndexOf(" ") + 1).toLong()
                            if (byte < MemoryUtil.SizeUnit.KB.mp) {
                                "${byte}B"
                            } else if (byte >= MemoryUtil.SizeUnit.KB.mp && byte < MemoryUtil.SizeUnit.MB.mp) {
                                String.format("%.2fKB", MemoryUtil.b2kb(byte))
                            } else if (byte >= MemoryUtil.SizeUnit.MB.mp && byte < MemoryUtil.SizeUnit.GB.mp) {
                                String.format("%.2fMB", MemoryUtil.b2mb(byte))
                            } else if (byte >= MemoryUtil.SizeUnit.GB.mp && byte < MemoryUtil.SizeUnit.TB.mp) {
                                String.format("%.2fGB", MemoryUtil.b2gb(byte))
                            } else {
                                String.format("%.2fTG", MemoryUtil.b2tb(byte))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            "0B"
                        }
                    }

                    item.lowercase().contains("hash") -> {
                        hash = item.substring(item.lastIndexOf(" ") + 1)
                    }
                }
            }
            imageList.add(Image(name, size, hash))
        }
    }

    loadState.value = true
}

/**
 * 提取镜像
 */
private suspend fun extractImage(
    image: Image,
    extractState: MutableState<Boolean>,
    runtimeStore: RuntimeStore,
    keepShellStore: KeepShellStore,
    payloadDumperStore: PayloadDumperStore,
    targetPath: MutableState<String>,
    outPath: MutableState<String>
) = withContext(Dispatchers.IO) {
    val languageBasic = LocalLanguageType.value.lang
    extractState.value = false
    val out = keepShellStore cmd payloadDumperStore.payload("-p ${image.name} ${targetPath.value}")
    val tempPath = runtimeStore.runtimeFile.absolutePath + File.separator + image.name + ".img"
    if (out.replace(" ", "").contains("partition:${image.name}dumped")) {
        val file = File(outPath.value)
        if (!file.exists()) {
            file.mkdirs()
        }
        try {
            val outPathString = outPath.value + File.separator + image.name + ".img"
            FileCopyUtils.copyFile(File(tempPath), File(outPathString))
            sendNotice(languageBasic.noticePickAppSuccess, String.format(languageBasic.imagePickedSaveAt, outPathString)) {
                Desktop.getDesktop().open(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendNotice(languageBasic.noticePickAppFail, e.toString())
        }
    } else {
        sendNotice(languageBasic.noticePickAppFail, "AssertionError: operation data hash mismatch.")
    }
    File(tempPath).delete()
    extractState.value = true
}