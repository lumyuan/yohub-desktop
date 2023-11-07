package io.lumstudio.yohub.windows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.component.ScrollbarContainer
import com.konyaco.fluent.component.rememberScrollbarAdapter
import io.lumstudio.yohub.common.shell.GpuUtils
import io.lumstudio.yohub.common.shell.LocalKeepShell
import io.lumstudio.yohub.common.shell.MemoryUtil
import io.lumstudio.yohub.common.shell.filter
import io.lumstudio.yohub.common.utils.BrandLogoUtil
import io.lumstudio.yohub.common.utils.CpuInfoUtil
import io.lumstudio.yohub.common.utils.CpuLoadUtils
import io.lumstudio.yohub.model.CpuCoreInfo
import io.lumstudio.yohub.runtime.ClientState
import io.lumstudio.yohub.runtime.LocalAdbRuntime
import io.lumstudio.yohub.runtime.LocalDevice
import io.lumstudio.yohub.runtime.LocalDevices
import io.lumstudio.yohub.ui.component.IndicatorBar
import io.lumstudio.yohub.ui.component.ProgressBar
import io.lumstudio.yohub.ui.component.Toolbar
import io.lumstudio.yohub.ui.component.TooltipText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.hostOs

@Composable
fun AdbScreen(adbPage: AdbPage) {
    val deviceStore = LocalDevice.current
    val devicesStore = LocalDevices.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            Toolbar(adbPage.label)
        }
        val scrollState = rememberScrollState()
        AnimatedVisibility(deviceStore.device?.state == ClientState.DEVICE) {
            ScrollbarContainer(
                adapter = rememberScrollbarAdapter(scrollState),
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().verticalScroll(scrollState)
                        .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
                ) {
                    LinkedLayout()
                }
            }
        }
        AnimatedVisibility(deviceStore.device?.state != ClientState.DEVICE) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (devicesStore.devices.isEmpty()) {
                        "请连接设备"
                    } else if (deviceStore.device == null) {
                        "请选择设备"
                    } else if (deviceStore.device?.state == ClientState.UNAUTHORIZED) {
                        "请重新连接此设备并在手机上授予权限"
                    } else if (deviceStore.device?.state == ClientState.FASTBOOT || deviceStore.device?.state == ClientState.RECOVERY) {
                        "请连接一个ADB设备"
                    } else {
                        "已连接设备：${DeviceName.value}"
                    }
                )
            }
        }
    }
}

@Composable
private fun LinkedLayout() {
    val deviceStore = LocalDevice.current

    val memoryState = remember {
        mutableStateOf(MemoryState(0f, 0f, 0f, 0f, 0f, 0f))
    }

    val externalStorageState = remember {
        mutableStateOf(MemoryUtil.ExternalStorage(0f, 0f, 0f, 0f))
    }

    val cpuState = remember {
        mutableStateOf(CpuState(-1, ArrayList(), HashMap()))
    }

    val androidInfo = remember {
        mutableStateOf(AndroidInfo("", "", "", ""))
    }

    val ramPercentage = remember {
        mutableStateOf(0f)
    }
    val swapPercentage = remember {
        mutableStateOf(0f)
    }

    val gpuStateMutableState = remember {
        mutableStateOf(GpuState(0f, 0f))
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(300.dp)
        ) {
            PhoneLayout()
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                MemoryLayout(memoryState, ramPercentage, swapPercentage, externalStorageState)
                Spacer(modifier = Modifier.size(16.dp))
                GpuLayout(gpuStateMutableState)
            }
        }
    }

    val keepShellStore = LocalKeepShell.current
    val cpuInfoUtil by remember { mutableStateOf(CpuInfoUtil(keepShellStore)) }
    val memoryUtil by remember { mutableStateOf(MemoryUtil(keepShellStore)) }
    val cpuLoadUtils by remember { mutableStateOf(CpuLoadUtils(keepShellStore)) }
    val gpuUtils by remember { mutableStateOf(GpuUtils(keepShellStore, hostOs.isWindows)) }
    val openglText = remember { mutableStateOf("加载中...") }

    LaunchedEffect(deviceStore.device) {
        withContext(Dispatchers.IO) {
            openglText.value = (keepShellStore adb "shell dumpsys SurfaceFlinger | $filter -i GLES").replace("GLES: ", "")

            val cpuCoreNum = cpuInfoUtil.cpuCoreNum()
            while (this@LaunchedEffect.isActive && deviceStore.device?.state == ClientState.DEVICE) {
                val memoryInfo = memoryUtil.memoryInfo()
                val info = memoryInfo[MemoryUtil.MemoryType.MemTotal]?.info
                val externalStorageInfo = memoryUtil.externalStorageInfo()
                externalStorageState.value = externalStorageInfo

                if (info != null) {
                    val memoryStateBean = MemoryState(
                        ramTotalSize = MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.MemTotal]?.info!!),
                        ramUsedSize = MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.MemTotal]?.info!! - memoryInfo[MemoryUtil.MemoryType.MemAvailable]?.info!!),
                        swapTotalSize = MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.SwapTotal]?.info!!),
                        swapUsedSize = MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.SwapFree]?.info!!),
                        dirty = String.format(
                            "%.1f MB",
                            MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.Dirty]?.info!!)
                        ),
                        swapCached = String.format(
                            "%.1f MB",
                            MemoryUtil.kb2mb(memoryInfo[MemoryUtil.MemoryType.SwapCached]?.info!!)
                        ),
                        romTotalSize = 0f,
                        romUsedSize = 0f
                    )
                    memoryState.value = memoryStateBean

                    ramPercentage.value =
                        (memoryStateBean.ramTotalSize - memoryStateBean.ramUsedSize) / memoryStateBean.ramTotalSize * 100f
                    swapPercentage.value =
                        (memoryStateBean.swapTotalSize - memoryStateBean.swapUsedSize) / memoryStateBean.swapTotalSize * 100f

                    val gpuFreq = gpuUtils.getGpuFreq() + "Mhz"
                    val gpuLoad = gpuUtils.getGpuLoad()

                    val gpuState = GpuState(
                        total = 100f,
                        used = gpuLoad.toFloat(),
                        freq = gpuFreq,
                        kernel = openglText.value
                    )

                    gpuStateMutableState.value = gpuState

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
                    cpuState.value =
                        CpuState(
                            cpuCoreNum,
                            cores,
                            loads ?: HashMap(),
                        )

                    val release = keepShellStore adb "shell getprop ro.build.version.release"
                    val sdk = keepShellStore adb "shell getprop ro.build.version.sdk"

                    var level = ""
                    var temperature = ""

                    (keepShellStore adb "shell dumpsys battery").split("\n").onEach {
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
                    androidInfo.value = AndroidInfo(release, sdk, level, temperature)
                }
                delay(2000)
            }
        }
    }
}

