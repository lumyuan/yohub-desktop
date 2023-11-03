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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.Table
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.runtime.ClientState
import io.lumstudio.yohub.runtime.LocalDevice
import io.lumstudio.yohub.ui.component.Dialog
import io.lumstudio.yohub.ui.component.FluentItem
import io.lumstudio.yohub.ui.component.Toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.io.File
import java.io.FilenameFilter
import javax.swing.JFrame

@Composable
fun FlashImageScreen(flashImagePage: FlashImagePage) {
    val deviceStore = LocalDevice.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            deviceStore.device?.state != ClientState.FASTBOOT,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Toolbar(flashImagePage.label)
                flashPages[0].content()
            }
        }
        AnimatedVisibility(deviceStore.device?.state == ClientState.FASTBOOT) {
            ScrollbarContainer(
                adapter = rememberScrollbarAdapter(scrollState),
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().verticalScroll(scrollState).padding(16.dp)
                ) {
                    Toolbar(flashImagePage.label)
                    flashPages[1].content()
                }
            }
        }
    }
}

private val flashPages by lazy {
    arrayListOf(
        UnlinkPage(),
        LinkedPage()
    )
}

class UnlinkPage : NavPage("未连接Fastboot设备", title = "请连接Fastboot设备") {
    @Composable
    override fun content() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$title",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(28.dp)
            )
        }
    }

}

@Stable
data class Partition(
    var name: String
)

class LinkedPage : NavPage("镜像刷写") {
    @Composable
    override fun content() {
        val keepShellStore = LocalKeepShell.current
        val bootPath = remember { mutableStateOf("") }
        val filter = remember { mutableStateOf(true) }
        val partitionName = remember { mutableStateOf("") }
        val partitionList = remember { mutableStateOf(ArrayList<Partition>()) }
        val flashState = remember { mutableStateOf(false) }
        val fileDialog = remember { FileDialog(JFrame()) }
        val text = remember { mutableStateOf("输入镜像") }
        var displayDialog by remember { mutableStateOf(false) }
        TargetPathEditor(bootPath, fileDialog)
        Spacer(modifier = Modifier.size(16.dp))
        PartitionChooser(partitionName, filter, partitionList, keepShellStore)
        loadPartition(filter, partitionList, keepShellStore)
        Column {
            AnimatedVisibility(partitionName.value.isNotBlank() && bootPath.value.endsWith(".img") && File(bootPath.value).exists()) {
                Column {
                    Spacer(modifier = Modifier.size(28.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            displayDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !flashState.value
                    ) {
                        AnimatedVisibility(flashState.value) {
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
        }
        Dialog(
            title = "确认刷写",
            visible = displayDialog,
            cancelButtonText = "取消",
            confirmButtonText = "确定",
            onCancel = {
                displayDialog = false
            },
            onConfirm = {
                displayDialog = false
                CoroutineScope(Dispatchers.IO).launch {
                    flashTask(flashState, text, keepShellStore, bootPath, partitionName)
                }
            },
            content = {
                Text("是否执行镜像刷写任务？\n\n已选择文件：${File(bootPath.value).name}\n已选择分区：${partitionName.value}")
            }
        )
    }
}

private fun loadPartition(
    filter: MutableState<Boolean>,
    partitionList: MutableState<ArrayList<Partition>>,
    keepShellStore: KeepShellStore
) {
    CoroutineScope(Dispatchers.IO).launch {
        val list = ArrayList<Partition>()
        (keepShellStore fastboot "getvar all")
            .split("\n")
            .filter { it.contains("partition-type") }
            .sortedBy { it }
            .onEach {
                val start = "partition-type:"
                val end = ":"
                var name = it.substring(it.indexOf(start) + start.length, it.lastIndexOf(end))
                if (filter.value) {
                    if (!name.endsWith("_b")) {
                        if (name.endsWith("_a")) {
                            name = name.substring(0, name.lastIndexOf("_"))
                        }
                        list.add(Partition(name))
                    }
                } else {
                    list.add(Partition(name))
                }
            }
        partitionList.value = list
    }
}

private fun flashTask(
    flashState: MutableState<Boolean>,
    text: MutableState<String>,
    keepShellStore: KeepShellStore,
    bootPath: MutableState<String>,
    partitionName: MutableState<String>
) {
    val timeMillis = System.currentTimeMillis()
    flashState.value = true
    text.value = "刷写任务执行中，请稍候..."
    val out = keepShellStore fastboot "flash ${partitionName.value} ${bootPath.value}"
    val end = out.split("\n").last { it.trim().isNotEmpty() }
    if (out.uppercase().contains("OKAY") && out.contains("Finished")) {
        sendNotice("刷写成功！", "总耗时：${String.format("%.2f", (System.currentTimeMillis() - timeMillis).toFloat() / 1000f)}秒")
    }else {
        sendNotice("刷写失败！", end)
    }
    flashState.value = false
    text.value = "输入镜像"
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
                Text("输入镜像文件路径")
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
                } else {
                    sendNotice("选择失败", "不受支持的文件类型：${fileDialog.file}")
                }
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("选择文件")
        }
    }
}

@Composable
private fun PartitionChooser(
    partitionName: MutableState<String>,
    filter: MutableState<Boolean>,
    partitionList: MutableState<ArrayList<Partition>>,
    keepShellStore: KeepShellStore
) {
    var dropdownMenuState by remember { mutableStateOf(false) }
    Column {
        FluentItem(
            icon = Icons.Default.Table,
            title = if (partitionName.value.isBlank()) "选择要刷入的分区" else "已选择分区：${partitionName.value}"
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        filter.value = !filter.value
                        loadPartition(filter, partitionList, keepShellStore)
                    }
            ) {
                Checkbox(
                    filter.value,
                    onCheckedChange = null,
                    enabled = false
                )
                Text("过滤A/B分区", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.size(28.dp))
            Column {
                Button(
                    onClick = {
                        dropdownMenuState = true
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("选择分区")
                }
                DropdownMenu(
                    dropdownMenuState,
                    onDismissRequest = {
                        dropdownMenuState = false
                    },
                    modifier = Modifier.size(250.dp, 200.dp),
                    offset = DpOffset(0.dp, 16.dp)
                ) {
                    partitionList.value.onEach {
                        DropdownMenuItem(
                            text = {
                                Text(it.name, style = MaterialTheme.typography.labelMedium)
                            },
                            onClick = {
                                partitionName.value = it.name
                                dropdownMenuState = false
                            }
                        )
                    }
                }
            }
        }
    }
}