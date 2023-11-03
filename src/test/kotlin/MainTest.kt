import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import io.lumstudio.yohub.theme.*
import kotlinx.coroutines.*
import java.lang.RuntimeException

fun main() {
    val gson = Gson()
    val customColorTheme = CustomColorTheme(
        name = "д╛хо",
        light = LightColorScheme.color(),
        dark = DarkColorScheme.color()
    )
    val json = gson.toJson(customColorTheme)
    println(json)
    val colorTheme = gson.fromJson(json, CustomColorTheme::class.java)
    println(colorTheme)
    println(colorTheme.getLightColorScheme())
}

