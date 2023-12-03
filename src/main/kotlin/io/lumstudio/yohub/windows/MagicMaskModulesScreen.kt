package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.AppsAddIn
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.common.utils.FileCopyUtils
import io.lumstudio.yohub.lang.LocalLanguageType
import io.lumstudio.yohub.runtime.LocalMagiskPatcherRuntime
import io.lumstudio.yohub.runtime.MagiskPatcherStore
import io.lumstudio.yohub.ui.component.FlowButton
import io.lumstudio.yohub.ui.component.FluentItem
import io.lumstudio.yohub.ui.component.Toolbar
import io.lumstudio.yohub.windows.navigation.MagicMaskModulesPage
import io.lumstudio.yohub.windows.navigation.MagiskPatcherPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.FileDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MagicMaskModulesScreen(magicMaskModulesPage: MagicMaskModulesPage) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val scrollState = rememberScrollState()
        ScrollbarContainer(
            adapter = rememberScrollbarAdapter(scrollState),
        ) {
            Column(
                modifier = Modifier.fillMaxHeight().verticalScroll(scrollState)
                    .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    magicMaskModulesPage.nestedItems?.onEach {
                        FlowButton(
                            icon = {
                                it.icon().invoke()
                            },
                            onClick = {
                                selectPage.value = it.parent ?: it
                                magicMaskModulesPage.karavel?.navigate(it)
                            }
                        ) {
                            Text(it.label())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MagiskPatcherScreen(magiskPatcherPage: MagiskPatcherPage) {
    val languageBasic = LocalLanguageType.value.lang
    val keepShellStore = LocalKeepShell.current
    val magiskPatcherStore = LocalMagiskPatcherRuntime.current
    val window = LocalWindowMain.current

    val targetPath = remember { mutableStateOf("") }
    val outPath = remember { mutableStateOf("") }
    val fileDialog = remember { FileDialog(window) }
    val version = remember { mutableStateOf("") }
    val magiskVersionFile = File(magiskPatcherStore.magiskPatcherHostFile, "prebuilt")

    LaunchedEffect(Unit) {
        version.value = magiskVersionFile.listFiles()?.first()?.name ?: ""
    }

    val scrollState = rememberScrollState()
    ScrollbarContainer(
        adapter = rememberScrollbarAdapter(scrollState),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(scrollState).padding(16.dp)
        ) {
            Toolbar(magiskPatcherPage.label())
            TargetPathEditor(targetPath, outPath, fileDialog)
            Spacer(modifier = Modifier.size(8.dp))
            FluentItem(
                icon = {
                       Icon(Icons.Default.AppsAddIn, null, modifier = Modifier.fillMaxSize())
                },
                title = languageBasic.magiskVersion
            ) {
                var open by remember { mutableStateOf(false) }
                TextButton(
                    onClick = {
                        open = true
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(version.value)
                }
                DropdownMenu(
                    expanded = open,
                    onDismissRequest = { open = false }
                ) {
                    magiskVersionFile.listFiles()?.toList()?.sortedBy { it.name }?.onEach {
                        DropdownMenuItem(
                            text = {
                                Text(it.name)
                            },
                            onClick = {
                                open = false
                                version.value = it.name
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            OutputPathEditor(targetPath, outPath)

            val exists = targetPath.value.endsWith(".img") && File(targetPath.value).exists()
            val text = remember { mutableStateOf(languageBasic.startPatcherImage) }
            val patcherState = remember { mutableStateOf(false) }
            AnimatedVisibility(exists) {
                Column {
                    Spacer(modifier = Modifier.size(28.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                patcherImage(
                                    keepShellStore,
                                    targetPath,
                                    outPath,
                                    text,
                                    patcherState,
                                    magiskPatcherStore,
                                    version
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !patcherState.value
                    ) {
                        AnimatedVisibility(patcherState.value) {
                            Row {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                        Text(text.value)
                    }
                }
            }

            AnimatedVisibility(!exists) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {},
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        languageBasic.notChooseBoot,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

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
                Text(languageBasic.inputBootPath)
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily(Font(R.font.jetBrainsMonoRegular))),
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = {
                fileDialog.file = "*.img"
                fileDialog.mode = FileDialog.LOAD
                fileDialog.isVisible = true
                if (fileDialog.file?.endsWith(".img") == true) {
                    targetPath.value = fileDialog.directory + fileDialog.file
                    outPath.value = fileDialog.directory
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
    AnimatedVisibility(targetPath.value.endsWith(".img") && File(targetPath.value).exists()) {
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
                        Text(languageBasic.bootOutputPath)
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

private val dateFormat by lazy {
    SimpleDateFormat("yyMMddmmss")
}

private suspend fun patcherImage(
    keepShellStore: KeepShellStore,
    targetPath: MutableState<String>,
    outPath: MutableState<String>,
    text: MutableState<String>,
    patcherState: MutableState<Boolean>,
    magiskPatcherStore: MagiskPatcherStore,
    version: MutableState<String>
) = withContext(Dispatchers.IO) {
    val languageBasic = LocalLanguageType.value.lang
    text.value = languageBasic.bootPatching
    patcherState.value = true
    val out = keepShellStore magiskPatcher "boot_patch \"${targetPath.value}\" true false false \"${version.value}\""
    if (out.split("\n").last { it.isNotEmpty() }.contains("Success")) {
        val tempPath = File(magiskPatcherStore.magiskPatcherHostFile, "new-boot.img")
        val outFile = File(outPath.value, "magisk-patched-${dateFormat.format(Date())}.img")
        try {
            FileCopyUtils.copyFile(tempPath, outFile)
            sendNotice(languageBasic.patchSuccess, String.format(languageBasic.patchSuccessMessage, outFile.name, outFile.parent)) {
                try {
                    Desktop.getDesktop().open(outFile.parentFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendNotice(languageBasic.patchFail, e.toString())
        }
    } else {
        sendNotice(languageBasic.patchFail, out.split("\n")[0])
    }
    text.value = languageBasic.startPatcherImage
    patcherState.value = false
}