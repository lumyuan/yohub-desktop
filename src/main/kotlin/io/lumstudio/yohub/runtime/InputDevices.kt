package io.lumstudio.yohub.runtime

import androidx.compose.runtime.*
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import io.lumstudio.yohub.common.sendNotice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.usb4java.*


@Composable
fun InputDevicesWatcherWithWindows(
    onChange: () -> Unit,
    isRunning: Boolean = true
) {
    var point by remember { mutableStateOf(System.currentTimeMillis() - 1000) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val user32 = User32.INSTANCE
            val hMod = Kernel32.INSTANCE.GetModuleHandle("")

            val wndClass = WinUser.WNDCLASSEX()
            wndClass.hInstance = hMod
            wndClass.lpfnWndProc =
                WinUser.WindowProc { hWnd: WinDef.HWND, msg: Int, wParam: WinDef.WPARAM, lParam: WinDef.LPARAM ->
                    if (msg == WinUser.WM_DEVICECHANGE) {
                        val millis = System.currentTimeMillis()
                        if (millis - point > 500) {
                            onChange()
                            point = millis
                        }
                    }
                    user32.DefWindowProc(hWnd, msg, wParam, lParam)
                }
            wndClass.lpszClassName = "InputDevicesWatcher"
            user32.RegisterClassEx(wndClass)
            val hWnd = user32.CreateWindowEx(
                0,
                wndClass.lpszClassName,
                "YoHubInputDevicesWatcher",
                0,
                0, 0, 0, 0,
                null,
                null,
                hMod,
                null
            )

            onChange()
            val msg = WinUser.MSG()
            while (user32.GetMessage(msg, hWnd, 0, 0) != 0 && isRunning) {
                user32.TranslateMessage(msg)
                user32.DispatchMessage(msg)
            }
        }
    }
}

@Composable
fun InputDevicesWatcherWithLinux(
    onChange: () -> Unit,
    isRunning: Boolean = true
) {
    val context by remember { mutableStateOf(Context()) }
    val handle by remember { mutableStateOf(HotplugCallbackHandle()) }

    var result = LibUsb.init(context)
    if (result != LibUsb.SUCCESS) {
        val exception = LibUsbException("Unable to initialize libusb.", result)
        sendNotice(exception::class.simpleName ?: "", exception.message?:"")
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // 注册设备插拔事件回调
                LibUsb.setOption(context, LibUsb.OPTION_LOG_LEVEL, LibUsb.LOG_LEVEL_WARNING)
                val callback = HotplugCallback { _, _, event, _ ->
                    if (event == LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED) {
                        onChange()
                    } else if (event == LibUsb.HOTPLUG_EVENT_DEVICE_LEFT) {
                        onChange()
                    }
                    if (isRunning) 0 else 1
                }
                result = LibUsb.hotplugRegisterCallback(
                    context,
                    LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED or LibUsb.HOTPLUG_EVENT_DEVICE_LEFT,
                    LibUsb.HOTPLUG_ENUMERATE,
                    LibUsb.HOTPLUG_MATCH_ANY,
                    LibUsb.HOTPLUG_MATCH_ANY,
                    LibUsb.HOTPLUG_MATCH_ANY,
                    callback,
                    null,
                    handle
                )
                if (result != LibUsb.SUCCESS) {
                    val exception = LibUsbException("Unable to register hotplug callback.", result)
                    sendNotice(exception::class.simpleName ?: "", exception.message?:"")
                }
                System.`in`.read()
                LibUsb.hotplugDeregisterCallback(context, handle)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                LibUsb.exit(context)
            }
        }
    }
}