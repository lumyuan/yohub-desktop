package io.lumstudio.yohub.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.lang.LocalLanguageType
import io.lumstudio.yohub.runtime.ClientState
import io.lumstudio.yohub.runtime.LocalDevice
import io.lumstudio.yohub.windows.navigation.NavPage
import io.lumstudio.yohub.windows.selectPage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdbFlowFeature(page: NavPage) {
    val deviceStore = LocalDevice.current
    val languageBasic = LocalLanguageType.value.lang
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        page.nestedItems?.onEach {
            FlowButton(
                icon = {
                    it.icon().invoke()
                },
                onClick = {
                    if (deviceStore.device?.state != ClientState.DEVICE) {
                        sendNotice(languageBasic.insufficientPermissions, languageBasic.pleaseLinkAdbDevice)
                    } else {
                        selectPage.value = it
                        page.karavel?.navigate(it)
                    }
                }
            ) {
                Text(it.label())
            }
        }
    }
}