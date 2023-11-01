package io.lumstudio.yohub.windows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.Color
import com.konyaco.fluent.icons.regular.DrawerArrowDownload
import com.konyaco.fluent.icons.regular.Open
import io.lumstudio.yohub.common.LocalContext
import io.lumstudio.yohub.common.LocalIOCoroutine
import io.lumstudio.yohub.common.utils.LocalPreferences
import io.lumstudio.yohub.common.utils.PreferencesName
import io.lumstudio.yohub.theme.DarkTheme
import io.lumstudio.yohub.theme.LocalTheme
import io.lumstudio.yohub.ui.component.FluentItem
import io.lumstudio.yohub.ui.component.Toolbar
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(settingsPage: SettingsPage) {
    val scrollState = rememberScrollState()
    ScrollbarContainer(
        adapter = rememberScrollbarAdapter(scrollState),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(scrollState).padding(16.dp)
        ) {
            settingsPage.nestedItems?.onEach {
                it.content()
            }
        }
    }
}

class ThemeSetting : NavPage("主题设置") {

    private val gson by lazy { Gson() }

    @Composable
    override fun content() {
        val themeStore = LocalTheme.current
        val preferencesStore = LocalPreferences.current
        val ioCoroutine = LocalIOCoroutine.current
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Toolbar(label, enableAnimate = false)
            FluentItem(
                Icons.Default.Color,
                "深色模式"
            ) {
                DarkTheme.values().onEach {
                    Row(
                        modifier = Modifier.padding(start = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                themeStore.theme = it
                                preferencesStore.preference[PreferencesName.DARK_MODEL.toString()] = gson.toJson(it)
                                ioCoroutine.ioScope.launch {
                                    preferencesStore.submit()
                                }
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeStore.theme == it,
                                onClick = null,
                                enabled = false
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(it.annotation)
                        }
                    }
                }
            }
        }
    }
}

class VersionSetting: NavPage("版本") {

    @Composable
    override fun content() {
        val contextStore = LocalContext.current
        Column {
            Toolbar(label, enableAnimate = false)
            FluentItem(
                Icons.Default.Open,
                "开源地址"
            ) {
                TextButton(
                    onClick = {
                        contextStore.startBrowse("https://github.com/lumyuan/yohub-desktop")
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("去围观")
                }
            }
        }
    }

}