package io.lumstudio.yohub.common.utils

import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.RootFile

class CpuInfoUtil(private val keepShellStore: KeepShellStore) {

    private val rootFile: RootFile = RootFile(keepShellStore)

    fun cpuCoreNum(): Int {
        val head = "/sys/devices/system/cpu"
        var num = 0
        val listCmd = keepShellStore adb "shell ls $head"
        val split = listCmd.split("\n")
        split.filter { rootFile.exists("$head/$it/cpufreq/cpuinfo_max_freq") }
            .onEach {
            val freqCmd = (keepShellStore adb "shell cat $head/$it/cpufreq/cpuinfo_max_freq").replace("\n", "").trim()
            try {
                freqCmd.toLong()
                num++
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        return num
    }

    fun getCurCpuFreq(number: Int): String = (keepShellStore adb "shell cat /sys/devices/system/cpu/cpu$number/cpufreq/scaling_cur_freq").replace("\n", "").trim()

    fun getMinCpuFreq(number: Int): String = (keepShellStore adb "shell cat /sys/devices/system/cpu/cpu$number/cpufreq/cpuinfo_min_freq").replace("\n", "").trim()

    fun getMaxCpuFreq(number: Int): String = (keepShellStore adb "shell cat /sys/devices/system/cpu/cpu$number/cpufreq/cpuinfo_max_freq").replace("\n", "").trim()

}