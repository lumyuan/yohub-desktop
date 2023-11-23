package io.lumstudio.yohub.common.shell

import androidx.compose.runtime.compositionLocalOf
import io.lumstudio.yohub.runtime.AdbStore
import io.lumstudio.yohub.runtime.MagiskPatcherStore
import io.lumstudio.yohub.runtime.PayloadDumperStore

/**
 * Created by Hello on 2018/01/23.
 */

val LocalKeepShell = compositionLocalOf<KeepShellStore> { error("Not provided.") }

class KeepShellStore(
    private val workPath: String,
    private val adbStore: AdbStore,
    private val magiskPatcherStore: MagiskPatcherStore
) {
    private val keepShells = HashMap<String, KeepShell>()

    fun getInstance(key: String): KeepShell {
        synchronized(keepShells) {
            if (!keepShells.containsKey(key)) {
                keepShells[key] = KeepShell(workPath)
            }
            return keepShells[key]!!
        }
    }

    fun destroyInstance(key: String) {
        synchronized(keepShells) {
            if (!keepShells.containsKey(key)) {
                return
            } else {
                val keepShell = keepShells.get(key)!!
                keepShells.remove(key)
                keepShell.tryExit()
            }
        }
    }

    fun destroyAll() {
        synchronized(keepShells) {
            while (keepShells.isNotEmpty()) {
                val key = keepShells.keys.first()
                val keepShell = keepShells.get(key)!!
                keepShells.remove(key)
                keepShell.tryExit()
            }
        }
    }

    val defaultKeepShell = KeepShell(workPath)
    val secondaryKeepShell = KeepShell(workPath)

    fun getDefaultInstance(): KeepShell {
        return if (defaultKeepShell.isIdle || !secondaryKeepShell.isIdle) {
            defaultKeepShell
        } else {
            secondaryKeepShell
        }
    }

    fun doCmdSync(commands: List<String>): Boolean {
        val stringBuilder = StringBuilder()

        for (cmd in commands) {
            stringBuilder.append(cmd)
            stringBuilder.append("\n\n")
        }

        return doCmdSync(stringBuilder.toString()) != "error"
    }

    //执行脚本
    fun doCmdSync(cmd: String): String {
        return getDefaultInstance().doCmdSync(cmd)
    }

    fun tryExit() {
        defaultKeepShell.tryExit()
        secondaryKeepShell.tryExit()
    }

    infix fun cmd(cmd: String) = doCmdSync(cmd)
    infix fun admin(cmd: String) = doCmdSync("powershell -Command \"Start-Process -Verb RunAs -FilePath '$cmd'\"")
    infix fun adb(cmd: String) = doCmdSync(adbStore adb cmd)
    infix fun adbShell(cmd: String) = doCmdSync(adbStore adb "shell \"$cmd\"")
    infix fun fastboot(cmd: String) = doCmdSync(adbStore fastboot cmd)

    infix fun magiskPatcher(cmd: String) = getInstance("magisk-patcher").let { it.doCmdSync("cd ${magiskPatcherStore.magiskPatcherHostFile.absolutePath}"); it.doCmdSync(cmd) }
}
