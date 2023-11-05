package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.common.utils.FileCopyUtils
import io.lumstudio.yohub.runtime.*
import io.lumstudio.yohub.ui.component.Toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.FileDialog
import java.io.File
import java.io.FilenameFilter
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JFrame

@Composable
fun MagicMaskModulesScreen(magicMaskModulesPage: MagicMaskModulesPage) {
    val scrollState = rememberScrollState()
    ScrollbarContainer(
        adapter = rememberScrollbarAdapter(scrollState),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(scrollState).padding(16.dp)
        ) {
            magicMaskModulesPage.nestedItems?.onEach {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .clickable {
                            magicMaskModulesPage.karavel?.navigate(it)
                        }
                ) {
                    Text(
                        it.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MagiskPatcherScreen(magiskPatcherPage: MagiskPatcherPage) {
    val keepShellStore = LocalKeepShell.current
    val runtimeStore = LocalRuntime.current
    val pythonStore = LocalPythonRuntime.current
    val magiskPatcherStore = LocalMagiskPatcherRuntime.current

    val targetPath = remember { mutableStateOf("") }
    val outPath = remember { mutableStateOf("") }
    val fileDialog = remember { FileDialog(JFrame()) }

    val scrollState = rememberScrollState()
    ScrollbarContainer(
        adapter = rememberScrollbarAdapter(scrollState),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(scrollState).padding(16.dp)
        ) {
            Toolbar(magiskPatcherPage.label)
            TargetPathEditor(targetPath, outPath, fileDialog)
            Spacer(modifier = Modifier.size(8.dp))
            OutputPathEditor(targetPath, outPath)

            val exists = targetPath.value.endsWith(".img") && File(targetPath.value).exists()
            val text = remember { mutableStateOf("开始修补镜像") }
            val patcherState = remember { mutableStateOf(false) }
            AnimatedVisibility(exists) {
                Column {
                    Spacer(modifier = Modifier.size(28.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                patcherImage(
                                    runtimeStore,
                                    keepShellStore,
                                    pythonStore,
                                    magiskPatcherStore,
                                    targetPath,
                                    outPath,
                                    text,
                                    patcherState
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
                        "未选择Boot文件",
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = targetPath.value,
            onValueChange = { targetPath.value = it },
            label = {
                Text("输入镜像文件路径（支持boot/init_boot）")
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily(Font(R.font.jetBrainsMonoRegular))),
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = {
                fileDialog.filenameFilter = FilenameFilter { _, name -> name.endsWith(".img") }
                fileDialog.mode = FileDialog.LOAD
                fileDialog.isVisible = true
                if (fileDialog.file?.endsWith(".img") == true) {
                    targetPath.value = fileDialog.directory + fileDialog.file
                    outPath.value = fileDialog.directory
                } else if (fileDialog.file != null) {
                    sendNotice("选择失败", "不受支持的文件类型：${fileDialog.file}")
                }
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("选择文件")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.OutputPathEditor(targetPath: MutableState<String>, outPath: MutableState<String>) {
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
                        Text("Boot文件输出路径")
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
                    Text("打开文件管理器")
                }
            }
        }
    }
}

private val dateFormat by lazy {
    SimpleDateFormat("yyMMddmmss")
}

private suspend fun patcherImage(
    runtimeStore: RuntimeStore,
    keepShellStore: KeepShellStore,
    pythonStore: PythonStore,
    magiskPatcherStore: MagiskPatcherStore,
    targetPath: MutableState<String>,
    outPath: MutableState<String>,
    text: MutableState<String>,
    patcherState: MutableState<Boolean>
) = withContext(Dispatchers.IO) {
    text.value = "Boot修补中，请稍候..."
    patcherState.value = true
    val out = keepShellStore cmd pythonStore.py(magiskPatcherStore.script("boot_patch.py ${targetPath.value}"))
    if (out.contains("- Repacking boot image")) {
        val tempPath = File(runtimeStore.runtimeFile, "new-boot.img")
        val outFile = File(outPath.value, "magisk-patched-${dateFormat.format(Date())}.img")
        try {
            FileCopyUtils.copyFile(tempPath, outFile)
            sendNotice("修补成功！", "已将修补好的镜像文件【${outFile.name}】存放于${outFile.parent}路径下") {
                try {
                    Desktop.getDesktop().open(outFile.parentFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendNotice("修补失败！", e.toString())
        }
    } else {
        sendNotice("修补失败！", out.split("\n")[0])
    }
    text.value = "开始修补镜像"
    patcherState.value = false
}