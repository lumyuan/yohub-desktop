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
import kotlinx.coroutines.runBlocking
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
fun sendNotice(
    title: String,
    message: String,
    messageType: TrayIcon.MessageType = TrayIcon.MessageType.NONE,
    onClick: () -> Unit = {}
) {
    if (!SystemTray.isSupported()) {
        println("SystemTray is not supported on this platform.")
        return
    }
    try {
        val systemTray = SystemTray.getSystemTray()
        val trayIcon = TrayIcon(ImageIO.read(ByteArrayInputStream(runBlocking { resource(R.icon.logo).readBytes() })), "YoHub")
        trayIcon.isImageAutoSize = true
        trayIcon.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onClick()
            }
        })
        systemTray.add(trayIcon)
        trayIcon.displayMessage(title, message, messageType)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}