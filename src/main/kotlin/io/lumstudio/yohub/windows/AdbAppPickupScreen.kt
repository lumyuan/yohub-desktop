package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.Apps
import com.konyaco.fluent.icons.regular.DrawerArrowDownload
import com.konyaco.fluent.icons.regular.Search
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.LocalKeepShell
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
    val keepShellStore = LocalKeepShell.current
    val outPath = remember { mutableStateOf("") }
    val jFileChooser by remember { mutableStateOf(JFileChooser()) }
    val filterCode = remember { mutableStateOf("") }
    val searchState = remember { mutableStateOf("") }
    val appList = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            OutputLayout(outPath, jFileChooser)
            Spacer(modifier = Modifier.size(28.dp))
            Text("应用列表", style = MaterialTheme.typography.labelSmall)
            FilterLayout(searchState, filterCode)
        }
        TipsLayout(appList)
        Spacer(modifier = Modifier.size(8.dp))
        ListLayout(keepShellStore, searchState, filterCode, appList, outPath)
    }
}

@Composable
private fun TipsLayout(appList: SnapshotStateList<String>) {
    if (appList.isNotEmpty()) {
        Text(
            "共发现${appList.size}款应用",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp).alpha(.8f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutputLayout(outPath: MutableState<String>, fileChooser: JFileChooser) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            label = {
                Text("输入保存安装包的路径")
            },
            value = outPath.value,
            onValueChange = { outPath.value = it },
            modifier = Modifier.fillMaxWidth().weight(1f)
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
            Text("选择文件夹")
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
            Text("打开文件管理器")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterLayout(searchState: MutableState<String>, filterCode: MutableState<String>) {
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
                Text("搜索应用（包名）")
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
            }
        )
        Spacer(modifier = Modifier.size(16.dp))
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
                Text("全部", style = MaterialTheme.typography.labelMedium)
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
                Text("系统", style = MaterialTheme.typography.labelMedium)
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
                Text("用户", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListLayout(
    keepShellStore: KeepShellStore,
    searchState: MutableState<String>,
    filterCode: MutableState<String>,
    appList: SnapshotStateList<String>,
    outPath: MutableState<String>
) {
    LaunchedEffect(filterCode.value) {
        withContext(Dispatchers.IO) {
            appList.clear()
            val out = keepShellStore adbShell "pm list packages -f ${filterCode.value} | cut -d ':' -f 2"
            appList.addAll(out.split("\n").toList())
        }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)) {
        appList.filter { it.lowercase().contains(searchState.value.trim().lowercase()) && it.contains("=") }
            .sortedBy { it.substring(it.lastIndexOf("=") + 1) }
            .onEach {
            val name = it.substring(it.lastIndexOf("=") + 1)
            val installPath = it.substring(0, it.lastIndexOf("="))
            item {
                FluentItem(
                    icon = {
                        Icon(Icons.Default.Apps, null, modifier = Modifier.fillMaxSize())
                    },
                    title = name,
                ) {
                    TooltipArea(
                        tooltip = {
                            TooltipText {
                                Text("提取【$name】")
                            }
                        }
                    ) {
                        var state by remember { mutableStateOf(true) }
                        AnimatedVisibility(
                            visible = state,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(
                                onClick = {
                                    val file = File(outPath.value)
                                    if (file.exists() && file.isDirectory) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            state = false
                                            val out = keepShellStore adb "pull \"$installPath\" \"${File(outPath.value, name).absolutePath}.apk\""
                                            println(out)
                                            if (out.contains("1 file pulled")) {
                                                sendNotice("提取成功！", out.substring(out.indexOf(":") + 1).trim())
                                            }else {
                                                sendNotice("提取失败！", out)
                                            }
                                            state = true
                                        }
                                    }else {
                                        sendNotice("提取失败！", "请检查保存安装包的路径是否正确！")
                                    }
                                },
                                enabled = state
                            ) {
                                Icon(Icons.Default.DrawerArrowDownload, null)
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