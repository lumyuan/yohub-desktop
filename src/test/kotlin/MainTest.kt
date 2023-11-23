import io.lumstudio.yohub.common.utils.FileDownloader
import kotlin.system.exitProcess


fun main() {
    val url = "https://github.com/topjohnwu/Magisk/releases/download/v26.4/Magisk-v26.4.apk"
    val path = "C:\\Users\\22059\\Desktop\\ui\\Magisk-v26.4.apk"
    FileDownloader.downloadFile(url, path)
    println("进程结束")
    exitProcess(0)
}

