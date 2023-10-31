package io.lumstudio.yohub.runtime

import androidx.compose.runtime.*
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun InputDevicesWatcher(
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