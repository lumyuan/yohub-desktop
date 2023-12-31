﻿package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.sendNotice
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.common.shell.MemoryUtil
import io.lumstudio.yohub.common.utils.BrandLogoUtil
import io.lumstudio.yohub.common.utils.CpuInfoUtil
import io.lumstudio.yohub.common.utils.CpuLoadUtils
import io.lumstudio.yohub.lang.LocalLanguageType
import io.lumstudio.yohub.model.CpuCoreInfo
import io.lumstudio.yohub.runtime.*
import io.lumstudio.yohub.ui.component.*
import io.lumstudio.yohub.windows.navigation.AdbPage
import io.lumstudio.yohub.windows.navigation.NavPage
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

@Composable
fun AdbScreen(adbPage: AdbPage) {
    val deviceStore = LocalDevice.current
    val scrollState = rememberScrollState()
    ScrollbarContainer(
        adapter = rememberScrollbarAdapter(scrollState),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(scrollState)
                .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
        ) {
            LinkedLayout(adbPage, deviceStore)
        }
    }
}

val LocalAndroidState = compositionLocalOf<AndroidState> { error("Not provided.") }

@Stable
data class AndroidState(
    val sdk: Int,
)

@Composable
fun LinkedScaffold(navPage: NavPage, content: @Composable ColumnScope.() -> Unit) {
    val deviceStore = LocalDevice.current
    val devicesStore = LocalDevices.current
    val keepShellStore = LocalKeepShell.current
    val androidKitStore = LocalAndroidToolkit.current
    val languageBasic = LocalLanguageType.value.lang

    val loadState = remember { mutableStateOf(false) }

    LaunchedEffect(deviceStore.device?.state) {
        withContext(Dispatchers.IO) {
            if (deviceStore.device?.state == ClientState.DEVICE) {
                androidKitStore.unzipPath.listFiles()?.onEach {
                    keepShellStore adb "push \"${androidKitStore file it.name}\" \"${androidKitStore.androidToolkitPath}/${it.name}\""
                    keepShellStore adbShell "chmod 0777 ${androidKitStore.androidToolkitPath}/${it.name}"
                }
            }
            loadState.value = true
        }
    }

    AnimatedVisibility(
        !loadState.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.size(8.dp))
            Text(languageBasic.loading)
        }
    }

    AnimatedVisibility(
        loadState.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp)
            ) {
                Toolbar(navPage.label())
            }
            AnimatedVisibility(deviceStore.device?.state == ClientState.DEVICE) {
                val androidState = remember { mutableStateOf(AndroidState(0)) }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        androidState.value = AndroidState(
                            sdk = (keepShellStore adbShell "getprop ro.build.version.sdk").replace("\n", "")
                                .replace(" ", "").toInt(),

                            )
                    }
                }
                CompositionLocalProvider(
                    LocalAndroidState provides androidState.value
                ) {
                    content()
                }
            }
            AnimatedVisibility(deviceStore.device?.state != ClientState.DEVICE) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (devicesStore.devices.isEmpty()) {
                            languageBasic.pleaseLinkDevice
                        } else if (deviceStore.device == null) {
                            languageBasic.pleaseChooseDevice
                        } else if (deviceStore.device?.state == ClientState.UNAUTHORIZED) {
                            languageBasic.pleaseAuthorizeDevice
                        } else if (deviceStore.device?.state == ClientState.FASTBOOT || deviceStore.device?.state == ClientState.RECOVERY) {
                            languageBasic.pleaseLinkAdbDevice
                        } else {
                            String.format(languageBasic.linkedAdbDevice, DeviceName.value)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkedLayout(adbPage: AdbPage, deviceStore: DeviceStore) {
    val languageBasic = LocalLanguageType.value.lang

    val loadState = remember { mutableStateOf(false) }

    val memoryState = remember { mutableStateOf(MemoryState(0f, 0f, 0f, 0f, 0f, 0f)) }
    val externalStorageState = remember { mutableStateOf(MemoryUtil.ExternalStorage(0L, 0L, 0L, 0f)) }
    val cpuState = remember { mutableStateOf(CpuState(-1, ArrayList(), HashMap())) }
    val androidInfo = remember { mutableStateOf(AndroidInfo("", "", "", "")) }
    val ramPercentage = remember { mutableStateOf(0f) }
    val swapPercentage = remember { mutableStateOf(0f) }

    val keepShellStore = LocalKeepShell.current
    val cpuInfoUtil by remember { mutableStateOf(CpuInfoUtil(keepShellStore)) }
    val memoryUtil by remember { mutableStateOf(MemoryUtil(keepShellStore)) }
    val cpuLoadUtils by remember { mutableStateOf(CpuLoadUtils(keepShellStore)) }

    val cpuNameState = remember { mutableStateOf("") }
    val openglText = remember { mutableStateOf("") }

    LaunchedEffect(LocalLanguageType.value) {
        openglText.value = languageBasic.loading
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(languageBasic.deviceInfo, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.size(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(300.dp)
        ) {
            PhoneLayout(loadState, androidInfo, cpuNameState, openglText, cpuState)
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                MemoryLayout(loadState, memoryState, ramPercentage, swapPercentage, externalStorageState)
                Spacer(modifier = Modifier.size(16.dp))
                SocLayout(loadState, cpuState)
            }
        }
        Spacer(modifier = Modifier.size(28.dp))
        Text(languageBasic.adbAreaFunctions, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.size(8.dp))
        AdbFlowFeature(adbPage)
    }

    LaunchedEffect(deviceStore.device) {
        loadState.value = false
    }

    LaunchedEffect(deviceStore.device) {
        withContext(Dispatchers.IO) {
            openglText.value =
                (keepShellStore adbShell "dumpsys SurfaceFlinger | grep -i GLES").replace("GLES: ", "")
            val socModel = (keepShellStore adbShell "getprop ro.soc.model").replace("\n", "").trim()
            val hardware = (keepShellStore adbShell "getprop ro.boot.hardware").replace("\n", "").trim()
            cpuNameState.value = socModel.ifEmpty { hardware }
            val cpuCoreNum = cpuInfoUtil.cpuCoreNum()
            while (this@LaunchedEffect.isActive && deviceStore.device?.state == ClientState.DEVICE) {
                val memoryInfo = memoryUtil.memoryInfo()
                val info = memoryInfo[MemoryUtil.MemoryType.MemTotal]?.info
                if (info != null) {
                    val externalStorageInfo = memoryUtil.externalStorageInfo()
                    val memoryStateBean = MemoryState(
                        ramTotalSize = MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.MemTotal]?.info ?: 0),
                        ramUsedSize = MemoryUtil.kb2mb(
                            (memoryInfo[MemoryUtil.MemoryType.MemTotal]?.info
                                ?: 0) - (memoryInfo[MemoryUtil.MemoryType.MemAvailable]?.info ?: 0)
                        ),
                        swapTotalSize = MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.SwapTotal]?.info ?: 0),
                        swapUsedSize = MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.SwapFree]?.info ?: 0),
                        dirty = String.format(
                            "%.1f MB",
                            MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.Dirty]?.info ?: 0)
                        ),
                        swapCached = String.format(
                            "%.1f MB",
                            MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.SwapCached]?.info ?: 0)
                        ),
                        romTotalSize = 0f,
                        romUsedSize = 0f
                    )

                    /*CPU Freq*/
                    val cores = java.util.ArrayList<CpuCoreInfo>()
                    for (coreIndex in 0 until cpuCoreNum) {
                        val core = CpuCoreInfo(coreIndex)
                        core.currentFreq = cpuInfoUtil.getCurCpuFreq(coreIndex)
                        core.maxFreq = cpuInfoUtil.getMaxCpuFreq(coreIndex)
                        core.minFreq = cpuInfoUtil.getMinCpuFreq(coreIndex)
                        cores.add(core)
                    }

                    val loads = cpuLoadUtils.cpuLoad
                    for (core in cores) {
                        if (loads?.containsKey(core.coreIndex) == true) {
                            loads[core.coreIndex]?.let { core.loadRatio = it }
                        }
                    }

                    val map = HashMap<String, Int>()
                    cores.onEach {
                        val i = map[it.maxFreq]
                        if (i == null) {
                            map[it.maxFreq ?: ""] = 1
                        } else {
                            map[it.maxFreq ?: ""] = (map[it.maxFreq] ?: 1) + 1
                        }
                    }
                    val list = arrayListOf<Int>()
                    map.onEach {
                        list.add(it.value)
                    }
                    list.sort()
                    list.reverse()
                    val stringBuilder = StringBuilder("${cores.size} Cores")
                    stringBuilder.append("(")
                    list.indices.onEach {
                        stringBuilder.append("${list[it]}")
                        if (it < list.size - 1) {
                            stringBuilder.append("+")
                        }
                    }
                    stringBuilder.append(")")

                    val release = keepShellStore adbShell "getprop ro.build.version.release"
                    val sdk = keepShellStore adbShell "getprop ro.build.version.sdk"

                    var level = ""
                    var temperature = ""

                    (keepShellStore adbShell "dumpsys battery").split("\n").onEach {
                        when {
                            it.contains("level:") -> level = it.substring(it.lastIndexOf(" ") + 1) + "%"
                            it.contains("temperature:") -> {
                                try {
                                    val t = it.substring(it.lastIndexOf(" ") + 1).toFloat() / 10
                                    temperature = String.format("%.1f℃", t)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    memoryState.value = memoryStateBean
                    ramPercentage.value =
                        (memoryStateBean.ramTotalSize - memoryStateBean.ramUsedSize) / memoryStateBean.ramTotalSize * 100f
                    swapPercentage.value =
                        (memoryStateBean.swapTotalSize - memoryStateBean.swapUsedSize) / memoryStateBean.swapTotalSize * 100f
                    externalStorageState.value = externalStorageInfo
                    cpuState.value = CpuState(cpuCoreNum, cores, loads ?: HashMap(), socType = stringBuilder.toString())
                    androidInfo.value = AndroidInfo(release, sdk, level, temperature)
                }
                loadState.value = true
                delay(1000)
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PhoneLayout(
    loadState: MutableState<Boolean>,
    androidInfo: MutableState<AndroidInfo>,
    cpuNameState: MutableState<String>,
    openglText: MutableState<String>,
    cpuState: MutableState<CpuState>
) {
    val languageBasic = LocalLanguageType.value.lang
    var socConfig by remember { mutableStateOf("{}") }
    LaunchedEffect(Unit) {
        socConfig = String(resource(R.raw.socJson).readBytes())
    }
    Card(
        modifier = Modifier.width(150.dp).height(300.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize()
                .padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = .3f))
                        .align(Alignment.TopCenter)
                )
                Animator(!loadState.value) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            BrandLogoUtil.getLogoPainterByBrand(DeviceBrand.value),
                            null,
                            modifier = Modifier.size(45.dp).align(Alignment.Center)
                        )
                    }
                }
                Animator(loadState.value) {
                    SelectionContainer {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState())
                                    .padding(8.dp)
                            ) {
                                Image(
                                    BrandLogoUtil.getLogoPainterByBrand(DeviceBrand.value),
                                    null,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                val soc = CpuLoadUtils.getSoc(
                                    socConfig,
                                    if (cpuNameState.value.contains("mt")) cpuNameState.value.uppercase() else cpuNameState.value
                                )
                                Text(
                                    String.format(
                                        languageBasic.adbPhoneInfo,
                                        DeviceName.value,
                                        androidInfo.value.version.replace(
                                            "\n",
                                            ""
                                        ),
                                        androidInfo.value.sdk.replace("\n", ""),
                                        soc.name ?: cpuNameState.value.uppercase(),
                                        cpuState.value.coreCount,
                                        androidInfo.value.level,
                                        androidInfo.value.temperature,
                                        if (soc.name != null) {
                                            soc.toString() + "\n"
                                        } else {
                                            ""
                                        },
                                        openglText.value.replace("\n", "")
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily(Font(R.font.jetBrainsMonoRegular))
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Animator(
    loadState: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        loadState,
        enter = fadeIn(),
        exit = fadeOut(),
        content = content
    )
}

@Stable
data class MemoryState(
    val ramTotalSize: Float,
    val ramUsedSize: Float,
    val romTotalSize: Float,
    val romUsedSize: Float,
    val swapTotalSize: Float,
    val swapUsedSize: Float,
    val swapCached: String? = "N/A",
    val dirty: String? = "N/A"
)

@Stable
data class AndroidInfo(
    val version: String,
    val sdk: String,
    val level: String,
    val temperature: String,
)

@Stable
data class CpuState(
    val coreCount: Int = 0,
    val cores: ArrayList<CpuCoreInfo>,
    val loads: HashMap<Int, Double>,
    val cpuTemp: String? = null,
    val socType: String? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MemoryLayout(
    loadState: MutableState<Boolean>,
    memoryState: MutableState<MemoryState>,
    ramPercentage: MutableState<Float>,
    swapPercentage: MutableState<Float>,
    externalStorageState: MutableState<MemoryUtil.ExternalStorage>
) {
    val languageBasic = LocalLanguageType.value.lang
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().height(142.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = .3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TooltipArea(
                tooltip = {
                    TooltipText {
                        if (loadState.value) {
                            Text(
                                String.format(
                                    languageBasic.externalStorageInfo,
                                    String.format("%.2f", MemoryUtil.kb2gb(externalStorageState.value.total)),
                                    String.format("%.2f", MemoryUtil.kb2gb(externalStorageState.value.used)),
                                    String.format("%.2f", MemoryUtil.kb2gb(externalStorageState.value.avail))
                                )
                            )
                        }
                    }
                }
            ) {
                IndicatorBar(
                    componentSize = 110.dp,
                    modifier = Modifier.size(110.dp),
                    foregroundSweepAngle = (externalStorageState.value.used.toFloat() / externalStorageState.value.total.toFloat()) * 100f,
                    backgroundIndicatorStrokeWidth = 12.dp
                ) {
                    Text(languageBasic.externalStorageSpace, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                ProgressBar(
                    modifier = Modifier.fillMaxWidth(),
                    progress = ramPercentage.value
                )
                Spacer(modifier = Modifier.size(8.dp))
                AnimatedVisibility(loadState.value) {
                    Text(
                        String.format(
                            languageBasic.memorySpace,
                            ramPercentage.value,
                            "%",
                            memoryState.value.ramTotalSize.toInt() / 1024 + 1
                        ), style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                ProgressBar(
                    modifier = Modifier.fillMaxWidth(),
                    progress = swapPercentage.value
                )
                Spacer(modifier = Modifier.size(8.dp))
                AnimatedVisibility(loadState.value) {
                    Text(
                        String.format(
                            languageBasic.swapSpace,
                            swapPercentage.value,
                            "%",
                            memoryState.value.swapTotalSize.toInt() / 1024 + 1
                        ), style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                AnimatedVisibility(loadState.value) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SwapCached ${memoryState.value.swapCached.toString()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.fillMaxWidth().weight(1f))
                        Text("Dirty ${memoryState.value.dirty.toString()}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SocLayout(
    loadState: MutableState<Boolean>,
    cpuState: MutableState<CpuState>
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().height(142.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = .3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.size(110.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CpuTotalLayout(loadState, cpuState)
            }
            Spacer(modifier = Modifier.size(16.dp))
            AnimatedVisibility(
                loadState.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(count = cpuState.value.coreCount / 2)) {
                    items(cpuState.value.coreCount) {
                        CoreItem(it, cpuState)
                    }
                }
            }
            AnimatedVisibility(
                !loadState.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(count = 4)) {
                    items(8) {
                        Card(
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp).height(47.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {

                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.CpuTotalLayout(loadState: MutableState<Boolean>, cpuState: MutableState<CpuState>) {
    val languageBasic = LocalLanguageType.value.lang
    CpuChart(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
            .alpha(.5f),
        progress = cpuState.value.loads[-1]?.toFloat() ?: 0f
    ) {
        Text(text = "CPU")
    }
    Text(
        text = cpuState.value.socType ?: "",
        style = MaterialTheme.typography.bodyMedium
    )
    AnimatedVisibility(loadState.value) {
        Text(
            text = String.format(
                languageBasic.coreLoad,
                cpuState.value.loads[-1]?.toFloat() ?: 0f,
                "%"
            ),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .alpha(.5f)
        )
    }
}

@Composable
private fun CoreItem(column: Int, cpuState: MutableState<CpuState>) {
    Column(
        modifier = Modifier.height(55.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val cpuCoreInfo = cpuState.value.cores[column]
        CpuChart(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)
                .fillMaxSize()
                .weight(1f)
                .alpha(.5f),
            progress = cpuCoreInfo.loadRatio.toFloat()
        ) {
            Text(
                text = String.format("%d%s", cpuCoreInfo.loadRatio.toInt(), "%"),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            text = try {
                "${(cpuCoreInfo.currentFreq?.toLong() ?: 0) / 1000}MHz"
            } catch (e: Exception) {
                e.printStackTrace()
                "N/A"
            }, style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = try {
                "${(cpuCoreInfo.minFreq?.toLong() ?: 0) / 1000}~${(cpuCoreInfo.maxFreq?.toLong() ?: 0) / 1000}MHz"
            } catch (e: Exception) {
                e.printStackTrace()
                "N/A"
            },
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            modifier = Modifier.alpha(.7f)
        )
    }
}
