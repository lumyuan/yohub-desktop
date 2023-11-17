package io.lumstudio.yohub.common

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skiko.hostOs
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.Paths

val LocalContext = compositionLocalOf<ContextStore> { error("Not provided.") }
val LocalIOCoroutine = compositionLocalOf<IOCoroutine> { error("Not provided.") }

class IOCoroutine {
    val ioScope by mutableStateOf(
        CoroutineScope(
            CoroutineExceptionHandler { _, throwable ->
                throwable.printStackTrace()
            } + Dispatchers.IO
        )
    )
}

class ContextStore {
    val packageName: String = "YoHubDesktop"
    val rootPath: String = ".yohub-desktop"

    val versionTag: String = "1.0.6"

    //缓存目录
    val rootDir by lazy {
        val path = when {
            hostOs.isWindows -> System.getenv("LOCALAPPDATA") ?: Paths.get(
                System.getProperty("user.home"),
                "AppData",
                "Local"
            ).toString()

            hostOs.isMacOS -> Paths.get(System.getProperty("user.home"), "Library", "Caches").toString()
            else -> Paths.get(System.getProperty("user.home"), ".cache").toString()
        }
        val file = File(path, rootPath)
        if (!file.exists()) file.mkdirs()
        file
    }

    val fileDir by lazy {
        val file = File(rootDir, "files")
        if (!file.exists()) {
            file.mkdirs()
        }
        file
    }

    fun startBrowse(url: String) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(URI(url))
            } catch (e: Exception) {
                e.printStackTrace()
                sendNotice("浏览器打开失败", e.toString())
            }
        }
    }
}