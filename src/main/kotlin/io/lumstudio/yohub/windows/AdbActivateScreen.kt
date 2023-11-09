package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.ui.component.FlowButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdbActivateScreen() {
    val keepShellStore = LocalKeepShell.current
    val scrollState = rememberScrollState()
    ScrollbarContainer(
        adapter = rememberScrollbarAdapter(scrollState),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            ActivateShizuku(keepShellStore)
            ActivateBlackScope(keepShellStore)
        }
    }
}

@Composable
private fun ActivateShizuku(keepShellStore: KeepShellStore) {
    var state by remember { mutableStateOf(true) }
    FlowButton(
        onClick = {
            if (state) {
                CoroutineScope(Dispatchers.IO).launch {
                    state = false
                    val out =
                        keepShellStore adbShell "sh '/storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh'"
                    if (out.contains("shizuku_starter exit with 0")) {
                        sendNotice("激活成功！", "【Shizuku】ADB激活成功！退出代码：0")
                    }else {
                        sendNotice("激活异常！", out)
                    }
                    state = true
                }
            }
        },
        icon = {
            AnimatedVisibility(
                visible = state,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Image(
                    painter = painterResource(R.icon.icShizuku),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AnimatedVisibility(
                visible = !state,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }
    ) {
        Text("激活Shizuku")
    }
}

@Composable
private fun ActivateBlackScope(keepShellStore: KeepShellStore) {
    var state by remember { mutableStateOf(true) }
    FlowButton(
        onClick = {
            if (state) {
                CoroutineScope(Dispatchers.IO).launch {
                    state = false
                    val out =
                        keepShellStore adbShell "sh '/data/data/me.piebridge.brevent/brevent.sh'"
                    if (out.contains("for brevent log: logcat -b main -d -s BreventLoader BreventServer")) {
                        sendNotice("激活成功！", "【黑域】ADB激活成功！再次启动黑域APP即可使用")
                    }else {
                        sendNotice("激活异常！", out)
                    }
                    state = true
                }
            }
        },
        icon = {
            AnimatedVisibility(
                visible = state,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Icon(
                    painter = painterResource(R.icon.icBlackScope),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AnimatedVisibility(
                visible = !state,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }
    ) {
        Text("激活黑域")
    }
}