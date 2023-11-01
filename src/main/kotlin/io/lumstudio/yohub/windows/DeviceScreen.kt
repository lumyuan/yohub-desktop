package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.DeviceEq
import com.konyaco.fluent.icons.regular.Power
import io.lumstudio.yohub.common.LocalIOCoroutine
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.runtime.*
import io.lumstudio.yohub.ui.component.Dialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.skiko.hostOs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceScreen() {
    val driverStore = LocalDriver.current
    val fastbootDriverStore = LocalFastbootDriverRuntime.current
    val deviceStore = LocalDevice.current
    val devicesStore = LocalDevices.current
    val keepShellStore = LocalKeepShell.current
    val ioCoroutine = LocalIOCoroutine.current
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
    ) {
        val selectDevice = deviceStore.device
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            when {
                hostOs.isWindows -> InfoItem(
                    onClick = {
                        if (!driverStore.isInstall) {
                            CoroutineScope(Dispatchers.IO).launch {
                                fastbootDriverStore.install()
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.DeviceEq, null, modifier = Modifier.fillMaxSize())
                    }
                ) {
                    Text("驱动状态：${if (driverStore.isInstall) "正常" else "异常（点击修复）"}")
                }
            }

            InfoItem(
                onClick = {},
                icon = {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Android,
                        null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                var label by remember { mutableStateOf("未选中设备") }
                var sub by remember { mutableStateOf("") }
                val device = selectDevice

                if (devicesStore.devices.isEmpty()) {
                    label = "未连接设备"
                    sub = ""
                } else if (device == null) {
                    label = "未选中设备"
                    sub = ""
                } else {
                    label = "已连接：${device.id}"
                    sub = "设备类型：${device.type}"
                }

                Column {
                    Text(label)
                    if (sub.isNotEmpty()) {
                        Text(sub, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            var contentText by remember { mutableStateOf("") }
            var displayDialog by remember { mutableStateOf(false) }
            var onConfirm by remember { mutableStateOf({ }) }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.ADB
                        || selectDevice.type == ClientType.ADB_AB
                        || selectDevice.type == ClientType.ADB_VAB)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "确定要重启【${selectDevice?.id}】吗？"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adb "reboot"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("重启设备")
                }
            }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.ADB
                        || selectDevice.type == ClientType.ADB_AB
                        || selectDevice.type == ClientType.ADB_VAB)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "确定要将设备【${selectDevice?.id}】关机吗？"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adb "reboot p"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("设备关机")
                }
            }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.ADB
                        || selectDevice.type == ClientType.ADB_AB
                        || selectDevice.type == ClientType.ADB_VAB)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "确定要将【${selectDevice?.id}】重启到Bootloader吗？"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adb "reboot bootloader"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("重启至Bootloader")
                }
            }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.ADB
                        || selectDevice.type == ClientType.ADB_AB
                        || selectDevice.type == ClientType.ADB_VAB)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "确定要将【${selectDevice?.id}】重启到Recovery吗？"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adb "reboot recovery"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("重启至Recovery")
                }
            }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.FASTBOOT)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "确定要重启【${selectDevice?.id}】吗？"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore fastboot "reboot"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("重启设备")
                }
            }

            AnimatedVisibility(
                selectDevice != null
                        && selectDevice.state != ClientState.UNAUTHORIZED
                        && (selectDevice.type == ClientType.FASTBOOT)
            ) {
                InfoItem(
                    onClick = {
                        displayDialog = true
                        contentText = "确定要将【${selectDevice?.id}】重启到Recovery吗？"
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore fastboot "reboot recovery"
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text("重启至Recovery")
                }
            }

            Dialog(
                title = "提示",
                visible = displayDialog,
                cancelButtonText = "取消",
                confirmButtonText = "确定",
                onCancel = {
                    displayDialog = false
                },
                onConfirm = {
                    displayDialog = false
                    onConfirm()
                },
                content = {
                    Text(contentText)
                }
            )
        }
    }
}

@Composable
private fun InfoItem(
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.height(65.dp).padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(.5.dp, DividerDefaults.color.copy(alpha = .5f))
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) Box(Modifier.padding(start = 16.dp).size(28.dp), Alignment.Center) {
                icon()
            }
            Row(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 16.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content()
            }
        }
    }
}