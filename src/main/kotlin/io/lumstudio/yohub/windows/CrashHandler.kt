package io.lumstudio.yohub.windows

import io.lumstudio.yohub.common.ContextStore
import io.lumstudio.yohub.lang.LanguageType
import io.lumstudio.yohub.main
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

object CrashHandler {

    val DEFAULT_UNCAUGHT_EXCEPTION_HANDLER: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    const val EXCEPTION_TAG = "EXCEPTION_TAG"

    fun init(context: ContextStore, languageType: LanguageType, crashDir: String? = null) {
        Thread.setDefaultUncaughtExceptionHandler(object : Thread.UncaughtExceptionHandler {
            override fun uncaughtException(thread: Thread, throwable: Throwable) {
                try {
                    tryUncaughtException(thread, throwable)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    DEFAULT_UNCAUGHT_EXCEPTION_HANDLER?.uncaughtException(
                        thread,
                        throwable
                    )
                }
            }

            private fun tryUncaughtException(thread: Thread, throwable: Throwable) {
              val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

                val crash = if (crashDir.isNullOrEmpty()) File(context.fileDir, "/log")
                else File(crashDir)
                if (!crash.exists()) {
                    val mkdirs = crash.mkdirs()
                    println(mkdirs)
                }
                val crashFile = File(crash.absolutePath, "crash_$time.txt")

                val versionName = context.versionTag

                val sw = StringWriter()
                val pw = PrintWriter(sw)

                throwable.printStackTrace(pw)

                val fullStackTrace = sw.toString()

                pw.close()

                throwable.printStackTrace()

                val errorLog =
                    """************* ${languageType.lang.appHasError} ****************
Time Of Crash      : $time
App VersionName    : $versionName
Developer QQ：2205903933
************* ${languageType.lang.appHasError} ****************

$fullStackTrace"""
                try {

                } catch (e: Exception) {
                    e.printStackTrace()
                    DEFAULT_UNCAUGHT_EXCEPTION_HANDLER?.uncaughtException(
                        thread,
                        throwable
                    )
                }
            }
        })
    }
}