@Composable
private fun PhoneLayout() {
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
                Image(
                    BrandLogoUtil.getLogoPainterByBrand(DeviceBrand.value),
                    null,
                    modifier = Modifier.size(45.dp).align(Alignment.Center)
                )
            }
        }
    }
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
    val coreCount: Int = -1,
    val cores: ArrayList<CpuCoreInfo>,
    val loads: HashMap<Int, Double>,
    val cpuTemp: String? = null,
    val socType: String? = null
)

@Stable
data class GpuState(
    val total: Float,
    val used: Float,
    val freq: String? = null,
    val kernel: String? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MemoryLayout(
    memoryState: MutableState<MemoryState>,
    ramPercentage: MutableState<Float>,
    swapPercentage: MutableState<Float>,
    externalStorageState: MutableState<MemoryUtil.ExternalStorage>
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
            TooltipArea(
                tooltip = {
                    TooltipText {
                        Text(
                            "总空间：${String.format("%.0f", externalStorageState.value.total)} GB\n" +
                                    "已使用：${String.format("%.0f", externalStorageState.value.used)} GB\n" +
                                    "剩余：${String.format("%.0f", externalStorageState.value.avail)} GB"
                        )
                    }
                }
            ) {
                IndicatorBar(
                    componentSize = 110.dp,
                    modifier = Modifier.size(110.dp),
                    foregroundSweepAngle = (externalStorageState.value.used / externalStorageState.value.total) * 100f,
                    backgroundIndicatorStrokeWidth = 12.dp
                ) {
                    Text("存储空间", style = MaterialTheme.typography.bodyMedium)
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
                Text(
                    String.format(
                        "物理内存    %.2f%s（%dGB）",
                        ramPercentage.value,
                        "%",
                        memoryState.value.ramTotalSize.toInt() / 1024 + 1
                    ), style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.size(8.dp))
                ProgressBar(
                    modifier = Modifier.fillMaxWidth(),
                    progress = swapPercentage.value
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    String.format(
                        "交换分区    %.2f%s（%dGB）",
                        swapPercentage.value,
                        "%",
                        memoryState.value.swapTotalSize.toInt() / 1024 + 1
                    ), style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.size(16.dp))
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

@Composable
private fun GpuLayout(gpuStateMutableState: MutableState<GpuState>) {
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
            IndicatorBar(
                componentSize = 110.dp,
                modifier = Modifier.size(110.dp),
                foregroundSweepAngle = gpuStateMutableState.value.used,
                backgroundIndicatorStrokeWidth = 12.dp
            ) {
                Text("GPU", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = gpuStateMutableState.value.freq.toString(), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = String.format(
                        "负载：%d%s",
                        gpuStateMutableState.value.used.toInt(),
                        "%"
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = gpuStateMutableState.value.kernel.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}