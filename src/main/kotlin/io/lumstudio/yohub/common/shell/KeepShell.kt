package io.lumstudio.yohub.common.shell

import org.jetbrains.skiko.hostOs
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.util.concurrent.locks.ReentrantLock


/**
 * Created by Hello on 2018/01/23.
 */
class KeepShell(private val workPath: String) {
    private var p: Process? = null
    private var out: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private var currentIsIdle = true
    val isIdle: Boolean
        get() {
            return currentIsIdle
        }

    fun tryExit() {
        try {
            if (out != null)
                out?.close()
            if (reader != null)
                reader?.close()
        } catch (ex: Exception) {
        }
        try {
            p?.destroy()
        } catch (ex: Exception) {
        }
        enterLockTime = 0L
        out = null
        reader = null
        p = null
        currentIsIdle = true
    }

    private val GET_ROOT_TIMEOUT = 20000L
    private val mLock = ReentrantLock()
    private val LOCK_TIMEOUT = 10000L
    private var enterLockTime = 0L

    private fun getRuntimeShell() {
        if (p != null) return
        val getSu = Thread {
            try {
                mLock.lockInterruptibly()
                enterLockTime = System.currentTimeMillis()
                p = run {
                    val processBuilder = when {
                        hostOs.isWindows -> ProcessBuilder("cmd", "/k")
                        else -> ProcessBuilder("sh")
                    }
                    processBuilder.directory(File(workPath))
                    processBuilder.redirectErrorStream(true)
                    processBuilder.start()
                }
                out = p?.outputWriter()
                reader = p?.inputReader()
                Thread {
                    try {
                        var line: String?
                        val errorReader = p?.errorReader()
                        while (errorReader?.readLine().also { line = it } != null) {
                            println("KeepShellPublic-ERROR: $line")
                        }
                    } catch (ex: Exception) {
                        println("c: " + ex.message)
                    }
                }.start()
            } catch (ex: Exception) {
                println("getRuntime: " + ex.message)
            } finally {
                enterLockTime = 0L
                mLock.unlock()
            }
        }
        getSu.start()
        getSu.join(10000)
        if (p == null && getSu.state != Thread.State.TERMINATED) {
            enterLockTime = 0L
            getSu.interrupt()
        }
    }

    private var br = "\n\n"

    private val shellOutputCache = StringBuilder()
    private val startTag = "SH_START"
    private val endTag = "SH_END"
    private val startTagBytes = "ECHO $startTag"
    private val endTagBytes = "ECHO $endTag"

    fun doCmdSync(cmd: String): String {
        if (mLock.isLocked && enterLockTime > 0 && System.currentTimeMillis() - enterLockTime > LOCK_TIMEOUT) {
            tryExit()
            println("doCmdSync-Lock: " + "${System.currentTimeMillis()} - $enterLockTime > $LOCK_TIMEOUT")
        }
        getRuntimeShell()
        try {
            mLock.lockInterruptibly()
            currentIsIdle = false

            out?.run {
                when {
                    hostOs.isWindows -> write("$startTagBytes & $cmd & $endTagBytes\n")
                    else -> {
                        write("$startTagBytes\n")
                        write("$cmd\n")
                        write("$endTagBytes\n")
                    }
                }
                flush()
            }

            var line: String?
            while (reader?.readLine().also { line = it } != null) {
//                println(line)
                if (line?.contains(workPath) == true)
                    continue
                if (line?.contains(startTag) == true) {
                    shellOutputCache.clear()
                } else if (line?.contains(endTag) == true) {
                    shellOutputCache.append(line?.substring(0, line?.indexOf(endTag) ?: 0))
                    break
                } else {
                    if (line?.contains(startTag) == false && line?.contains(endTag) == false) {
                        shellOutputCache.append(line).append("\n")
                    }
                }
            }
            return shellOutputCache.toString().replace("\t", "\t")
        } catch (e: Exception) {
            tryExit()
            println("KeepShellAsync: " + e.message)
            return "error"
        } finally {
            enterLockTime = 0L
            mLock.unlock()
            currentIsIdle = true
        }
    }
}
