package io.lumstudio.yohub.runtime

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList

val LocalDevice = compositionLocalOf<DeviceStore> { error("not provided.") }

class DeviceStore {
    var device by mutableStateOf<Device?>(null)
}

val LocalDevices = compositionLocalOf<DevicesStore> { error("Not provided.") }

data class DevicesStore(var devices: SnapshotStateList<Device>)

@Stable
data class Device(
    val id: String,
    val state: ClientState,
    val type: ClientType,
)

enum class ClientState {
    DEVICE, UNAUTHORIZED, FASTBOOT, RECOVERY
}

enum class ClientType {
    ADB, ADB_AB, ADB_VAB, FASTBOOT, UNKNOWN
}