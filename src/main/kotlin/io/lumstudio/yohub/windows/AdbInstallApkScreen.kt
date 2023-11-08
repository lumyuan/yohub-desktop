package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.LocalKeepShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.io.File
import java.io.FilenameFilter
import javax.swing.JFrame

@Composable
fun AdbInstallApkScreen() {
    val keepShellStore = LocalKeepShell.current
    val installState = remember { mutableStateOf(false) }
    val targetPath = remember { mutableStateOf("") }
    val fileDialog by remember { mutableStateOf(FileDialog(JFrame())) }
    TargetPathEditor(targetPath, fileDialog)
    Spacer(modifier = Modifier.size(16.dp))
    val exists = remember { mutableStateOf(false) }
    val text = remember { mutableStateOf("") }
    LaunchedEffect(targetPath.value) {
        text.value = "开始安装【${File(targetPath.value).name}】"
        exists.value = (targetPath.value.endsWith(".apk") || targetPath.value.endsWith(".apex")) && File(targetPath.value).exists()
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(exists.value) {
            Column {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            installApk(installState, keepShellStore, text, targetPath)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    enabled = !installState.value
                ) {
                    AnimatedVisibility(installState.value) {
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

        AnimatedVisibility(!exists.value) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {},
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "未选择APK文件",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetPathEditor(targetPath: MutableState<String>, fileDialog: FileDialog) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = targetPath.value,
            onValueChange = { targetPath.value = it },
            label = {
                Text("输入APK文件路径")
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily(Font(R.font.jetBrainsMonoRegular))),
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = {
                fileDialog.filenameFilter = FilenameFilter { _, name -> (name.endsWith(".apk") || name.endsWith(".apex")) }
                fileDialog.mode = FileDialog.LOAD
                fileDialog.isVisible = true
                if (fileDialog.file?.endsWith(".apk") == true || fileDialog.file?.endsWith(".apex") == true) {
                    targetPath.value = fileDialog.directory + fileDialog.file
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

private fun installApk(
    installState: MutableState<Boolean>,
    keepShellStore: KeepShellStore,
    text: MutableState<String>,
    targetPath: MutableState<String>
) {
    installState.value = true
    text.value = "应用安装中，请稍候..."
    val out = keepShellStore adb "install \"${targetPath.value}\""
    if (out.contains("Success")) {
        sendNotice("安装成功！", "已将【${File(targetPath.value).name}】安装到设备【${DeviceName.value}】中")
    }else {
        sendNotice("安装失败！", out)
    }
    text.value = "开始安装【${File(targetPath.value).name}】"
    installState.value = false
}