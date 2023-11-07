package io.lumstudio.yohub.model

/**/

class CpuCoreInfo(var coreIndex: Int) {
    var minFreq: String? = null
    var maxFreq: String? = null
    var currentFreq: String? = null
    var loadRatio = 0.0
    override fun toString(): String {
        return "CpuCoreInfo(coreIndex=$coreIndex, minFreq=$minFreq, maxFreq=$maxFreq, currentFreq=$currentFreq, loadRatio=$loadRatio)"
    }

}
