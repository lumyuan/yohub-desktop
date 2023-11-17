package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import com.konyaco.fluent.icons.regular.FolderSwap
import com.konyaco.fluent.icons.regular.Search
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.common.utils.LocalPreferences
import io.lumstudio.yohub.common.utils.PreferencesName
import io.lumstudio.yohub.lang.LanguageBasic
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

enum class RootState {
    CHECKING, SU, SH
}

@Composable
fun ColumnScope.RootScaffold(
    content: @Composable () -> Unit
) {
    val languageBasic = LocalLanguageType.value.lang
    val hasRootPermission = remember { mutableStateOf(RootState.CHECKING) }
    val keepShellStore = LocalKeepShell.current
    val preferencesStore = LocalPreferences.current
    val retry = remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit, retry.value) {
        withContext(Dispatchers.IO) {
            hasRootPermission.value = RootState.CHECKING
            val rootCode = try {
                RootCode.valueOf(preferencesStore.preference[PreferencesName.ROOT_CODE.toString()] ?: "")
            } catch (e: Exception) {
                RootCode.SU
            }
            val out = keepShellStore adbShell "${rootCode.value} -c ls /system"
            hasRootPermission.value =
                if (out.contains("No such file or directory") || out.contains("u: inaccessible or not found")) {
                    RootState.SH
                } else {
                    RootState.SU
                }
        }
    }

    AnimatedVisibility(
        hasRootPermission.value != RootState.CHECKING
    ) {
        when (hasRootPermission.value) {
            RootState.SU -> content()
            else -> NotRootLayout(languageBasic, retry)
        }
    }
    AnimatedVisibility(
        hasRootPermission.value == RootState.CHECKING
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(45.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(languageBasic.checkRootTip)
        }
    }
}

@Composable
fun ColumnScope.ImageBackupScreen() {
    RootScaffold {
        HasRootLayout()
    }
}

@Composable
private fun NotRootLayout(languageBasic: LanguageBasic, retry: MutableState<Long>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = {
                retry.value = System.currentTimeMillis()
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(languageBasic.notRootAndRetry)
        }
    }
}

@Stable
data class PartitionBean(
    val name: String,
    var selectable: Boolean = false,
)

