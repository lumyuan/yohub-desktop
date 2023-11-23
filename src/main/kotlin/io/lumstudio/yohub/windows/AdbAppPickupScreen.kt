package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.component.Scrollbar
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.*
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.common.shell.MemoryUtil
import io.lumstudio.yohub.lang.LocalLanguageType
import io.lumstudio.yohub.runtime.AndroidKitStore
import io.lumstudio.yohub.runtime.LocalAndroidToolkit
import io.lumstudio.yohub.ui.component.Dialog
import io.lumstudio.yohub.ui.component.FluentItem
import io.lumstudio.yohub.ui.component.TooltipText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import javax.swing.JFileChooser

@Composable
fun AdbAppPickupScreen() {
    val lang = LocalLanguageType.value.lang
    val keepShellStore = LocalKeepShell.current
    val androidKitStore = LocalAndroidToolkit.current
    val outPath = remember { mutableStateOf("") }
    val jFileChooser by remember { mutableStateOf(JFileChooser()) }
    val filterCode = remember { mutableStateOf("") }
    val searchState = remember { mutableStateOf("") }
    val appList = remember { mutableStateListOf<AppInfo>() }

    val loadState = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            OutputLayout(outPath, jFileChooser)
            Spacer(modifier = Modifier.size(28.dp))
            Text(lang.appList, style = MaterialTheme.typography.labelSmall)
            FilterLayout(loadState, searchState, filterCode)
        }
        TipsLayout(loadState, appList)
        Spacer(modifier = Modifier.size(8.dp))
        ListLayout(loadState, keepShellStore, androidKitStore, searchState, filterCode, appList, outPath)
    }
}

