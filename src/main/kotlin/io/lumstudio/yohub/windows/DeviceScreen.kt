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
import io.lumstudio.yohub.ui.component.FlowButton
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
                hostOs.isWindows -> FlowButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            fastbootDriverStore.install()
                        }
                    },
                    icon = {
                        Icon(Icons.Default.DeviceEq, null, modifier = Modifier.fillMaxSize())
                    }
                ) {
                    Text("驱动状态：${if (driverStore.isInstall) "正常" else "异常（点击修复）"}")
                }
            }

            FlowButton(
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
                    label = "已连接：${DeviceName.value}"
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
                FlowButton(
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
                FlowButton(
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
                FlowButton(
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
                FlowButton(
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
                FlowButton(
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
                FlowButton(
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