@Composable
private fun HasRootLayout() {
    val languageBasic = LocalLanguageType.value.lang
    val keepShellStore = LocalKeepShell.current
    val androidKitStore = LocalAndroidToolkit.current
    val outPath = remember { mutableStateOf("") }
    val jFileChooser by remember { mutableStateOf(JFileChooser()) }
    val rootPath = remember { mutableStateOf("/dev/block/bootdevice/by-name/") }
    val partitions = remember { mutableStateListOf<PartitionBean>() }
    val filterText = remember { mutableStateOf("") }
    val loadState = remember { mutableStateOf(true) }
    val taskLog = remember { mutableStateListOf<String>() }

    Column(
        Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            OutputLayout(outPath, jFileChooser)
            Spacer(modifier = Modifier.size(16.dp))
            Text(languageBasic.partitionList, style = MaterialTheme.typography.labelMedium)
            FilterLayout(
                keepShellStore,
                filterText,
                languageBasic,
                partitions,
                taskLog,
                rootPath,
                outPath,
                androidKitStore
            )
        }
        TipsLayout(loadState, partitions)
        Spacer(modifier = Modifier.size(8.dp))
        ListLayout(keepShellStore, loadState, filterText, partitions)
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
                Text(lang.imageBackupPath)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun FilterLayout(
    keepShellStore: KeepShellStore,
    searchState: MutableState<String>,
    languageBasic: LanguageBasic,
    partitions: SnapshotStateList<PartitionBean>,
    taskLog: SnapshotStateList<String>,
    rootPath: MutableState<String>,
    outPath: MutableState<String>,
    androidKitStore: AndroidKitStore
) {
    val dialogState = remember { mutableStateOf(false) }
    val backupState = remember { mutableStateOf(false) }
    val actionState = remember { mutableStateOf(false) }
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
                Text(languageBasic.searchPartition)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            TooltipArea(
                tooltip = {
                    TooltipText {
                        Text(languageBasic.backupAllImage)
                    }
                }
            ) {
                TextButton(
                    onClick = {
                        val selectAll = partitions.isSelectAll()
                        partitions.toList().forEachIndexed { index, partitionBean ->
                            partitions[index] = partitionBean.copy(selectable = !selectAll)
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Checkbox(
                        checked = partitions.isSelectAll(),
                        onCheckedChange = null
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        if (partitions.isSelectAll()) {
                            languageBasic.selectNone
                        } else {
                            languageBasic.selectAll
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                enabled = !backupState.value,
                onClick = {
                    if (!File(outPath.value).exists()) {
                        sendNotice(languageBasic.backupFail, languageBasic.backupFolderIsNotExists)
                    } else if (partitions.selectCount() > 0) {
                        dialogState.value = true
                    } else {
                        sendNotice(languageBasic.backupFail, languageBasic.noSelectPartitionMessage)
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(languageBasic.textBackupPartitions)
            }
        }
    }

    Dialog(
        visible = dialogState.value,
        title = languageBasic.tips,
        content = {
            Text(String.format(languageBasic.confirmBackupImage, partitions.selectedItems().size))
        },
        confirmButtonText = languageBasic.defined,
        cancelButtonText = languageBasic.cancel,
        onCancel = {
            dialogState.value = false
        },
        onConfirm = {
            if (!File(outPath.value).exists()) {
                sendNotice(languageBasic.backupFail, languageBasic.backupFolderIsNotExists)
            } else if (partitions.selectCount() > 0) {
                backupState.value = true
                actionState.value = true
                CoroutineScope(Dispatchers.IO).launch {
                    backupTask(
                        keepShellStore,
                        taskLog,
                        actionState,
                        backupState,
                        rootPath,
                        outPath,
                        androidKitStore,
                        partitions.selectedItems()
                    )
                }
            } else {
                sendNotice(languageBasic.backupFail, languageBasic.noSelectPartitionMessage)
            }
            dialogState.value = false
        }
    )

    val coroutineScope = rememberCoroutineScope()
    Dialog(
        visible = backupState.value || actionState.value,
        title = languageBasic.backupTaskTitle,
        content = {
            Text(languageBasic.backupTaskMessage)
            Spacer(modifier = Modifier.size(16.dp))
            val state = rememberLazyListState()
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier.fillMaxWidth().height(210.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(200.dp).horizontalScroll(scrollState).weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                            .height(200.dp),
                        state = state
                    ) {
                        taskLog.toList().forEachIndexed { index, log ->
                            if (log.isNotEmpty()) {
                                item {
                                    SelectionContainer(
                                        modifier = Modifier.fillMaxWidth().weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (index == taskLog.size - 1) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Checkbox(
                                                    checked = true,
                                                    onCheckedChange = null,
                                                    enabled = false
                                                )
                                            }
                                            Spacer(modifier = Modifier.size(16.dp))
                                            Text(log)
                                        }
                                    }
                                }
                            }
                            if (index == taskLog.size - 1) {
                                coroutineScope.launch { state.animateScrollToItem(index) }
                            }
                        }
                    }
                }
                if (!backupState.value) {
                    Scrollbar(
                        isVertical = true,
                        modifier = Modifier.fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(state),
                    )
                }
            }
            Scrollbar(
                isVertical = false,
                modifier = Modifier.fillMaxWidth(),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        },
        cancelButtonText = if (actionState.value && backupState.value) languageBasic.cancel else languageBasic.tryCancel,
        onCancel = {
            actionState.value = false
        }
    )
}

@Composable
private fun TipsLayout(loadState: MutableState<Boolean>, partitions: SnapshotStateList<PartitionBean>) {
    val lang = LocalLanguageType.value.lang
    if (partitions.isNotEmpty()) {
        Text(
            if (loadState.value) {
                String.format(lang.findPartitions, partitions.size, partitions.selectCount())
            } else {
                lang.loading
            },
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp).alpha(.8f)
        )
    }
}

@Composable
private fun ColumnScope.ListLayout(
    keepShellStore: KeepShellStore,
    loadState: MutableState<Boolean>,
    searchState: MutableState<String>,
    partitions: SnapshotStateList<PartitionBean>,
) {

    LaunchedEffect(Unit) {
        if (loadState.value) {
            loadState.value = false
            val out = keepShellStore adbShell "su -c ls -l /dev/block/bootdevice/by-name/ | awk '{print \$8}'"
            partitions.clear()
            val partitionBeans = ArrayList<PartitionBean>()
            out.split("\n").filter { it.trim().isNotEmpty() && !it.contains("userdata") }.sortedBy { it }
                .onEach {
                    partitionBeans.add(PartitionBean(it))
                }
            partitions.addAll(partitionBeans)
            loadState.value = true
        }
    }

    val lang = LocalLanguageType.value.lang
    AnimatedVisibility(loadState.value) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(start = 16.dp),
                state = scrollState
            ) {
                partitions.toList().forEachIndexed { index, partitionBean ->
                    if (partitionBean.name.replace(" ", "").lowercase().contains(
                            searchState.value.replace(" ", "").lowercase()
                        )
                    ) {
                        item {
                            FluentItem(
                                icon = {
                                    Icon(Icons.Default.FolderSwap, null, modifier = Modifier.fillMaxSize())
                                },
                                title = partitionBean.name
                            ) {
                                Checkbox(
                                    partitionBean.selectable,
                                    onCheckedChange = null,
                                    modifier = Modifier.clickable {
                                        partitions[index] = partitionBean.copy(selectable = !partitionBean.selectable)
                                    }
                                )
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

private fun List<PartitionBean>.isSelectAll(): Boolean {
    forEach {
        if (!it.selectable) {
            return false
        }
    }
    return true
}

private fun List<PartitionBean>.selectCount(): Int {
    var count = 0
    forEach {
        if (it.selectable) {
            count++
        }
    }
    return count
}

private fun List<PartitionBean>.selectedItems(): List<PartitionBean> = this.filter { it.selectable }

private suspend fun backupTask(
    keepShellStore: KeepShellStore,
    taskLog: SnapshotStateList<String>,
    actionState: MutableState<Boolean>,
    backupState: MutableState<Boolean>,
    rootPath: MutableState<String>,
    outPath: MutableState<String>,
    androidKitStore: AndroidKitStore,
    partitions: List<PartitionBean>
) = withContext(Dispatchers.IO) {
    val languageBasic = LocalLanguageType.value.lang
    val timeMillis = System.currentTimeMillis()
    taskLog.clear()
    val tempPath = "${androidKitStore.androidToolkitPath}/backup"
    keepShellStore adbShell "mkdir -p $tempPath"
    var failCount = 0
    var completeCount = 0
    for ((index, partitionBean) in partitions.withIndex()) {
        if (!actionState.value) break
        taskLog.add("Task: backup [${partitionBean.name}] running...")
        val getPath = "$tempPath/${partitionBean.name}.img"
        val getCmd = "su -c dd if=${rootPath.value}${partitionBean.name} of=$getPath"
        val tempOut = keepShellStore adbShell getCmd
        if (tempOut.contains("Permission denied")) {
            taskLog[index] = "Task: backup [${partitionBean.name}] failed: Permission denied."
            failCount++
        } else {
            val msg = tempOut.split("\n").filter { it.contains("/s") }[0]
            taskLog[index] = "Task: backup [${partitionBean.name}] completed, $msg;"
            val copyOut = keepShellStore adb "pull \"$getPath\" \"${
                File(
                    outPath.value,
                    "${partitionBean.name}.img"
                ).absolutePath
            }\""
            if (copyOut.contains("1 file pulled")) {
                completeCount++
            } else {
                failCount++
            }
            taskLog[index] = taskLog[index] + " ${copyOut.replace("\n", "")}"
            keepShellStore adbShell "rm -rf $getPath"
        }
    }
    val time = (System.currentTimeMillis() - timeMillis) / 1000
    val formatSeconds = formatSeconds(time)
    if (!actionState.value) {
        sendNotice(
            languageBasic.backupImageCancel,
            String.format(languageBasic.backupImageCancelMessage, partitions.size, taskLog.size, formatSeconds)
        )
    } else {
        sendNotice(
            languageBasic.backupFinished,
            String.format(languageBasic.backupFinishedMessage, completeCount, failCount, partitions.size, formatSeconds)
        )
    }
    taskLog.add("")
    backupState.value = false
    actionState.value = false
    taskLog.clear()
}

private fun formatSeconds(seconds: Long): String {
    val hour = seconds / 3600
    val minute = (seconds % 3600) / 60
    val second = (seconds % 3600) % 60
    return String.format("%02d:%02d:%02d", hour, minute, second)
}