@Composable
private fun TipsLayout(loadState: MutableState<Boolean>, appList: SnapshotStateList<AppInfo>) {
    val lang = LocalLanguageType.value.lang
    if (appList.isNotEmpty()) {
        Text(
            if (loadState.value) {
                String.format(lang.findApps, appList.size)
            } else {
                lang.loading
            },
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp).alpha(.8f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutputLayout(outPath: MutableState<String>, fileChooser: JFileChooser) {
    val lang = LocalLanguageType.value.lang
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            label = {
                Text(lang.inputSaveApkPath)
            },
            value = outPath.value,
            onValueChange = { outPath.value = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.size(16.dp))
        TextButton(
            onClick = {
                fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                val result = fileChooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    outPath.value = fileChooser.selectedFile.absolutePath
                }
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(lang.chooseDir)
        }
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = {
                try {
                    Desktop.getDesktop().open(File(outPath.value))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(lang.openFileManager)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterLayout(
    loadState: MutableState<Boolean>,
    searchState: MutableState<String>,
    filterCode: MutableState<String>
) {
    val lang = LocalLanguageType.value.lang
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchState.value,
            onValueChange = { searchState.value = it },
            leadingIcon = {
                Icon(Icons.Default.Search, null)
            },
            label = {
                Text(lang.searchApps)
            },
            trailingIcon = {
                if (searchState.value.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchState.value = ""
                        }
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Close, null)
                    }
                }
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.size(16.dp))
        AnimatedVisibility(loadState.value) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.wrapContentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            filterCode.value = ""
                        }.padding(8.dp)
                ) {
                    RadioButton(
                        selected = filterCode.value == "",
                        onClick = null
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(lang.all, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.wrapContentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            filterCode.value = "-s"
                        }.padding(8.dp)
                ) {
                    RadioButton(
                        selected = filterCode.value == "-s",
                        onClick = null
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(lang.system, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.wrapContentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            filterCode.value = "-3"
                        }.padding(8.dp)
                ) {
                    RadioButton(
                        selected = filterCode.value == "-3",
                        onClick = null
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(lang.user, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

data class AppInfo(
    val label: String,
    val packageName: String,
    val versionName: String,
    val versionCode: String,
    val sdkVersion: String,
    val targetSdkVersion: String,
    val installPath: String,
    val size: Long,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnScope.ListLayout(
    loadState: MutableState<Boolean>,
    keepShellStore: KeepShellStore,
    androidKitStore: AndroidKitStore,
    searchState: MutableState<String>,
    filterCode: MutableState<String>,
    appList: SnapshotStateList<AppInfo>,
    outPath: MutableState<String>
) {
    val lang = LocalLanguageType.value.lang
    LaunchedEffect(filterCode.value) {

        if (loadState.value) {
            withContext(Dispatchers.IO) {
                loadState.value = false
                val arrayList = ArrayList<AppInfo>()

                val appInfo = String(
                    (keepShellStore adbShell "sh \"${androidKitStore.androidToolkitPath}/appinfo.sh ${filterCode.value}\"").toByteArray(),
                    Charsets.UTF_8
                )

                appInfo.split("\n").filter { it.contains("<tb>") }
                    .onEach {
                        try {
                            val table = it.split("<tb>")
                            var label = ""
                            var packageName = ""
                            var versionName = ""
                            var versionCode = ""
                            var sdkVersion = ""
                            var targetSdkVersion = ""
                            val installPath = table[2]
                            val size: Long = try {
                                table[6].toLong()
                            } catch (e: Exception) {
                                -1L
                            }
                            val packageInfo = table[3].substring(table[3].indexOf(":") + 2).split("' ")
                            packageInfo.onEach { pi ->
                                when {
                                    pi.contains("name=") -> {
                                        packageName = pi.substring(pi.indexOf("'") + 1)
                                    }

                                    pi.contains("versionCode=") -> {
                                        versionCode = pi.substring(pi.indexOf("'") + 1)
                                    }

                                    pi.contains("versionName=") -> {
                                        versionName = pi.substring(pi.indexOf("'") + 1)
                                    }
                                }
                            }
                            label = if (table[1].contains("label")) {
                                table[1].substring(table[1].indexOf("'") + 1, table[1].lastIndexOf("'"))
                            } else if (table[0].contains("label")) {
                                table[0].substring(table[0].indexOf("'") + 1, table[0].lastIndexOf("'"))
                            } else {
                                packageName
                            }
                            sdkVersion = table[4].substring(table[4].indexOf("'") + 1, table[4].lastIndexOf("'"))
                            targetSdkVersion = table[5].substring(table[5].indexOf("'") + 1, table[5].lastIndexOf("'"))
                            val element =
                                AppInfo(
                                    label,
                                    packageName,
                                    versionName,
                                    versionCode,
                                    sdkVersion,
                                    targetSdkVersion,
                                    installPath,
                                    size
                                )
                            arrayList.add(element)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                appList.clear()
                appList.addAll(arrayList.sortedBy { it.label })
                loadState.value = true
            }
        }
    }

    AnimatedVisibility(loadState.value) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            val scrollState = rememberLazyListState()
            val dialog = remember { mutableStateOf(false) }
            val app = remember { mutableStateOf(AppInfo("", "", "", "", "", "", "", 0)) }
            val selectIndex = remember { mutableStateOf(-1) }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(start = 16.dp, end = 16.dp),
                state = scrollState
            ) {
                appList.toList().forEachIndexed { index, it ->
                    if (it.label.lowercase()
                            .replace(" ", "")
                            .contains(
                                searchState.value.replace(" ", "")
                                    .lowercase()
                            )
                        || it.packageName.lowercase()
                            .replace(" ", "")
                            .contains(
                                searchState.value.replace(" ", "")
                                    .lowercase()
                            )
                    ) {
                        item {
                            FluentItem(
                                softWrap = true,
                                selectable = true,
                                icon = {
                                    Icon(Icons.Default.Apps, null, modifier = Modifier.fillMaxSize())
                                },
                                title = it.label + if (it.installPath.contains("/disabled_package/")) {
                                    lang.appDisabled
                                } else "",
                                subtitle = String.format(
                                    lang.appInfoFormat,
                                    it.packageName,
                                    MemoryUtil.format(it.size),
                                    "${it.versionName}(${it.versionCode})",
                                    it.targetSdkVersion
                                )
                            ) {

                                val open = remember { mutableStateOf(false) }
                                var state by remember { mutableStateOf(true) }
                                val androidState = LocalAndroidState.current

                                TooltipArea(
                                    tooltip = {
                                        TooltipText {
                                            Text(lang.labelSettings)
                                        }
                                    }
                                ) {
                                    AnimatedVisibility(
                                        visible = state,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        Column {
                                            IconButton(
                                                onClick = {
                                                    open.value = true
                                                }
                                            ) {
                                                Icon(Icons.Default.ArrowRight, null)
                                            }
                                            DropdownMenu(
                                                expanded = open.value,
                                                onDismissRequest = { open.value = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(lang.textPickup)
                                                    },
                                                    onClick = {
                                                        open.value = false
                                                        val file = File(outPath.value)
                                                        if (file.exists() && file.isDirectory) {
                                                            CoroutineScope(Dispatchers.IO).launch {
                                                                state = false
                                                                val out =
                                                                    keepShellStore adb "pull \"${it.installPath}\" \"${
                                                                        File(
                                                                            outPath.value,
                                                                            it.label
                                                                        ).absolutePath
                                                                    }.apk\""
                                                                if (out.contains("1 file pulled")) {
                                                                    sendNotice(
                                                                        lang.noticePickAppSuccess,
                                                                        out.substring(out.indexOf(":") + 1).trim()
                                                                    )
                                                                } else {
                                                                    sendNotice(lang.noticePickAppFail, out)
                                                                }
                                                                state = true
                                                            }
                                                        } else {
                                                            sendNotice(
                                                                lang.noticePickAppFail,
                                                                lang.noticeMessagePickAppFail
                                                            )
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(lang.textEnable)
                                                    },
                                                    onClick = {
                                                        open.value = false
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            val out =
                                                                keepShellStore adbShell "pm enable ${it.packageName}"
                                                            val out1 = if (androidState.sdk >= 34) {
                                                                keepShellStore adbShell "pm unsuspend ${it.packageName}"
                                                            } else {
                                                                "Package ${it.packageName} new suspended state"
                                                            }
                                                            if (out.contains("Package ${it.packageName} new state") && out1.contains(
                                                                    "Package ${it.packageName} new suspended state"
                                                                )
                                                            ) {
                                                                sendNotice(
                                                                    lang.noticeAppEnabled,
                                                                    String.format(
                                                                        lang.noticeAppEnabledMessage,
                                                                        it.label
                                                                    )
                                                                )
                                                            } else {
                                                                sendNotice(lang.noticeAppEnabledFail, "$out\n\n$out1")
                                                            }
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(lang.textDisable)
                                                    },
                                                    onClick = {
                                                        open.value = false
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            val out =
                                                                keepShellStore adbShell "pm ${if (androidState.sdk >= 34) "suspend" else "disable-user"} ${it.packageName}"
                                                            if (!out.contains("Package ${it.packageName} new state") || out.contains(
                                                                    "Package ${it.packageName} new suspended state"
                                                                )
                                                            ) {
                                                                sendNotice(
                                                                    lang.noticeAppDisabled,
                                                                    String.format(
                                                                        lang.noticeAppDisabledMessage,
                                                                        it.label
                                                                    )
                                                                )
                                                            } else {
                                                                sendNotice(lang.noticeAppDisabledFail, out)
                                                            }
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(lang.textUninstall)
                                                    },
                                                    onClick = {
                                                        app.value = it
                                                        selectIndex.value = index
                                                        open.value = false
                                                        dialog.value = true
                                                    }
                                                )

                                            }
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = !state,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Scrollbar(
                isVertical = true,
                modifier = Modifier.fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState),
            )
            Dialog(
                visible = dialog.value,
                title = lang.tips,
                content = {
                    Text(String.format(lang.dialogUninstallApp, DeviceName.value, app.value.label))
                },
                confirmButtonText = lang.defined,
                onConfirm = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val out = keepShellStore adb "uninstall ${app.value.packageName}"
                        if (out.contains("Success")) {
                            sendNotice(
                                lang.noticeAppUninstall,
                                String.format(lang.noticeAppUninstallMessage, app.value.label)
                            )
                            appList.removeAt(selectIndex.value)
                        } else {
                            sendNotice(lang.noticeAppUninstallFail, out)
                        }
                        dialog.value = false
                    }
                },
                cancelButtonText = lang.cancel,
                onCancel = {
                    dialog.value = false
                }
            )
        }
    }
    AnimatedVisibility(!loadState.value) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.size(8.dp))
            Text(lang.loading)
        }
    }

}