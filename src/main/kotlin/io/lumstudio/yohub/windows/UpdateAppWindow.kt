package io.lumstudio.yohub.windows

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.component.Scrollbar
import com.lt.load_the_image.rememberImagePainter
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.LocalContext
import io.lumstudio.yohub.common.net.api.impl.Repository
import io.lumstudio.yohub.common.net.pojo.YoHubRepos
import io.lumstudio.yohub.common.shell.MemoryUtil
import io.lumstudio.yohub.common.utils.LocalPreferences
import io.lumstudio.yohub.common.utils.PreferencesName
import io.lumstudio.yohub.lang.LocalLanguageType
import io.lumstudio.yohub.model.request
import io.lumstudio.yohub.ui.component.Dialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@Composable
fun UpdateAppWindow() {
    val hasUpdate = remember { mutableStateOf(false) }
    val repos = remember { mutableStateListOf<YoHubRepos>() }
    val richTextState = rememberRichTextState()
    val data = remember { mutableStateOf<YoHubRepos?>(null) }
    val contextStore = LocalContext.current
    val preferencesStore = LocalPreferences.current
    val languageBasic = LocalLanguageType.value.lang
    val simpleDateFormat = remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")) }
    LaunchedEffect(Unit) {
        request(
            showError = false,
            {
                repos.clear()
                repos.addAll(Repository.appRepos())
                val yoHubRepos = repos.first()
                data.value = yoHubRepos
                //判断GitHub上的Release版本是否高于本地版本
                hasUpdate.value = contextStore.versionTag compareVersions yoHubRepos.tag_name < 0 && (preferencesStore.preference[PreferencesName.IGNORE_VERSION.toString()] != yoHubRepos.tag_name || !yoHubRepos.prerelease)
                richTextState.setMarkdown(yoHubRepos.body)
            }
        )
    }
    Dialog(
        visible = hasUpdate.value,
        title = languageBasic.hasNewVersion,
        confirmButtonText = languageBasic.gotoDownload,
        onConfirm = {
            contextStore.startBrowse(repos.first().assets.first().browser_download_url)
        },
        cancelButtonText = if (data.value?.prerelease == true) {
            languageBasic.cancel
        } else null,
        onCancel = if (data.value?.prerelease == true) {
            {
                CoroutineScope(Dispatchers.IO).launch {
                    preferencesStore.preference[PreferencesName.IGNORE_VERSION.toString()] = data.value?.tag_name
                    preferencesStore.submit()
                    hasUpdate.value = false
                }
            }
        } else null,
        content = {
            Row(
                modifier = Modifier.fillMaxWidth().height(250.dp)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.fillMaxWidth().height(250.dp).weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    SelectionContainer {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val imagePainter = rememberImagePainter(
                                    data.value?.assets?.first()?.uploader?.avatar_url ?: "",
                                    R.icon.logoRound
                                )
                                Image(
                                    painter = imagePainter,
                                    null,
                                    modifier = Modifier.size(45.dp).clip(RoundedCornerShape(22.5.dp)).clickable {
                                        contextStore.startBrowse(data.value?.assets?.first()?.uploader?.html_url ?: "")
                                    }
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                                Text(
                                    data.value?.assets?.first()?.uploader?.login ?: "Unknown",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Spacer(modifier = Modifier.size(16.dp))
                            Text(
                                String.format(
                                    languageBasic.updateVersionText,
                                    data.value?.name,
                                    simpleDateFormat.value.format(data.value?.created_at),
                                    MemoryUtil.format(data.value?.assets?.get(0)?.size ?: 0L),
                                    data.value?.assets?.get(0)?.download_count
                                )
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                            RichText(state = richTextState, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Scrollbar(
                    isVertical = true,
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    )
}

infix fun String.compareVersions(version2: String): Int {
    val parts1 = this.split(".")
    val parts2 = version2.split(".")
    for (i in 0 until maxOf(parts1.size, parts2.size)) {
        val num1 = if (i < parts1.size) parts1[i].toInt() else 0
        val num2 = if (i < parts2.size) parts2[i].toInt() else 0

        if (num1 < num2) return -1
        if (num1 > num2) return 1
    }
    return 0
}