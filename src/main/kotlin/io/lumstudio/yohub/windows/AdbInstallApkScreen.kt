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
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.lang.LocalLanguageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.io.File
import java.io.FilenameFilter
import javax.swing.JFrame

@Composable
fun AdbInstallApkScreen() {
    val scrollState = rememberScrollState()
    ScrollbarContainer(
        adapter = rememberScrollbarAdapter(scrollState),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(scrollState)
                .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
        ) {
            InstallLayout()
        }
    }
}

@Composable
private fun InstallLayout() {
    val languageBasic = LocalLanguageType.value.lang
    val keepShellStore = LocalKeepShell.current
    val window = LocalWindowMain.current

    val installState = remember { mutableStateOf(false) }
    val targetPath = remember { mutableStateOf("") }
    val fileDialog by remember { mutableStateOf(FileDialog(window)) }
    TargetPathEditor(targetPath, fileDialog)
    Spacer(modifier = Modifier.size(16.dp))
    val exists = remember { mutableStateOf(false) }
    val text = remember { mutableStateOf("") }
    LaunchedEffect(targetPath.value) {
        text.value = String.format(languageBasic.startInstall, File(targetPath.value).name)
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
                    languageBasic.notChooseApk,
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
    val languageBasic = LocalLanguageType.value.lang
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = targetPath.value,
            onValueChange = { targetPath.value = it },
            label = {
                Text(languageBasic.inputApkPath)
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
                    sendNotice(languageBasic.chooseFail, String.format(languageBasic.chooseFailMessage, fileDialog.file))
                }
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(languageBasic.chooseFile)
        }
    }
}

private fun installApk(
    installState: MutableState<Boolean>,
    keepShellStore: KeepShellStore,
    text: MutableState<String>,
    targetPath: MutableState<String>
) {
    val languageBasic = LocalLanguageType.value.lang
    installState.value = true
    text.value = languageBasic.apkInstalling
    val out = keepShellStore adb "install \"${targetPath.value}\""
    if (out.contains("Success")) {
        sendNotice(languageBasic.installSuccess, String.format(languageBasic.installSuccessMessage, File(targetPath.value).name, DeviceName.value))
    }else {
        sendNotice(languageBasic.installFail, out)
    }
    text.value = String.format(languageBasic.startInstall, File(targetPath.value).name)
    installState.value = false
}