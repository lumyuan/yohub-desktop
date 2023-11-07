
import java.io.BufferedReader
import java.io.InputStreamReader


fun main() {
    val adbCommand = "C:\\Users\\22059\\AppData\\Local\\.yohub-desktop\\runtime\\adb\\adb shell dumpsys SurfaceFlinger --list-layers"

    val process = Runtime.getRuntime().exec(adbCommand)
    val reader = BufferedReader(InputStreamReader(process.inputStream))

    var line: String
    while (reader.readLine().also { line = it } != null) {
        if (line.contains("GLES")) {
            println(line)
        }
    }
}

