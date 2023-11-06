package io.lumstudio.yohub.windows

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.Link
import com.konyaco.fluent.icons.regular.Search
import com.lt.load_the_image.rememberImagePainter
import io.lumstudio.yohub.common.LocalContext
import io.lumstudio.yohub.common.net.LoadState
import io.lumstudio.yohub.common.net.api.impl.Repository
import io.lumstudio.yohub.common.net.pojo.MagiskRepo
import io.lumstudio.yohub.common.shell.MemoryUtil
import io.lumstudio.yohub.model.request
import io.lumstudio.yohub.ui.component.FluentItem
import io.lumstudio.yohub.ui.component.Toolbar

enum class MagiskRepository {
    Topjohnwu, HuskyDG
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagiskRepositoryScreen(magiskRepositoryPage: MagiskRepositoryPage) {

    val repos = remember { mutableStateOf<List<MagiskRepo>>(arrayListOf()) }
    val search = remember { mutableStateOf("") }
    val loadState = remember { mutableStateOf(LoadState.Loading) }
    val reposUrl = remember { mutableStateOf(MagiskRepository.Topjohnwu) }

    InitRepository(repos, loadState, reposUrl)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            Toolbar(magiskRepositoryPage.label)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = search.value,
                onValueChange = { search.value = it },
                label = { Text("搜索Magisk版本") },
                leadingIcon = {
                    Icon(Icons.Default.Search, null)
                },
                trailingIcon = {
                    if (search.value.trim().isNotEmpty()) {
                        IconButton(
                            onClick = {
                                search.value = ""
                            }
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Default.Close, null)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.size(28.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Magisk历史版本", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MagiskRepository.values().onEach {
                        Spacer(modifier = Modifier.size(16.dp))
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    reposUrl.value = it
                                }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = reposUrl.value == it,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(it.toString(), style = MaterialTheme.typography.labelMedium)
                        }
                    }

                }
            }
            Spacer(modifier = Modifier.size(8.dp))
        }
        ReposLayout(repos, loadState, search, reposUrl)
    }
}

@Composable
private fun InitRepository(
    repos: MutableState<List<MagiskRepo>>,
    loadState: MutableState<LoadState>,
    reposUrl: MutableState<MagiskRepository>
) {
    LaunchedEffect(reposUrl.value) {
        loadRepos(repos, loadState, reposUrl)
    }
}

private fun loadRepos(
    repos: MutableState<List<MagiskRepo>>,
    loadState: MutableState<LoadState>,
    reposUrl: MutableState<MagiskRepository>
) {
    request(
        {
            loadState.value = LoadState.Loading
            repos.value = when (reposUrl.value) {
                MagiskRepository.Topjohnwu -> Repository.releaseByTopjohnwu()
                MagiskRepository.HuskyDG -> Repository.releaseByHuskyDG()
            }
            loadState.value = LoadState.Success
        }, {
            loadState.value = LoadState.Fail
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReposLayout(
    repos: MutableState<List<MagiskRepo>>,
    loadState: MutableState<LoadState>,
    search: MutableState<String>,
    reposUrl: MutableState<MagiskRepository>
) {
    val contextStore = LocalContext.current
    when (loadState.value) {
        LoadState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.size(16.dp))
                Text("拼命加载中...", style = MaterialTheme.typography.labelLarge)
            }
        }

        LoadState.Success -> {
            val scrollState = rememberScrollState()
            ScrollbarContainer(
                adapter = rememberScrollbarAdapter(scrollState),
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().verticalScroll(scrollState).padding(16.dp)
                ) {
                    repos.value.filter { it.name?.contains(search.value.trim()) == true || it.tagName?.contains(search.value.trim()) == true }
                        .onEach {
                            val painter = rememberImagePainter(it.avatarUrl.toString())
                            FluentItem(
                                icon = {
                                    Image(painter, null, modifier = Modifier.clip(RoundedCornerShape(12.dp)))
                                },
                                title = it.name.toString(),
                                subtitle = "版本：${it.tagName}  大小：${String.format("%.2f", MemoryUtil.b2mb(it.size?:0))}MB  下载次数：${it.downloadCount}"
                            ) {
                                Text(
                                    it.authorName.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f)
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                                Column {
                                    var open by remember { mutableStateOf(false) }
                                    TextButton(
                                        onClick = {
                                            open = true
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("下载APK")
                                    }
                                    DropdownMenu(
                                        open,
                                        onDismissRequest = {
                                            open = false
                                        }
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text("线路1")
                                            },
                                            onClick = {
                                                contextStore.startBrowse(it.downloadUrl.toString())
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Link, null)
                                            }
                                        )
                                        if (reposUrl.value == MagiskRepository.Topjohnwu) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text("线路2")
                                                },
                                                onClick = {
                                                    contextStore.startBrowse(it.downloadUrl2.toString())
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Link, null)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }

        LoadState.Fail -> {
            TextButton(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                onClick = {
                    loadRepos(repos, loadState, reposUrl)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "加载失败，点击重试",
                )
            }
        }
    }
}