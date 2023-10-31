package io.lumstudio.yohub.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import io.lumstudio.yohub.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO


val LocalApplication = compositionLocalOf<ApplicationScope> { error("Not provided.") }

fun micaApplication(
    exitProcessOnExit: Boolean = true,
    content: @Composable ApplicationScope.() -> Unit
) = application(exitProcessOnExit) {
    CompositionLocalProvider(
        LocalApplication provides this,
    ) {
        content(this)
    }
}

@OptIn(ExperimentalResourceApi::class)
fun sendNotice(title: String, message: String, onClick: () -> Unit = {}) {
    CoroutineScope(Dispatchers.IO).launch {
        if (!SystemTray.isSupported()) {
            println("SystemTray is not supported on this platform.")
            return@launch
        }
        try {
            val systemTray = SystemTray.getSystemTray()
            val trayIcon = TrayIcon(ImageIO.read(ByteArrayInputStream(resource(R.icon.logo).readBytes())), "YoHub")
            trayIcon.isImageAutoSize = true
            trayIcon.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.button == MouseEvent.BUTTON1) { // ×ó¼üµã»÷
                        onClick()
                    } else if (e.button == MouseEvent.BUTTON3) { // ÓÒ¼üµã»÷
                        println("Tray icon right clicked.")
                    }
                }
            })
            systemTray.add(trayIcon)
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
}