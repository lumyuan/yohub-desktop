package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.*
import io.lumstudio.yohub.common.LocalIOCoroutine
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.lang.LocalLanguageType
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
    val lang = LocalLanguageType.value.lang
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
                    Text(String.format(lang.driverState, if (driverStore.isInstall) lang.normal else lang.exception))
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
                var label by remember { mutableStateOf(lang.notChooseDevice) }
                var sub by remember { mutableStateOf("") }
                val device = selectDevice

                if (devicesStore.devices.isEmpty()) {
                    label = lang.unlinkDevice
                    sub = ""
                } else if (device == null) {
                    label = lang.notChooseDevice
                    sub = ""
                } else {
                    label = String.format(lang.linkedDevice, DeviceName.value)
                    sub = String.format(lang.deviceType, device.type)
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
                        && (selectDevice.type == ClientType.ADB_AB || selectDevice.type == ClientType.ADB_VAB)
                        && selectDevice.state != ClientState.UNAUTHORIZED
            ) {
                val slot = remember { mutableStateOf("") }
                LaunchedEffect(slot) {
                    slot.value = (keepShellStore adbShell "getprop ro.boot.slot_suffix").replace("\n", "").replace(" ", "")
                }
                if (slot.value.isNotEmpty()) {
                    FlowButton(
                        onClick = {  },
                        icon = {
                            Icon(Icons.Default.Phone, null, modifier = Modifier.fillMaxSize())
                        }
                    ) {
                        Text(String.format(lang.deviceSlot, slot.value.uppercase()))
                    }
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
                        contentText = String.format(lang.dialogRebootDevice, selectDevice?.id)
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adbShell "\"/system/bin/svc power reboot || /system/bin/reboot\""
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text(lang.rebootDevice)
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
                        contentText = String.format(lang.dialogShutdownDevice, selectDevice?.id)
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adbShell "\"/system/bin/svc power reboot p || /system/bin/reboot p\""
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text(lang.shutdownDevice)
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
                        contentText = String.format(lang.dialogRebootBootloaderDevice, selectDevice?.id)
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adbShell "\"/system/bin/svc power reboot bootloader || /system/bin/reboot bootloader\""
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text(lang.rebootBootloaderDevice)
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
                        contentText = String.format(lang.dialogRebootRecoveryDevide, selectDevice?.id)
                        onConfirm = {
                            ioCoroutine.ioScope.launch {
                                keepShellStore adbShell "\"/system/bin/svc power reboot recovery || /system/bin/reboot recovery\""
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Power, null)
                    }
                ) {
                    Text(lang.rebootRecoveryDevide)
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
                        contentText = String.format(lang.dialogRebootDevice, selectDevice?.id)
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
                    Text(lang.rebootDevice)
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
                        contentText = String.format(lang.dialogRebootRecoveryDevide, selectDevice?.id)
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
                    Text(lang.rebootRecoveryDevide)
                }
            }

            Dialog(
                title = lang.tips,
                visible = displayDialog,
                cancelButtonText = lang.cancel,
                confirmButtonText = lang.defined,
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