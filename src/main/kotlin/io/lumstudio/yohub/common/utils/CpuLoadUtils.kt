package io.lumstudio.yohub.common.utils

import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.KernelProp


/**
 * CPU负载计算器
 */
class CpuLoadUtils(
    private val keepShellStore: KeepShellStore
) {

    private val kernelProp: KernelProp
    init {
        kernelProp = KernelProp(keepShellStore)
        lastCpuState = kernelProp.getProp("/proc/stat", "^cpu")
        lastCpuStateSum = lastCpuState
    }

    private fun getCpuIndex(cols: Array<String>): Int {
        val cpuIndex: Int
        cpuIndex = if (cols[0] == "cpu") {
            -1
        } else {
            cols[0].substring(3).toInt()
        }
        return cpuIndex
    }

    private fun cpuTotalTime(cols: Array<String>): Long {
        var totalTime: Long = 0
        for (i in 1 until cols.size) {
            totalTime += cols[i].toLong()
        }
        return totalTime
    }

    private fun cpuIdelTime(cols: Array<String>): Long {
        return cols[4].toLong()
    }

    val cpuLoad: HashMap<Int, Double>? = null
        get() {
            if (lastCpuStateMap != null && System.currentTimeMillis() - lastCpuStateTime!! < 500) {
                return lastCpuStateMap
            }
            val loads = HashMap<Int, Double>()
            val times = kernelProp.getProp("/proc/stat", "^cpu")
            return if (times != "error" && times.startsWith("cpu")) {
                try {
                    if (lastCpuState.isEmpty()) {
                        lastCpuState = times
                        Thread.sleep(100)
                        field
                    } else {
                        val curTick = times.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        val prevTick = lastCpuState.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        for (cpuCurrentTime in curTick) {
                            val cols1 = cpuCurrentTime.replace(" {2}".toRegex(), " ").split(" ".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            var cols0: Array<String>? = null
                            // 根据前缀匹配上一个时段的cpu时间数据
                            for (cpu in prevTick) {
                                // startsWith条件必须加个空格，因为搜索cpu的时候 "cpu0 ..."、"cpu1 ..."等都会匹配
                                if (cpu.startsWith(cols1[0] + " ")) {
                                    cols0 = cpu.replace(" {2}".toRegex(), " ").split(" ".toRegex())
                                        .dropLastWhile { it.isEmpty() }
                                        .toTypedArray()
                                    break
                                }
                            }
                            if (cols0 != null && cols0.size != 0) {
                                val total1 = cpuTotalTime(cols1)
                                val idel1 = cpuIdelTime(cols1)
                                val total0 = cpuTotalTime(cols0)
                                val idel0 = cpuIdelTime(cols0)
                                val timePoor = total1 - total0
                                // 如果CPU时长是0，那就是离线咯
                                if (timePoor == 0L) {
                                    loads[getCpuIndex(cols1)] = 0.0
                                } else {
                                    val idelTimePoor = idel1 - idel0
                                    if (idelTimePoor < 1) {
                                        loads[getCpuIndex(cols1)] = 100.0
                                    } else {
                                        val load = 100 - idelTimePoor * 100.0 / timePoor
                                        loads[getCpuIndex(cols1)] = load
                                    }
                                }
                            } else {
                                loads[getCpuIndex(cols1)] = 0.0
                            }
                        }
                        lastCpuState = times
                        // 缓存状态以优化性能
                        lastCpuStateTime = System.currentTimeMillis()
                        lastCpuStateMap = loads
                        loads
                    }
                } catch (ex: Exception) {
                    loads
                }
            } else {
                loads
            }
        }
    val cpuLoadSum: Double? = null
        get() {
            if (lastCpuStateMap != null && System.currentTimeMillis() - lastCpuStateTime!! < 500 && lastCpuStateMap!!.containsKey(
                    -1
                )
            ) {
                return lastCpuStateMap!![-1]
            }
            val times = kernelProp.getProp("/proc/stat", "^cpu ")
            if (times != "error" && times.startsWith("cpu")) {
                try {
                    if (lastCpuStateSum.isEmpty()) {
                        lastCpuStateSum = times
                        Thread.sleep(100)
                        return field
                    } else {
                        val curTick = times.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        val prevTick = lastCpuStateSum.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        for (cpuCurrentTime in curTick) {
                            val cols1 = cpuCurrentTime.replace(" {2}".toRegex(), " ").split(" ".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            if (cols1[0].trim { it <= ' ' } == "cpu") {
                                val cols0: Array<String>
                                // 根据前缀匹配上一个时段的cpu时间数据
                                for (cpu in prevTick) {
                                    // startsWith条件必须加个空格，因为搜索cpu的时候 "cpu0 ..."、"cpu1 ..."等都会匹配
                                    if (cpu.startsWith("cpu ")) {
                                        lastCpuStateSum = times
                                        cols0 = cpu.replace(" {2}".toRegex(), " ").split(" ".toRegex())
                                            .dropLastWhile { it.isEmpty() }
                                            .toTypedArray()
                                        val total1 = cpuTotalTime(cols1)
                                        val idel1 = cpuIdelTime(cols1)
                                        val total0 = cpuTotalTime(cols0)
                                        val idel0 = cpuIdelTime(cols0)
                                        val timePoor = total1 - total0
                                        // 如果CPU时长是0，那就是离线咯
                                        return if (timePoor == 0L) {
                                            0.0
                                        } else {
                                            val idelTimePoor = idel1 - idel0
                                            if (idelTimePoor < 1) {
                                                100.0
                                            } else {
                                                100 - idelTimePoor * 100.0 / timePoor
                                            }
                                        }
                                    }
                                }
                                return 0.0
                            }
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
            return -1.0
        }

    companion object {
        private var lastCpuState = ""
        private var lastCpuStateMap: HashMap<Int, Double>? = null
        private var lastCpuStateSum = ""
        private var lastCpuStateTime: Long? = null
    }
}

