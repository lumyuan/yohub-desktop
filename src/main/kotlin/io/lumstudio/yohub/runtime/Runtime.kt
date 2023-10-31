package io.lumstudio.yohub.runtime

import androidx.compose.runtime.compositionLocalOf
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.utils.ZipUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.hostOs
import java.io.ByteArrayInputStream
import java.io.File

val LocalRuntime = compositionLocalOf<RuntimeStore> { error("Not provided.") }

class RuntimeStore(fileDir: File) {
    val runtimeFile: File by lazy {
        val file = File(fileDir, "runtime")
        if (!file.exists()) {
            file.mkdirs()
        }; file
    }
}

abstract class Runtime {
    suspend fun installRuntime(byteArray: ByteArray, installPath: String) {
        val inputStream = ByteArrayInputStream(byteArray)
        try {
            ZipUtils.unzip(inputStream, installPath)
            withContext(Dispatchers.IO) {
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

val LocalAdbRuntime = compositionLocalOf<AdbStore> { error("Not provided.") }

class AdbStore(runtimeDir: File, private val deviceStore: DeviceStore) : Runtime() {

    val adbHostFile: File by lazy {
        val file = File(runtimeDir, "adb")
        if (!file.exists()) {
            file.mkdirs()
        }; file
    }

    val resourceName: String by lazy {
        when {
            hostOs.isWindows -> R.raw.adbWin
            hostOs.isMacOS -> R.raw.adbMacOs
            else -> R.raw.adbLinux
        }
    }

    fun adbDevice(device: String?, cmd: String): String {
        val cmdBuilder = StringBuilder("$adbHostFile${File.separator}adb")
        if (!device.isNullOrEmpty()) {
            cmdBuilder.append(" -s $device")
        }
        cmdBuilder.append(" $cmd")
        return cmdBuilder.toString()
    }

    infix fun adb(cmd: String): String = adbDevice(deviceStore.device?.id, cmd)

    fun fastbootDevice(device: String?, cmd: String): String {
        val cmdBuilder = StringBuilder("$adbHostFile${File.separator}fastboot")
        if (!device.isNullOrEmpty()) {
            cmdBuilder.append(" -s $device")
        }
        cmdBuilder.append(" $cmd")
        return cmdBuilder.toString()
    }

    infix fun fastboot(cmd: String): String = fastbootDevice(deviceStore.device?.id, cmd)
}

val LocalPythonRuntime = compositionLocalOf<PythonStore> { error("Not provided.") }

class PythonStore(runtimeDir: File) : Runtime() {

    val pythonHostFile: File by lazy {
        val file = File(runtimeDir, "python")
        if (!file.exists()) {
            file.mkdirs()
        }; file
    }

    val resourceName: String by lazy {
        when {
            hostOs.isWindows -> R.raw.pythonWin
            hostOs.isMacOS -> R.raw.pythonMacOs
            else -> R.raw.pythonLinux
        }
    }

    infix fun py(cmd: String): String = "$pythonHostFile${File.separator}python $cmd"
}

val LocalPayloadDumperRuntime = compositionLocalOf<PayloadDumperStore> { error("Not provided.") }

class PayloadDumperStore(runtimeDir: File) : Runtime() {

    val payloadHostFile: File by lazy {
        val file = File(runtimeDir, "payload")
        if (!file.exists()) {
            file.mkdirs()
        }; file
    }

    val resourceName: String by lazy {
        when {
            hostOs.isWindows -> R.raw.payloadDumperWin
            hostOs.isMacOS -> R.raw.payloadDumperMacOs
            else -> R.raw.payloadDumperLinux
        }
    }

    infix fun payload(cmd: String): String = "$payloadHostFile${File.separator}payload_dumper $cmd"
}

val LocalMagiskPatcherRuntime = compositionLocalOf<MagiskPatcherStore> { error("Not provided.") }

class MagiskPatcherStore(runtimeDir: File) : Runtime() {

    val magiskPatcherHostFile: File by lazy {
        val file = File(runtimeDir, "magisk")
        if (!file.exists()) {
            file.mkdirs()
        }; file
    }

    val resourceName: String by lazy {
        when {
            hostOs.isWindows -> R.raw.magiskPatcherWin
            hostOs.isMacOS -> R.raw.magiskPatcherMacOs
            else -> R.raw.magiskPatcherLinux
        }
    }

    infix fun script(cmd: String): String = "$magiskPatcherHostFile${File.separator}$cmd"
}

val LocalFastbootDriverRuntime = compositionLocalOf<FastbootDriverStore> { error("Not provided.") }

class FastbootDriverStore(runtimeDir: File, private val keepShellStore: KeepShellStore) : Runtime() {

    val fastbootDriverHostFile: File by lazy {
        val file = File(runtimeDir, "driver")
        if (!file.exists()) {
            file.mkdirs()
        }; file
    }

    val resourceName: String by lazy {
        when {
            hostOs.isWindows -> R.raw.fastbootDriverWin
            hostOs.isMacOS -> R.raw.fastbootDriverMacOs
            else -> R.raw.fastbootDriverLinux
        }
    }

    infix fun path(cmd: String): String = "$fastbootDriverHostFile${File.separator}$cmd"

    suspend fun install() = withContext(Dispatchers.IO) {
        keepShellStore admin path("install")
    }
}