package io.lumstudio.yohub.common.shell

class MemoryUtil(private val keepShellStore: KeepShellStore) {

    data class MemoryInfo(
        val type: MemoryType,
        val info: Long,
    )

    enum class SizeUnit(val mp: Float) {
        B(1f), KB(1024f), MB(1024 * 1024f), GB(1024 * 1024 * 1024f), TB(1024 * 1024 * 1024 * 1024f)
    }

    enum class MemoryType {
        MemTotal, MemFree, MemAvailable, Buffers, Cached, SwapCached, Active, Inactive, ActiveAnon,
        InactiveAnon, ActiveFile, InactiveFile, Unevictable, Mlocked, SwapTotal, SwapFree, Dirty,
        Writeback, AnonPages, Mapped, Shmem, KReclaimable, Slab, SReclaimable, SUnreclaim, KernelStack,
        ShadowCallStack, PageTables, NFS_Unstable, Bounce, WritebackTmp, CommitLimit, Committed_AS,
        VmallocTotal, VmallocUsed, VmallocChunk, Percpu, AnonHugePages, ShmemHugePages, ShmemPmdMapped,
        FileHugePages, FilePmdMapped, CmaTotal, CmaFree, Unknown;

        companion object {
            fun string2type(str: String): MemoryType =
                when (str) {
                    "MemTotal" -> MemTotal
                    "MemFree" -> MemFree
                    "MemAvailable" -> MemAvailable
                    "Buffers" -> Buffers
                    "Cached" -> Cached
                    "SwapCached" -> SwapCached
                    "Active" -> Active
                    "Inactive" -> Inactive
                    "Active(anon)" -> ActiveAnon
                    "Inactive(anon)" -> InactiveAnon
                    "Active(file)" -> ActiveFile
                    "Inactive(file)" -> InactiveFile
                    "Unevictable" -> Unevictable
                    "Mlocked" -> Mlocked
                    "SwapTotal" -> SwapTotal
                    "SwapFree" -> SwapFree
                    "Dirty" -> Dirty
                    "AnonPages" -> AnonPages
                    "Mapped" -> Mapped
                    "Shmem" -> Shmem
                    "KReclaimable" -> KReclaimable
                    "Slab" -> Slab
                    "SReclaimable" -> SReclaimable
                    "SUnreclaim" -> SUnreclaim
                    "KernelStack" -> KernelStack
                    "ShadowCallStack" -> ShadowCallStack
                    "PageTables" -> PageTables
                    "NFS_Unstable" -> NFS_Unstable
                    "Bounce" -> Bounce
                    "WritebackTmp" -> WritebackTmp
                    "CommitLimit" -> CommitLimit
                    "Committed_AS" -> Committed_AS
                    "VmallocTotal" -> VmallocTotal
                    "VmallocUsed" -> VmallocUsed
                    "VmallocChunk" -> VmallocChunk
                    "Percpu" -> Percpu
                    "AnonHugePages" -> AnonHugePages
                    "ShmemHugePages" -> ShmemHugePages
                    "ShmemPmdMapped" -> ShmemPmdMapped
                    "FileHugePages" -> FileHugePages
                    "FilePmdMapped" -> FilePmdMapped
                    "CmaTotal" -> CmaTotal
                    "CmaFree" -> CmaFree
                    else -> Unknown
                }
        }
    }

    private fun loadInfo(): String = keepShellStore adbShell "cat /proc/meminfo"

    private fun analysisLine(line: String): MemoryInfo {
        val type = line.substring(0, line.indexOf(":"))
        val split = line.split("\\s+".toRegex())
        val tempInfo = split[1]
        val info = try {
            tempInfo.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
        return MemoryInfo(MemoryType.string2type(type), info)
    }

    fun memoryInfo(): Map<MemoryType, MemoryInfo> {
        val map = mutableMapOf<MemoryType, MemoryInfo>()
        loadInfo().split("\n").onEach {
            if (it.isNotEmpty() && it.contains(":")) {
                val memoryInfo = analysisLine(it)
                map[memoryInfo.type] = memoryInfo
            }
        }
        return map
    }

    data class ExternalStorage(
        val total: Long,
        val used: Long,
        val avail: Long,
        val useAngle: Float
    )

    fun externalStorageInfo(): ExternalStorage {
        var total = 0L
        var used = 0L
        var avail = 0L
        var useAngle = 0f
        try {
            val line =
                (keepShellStore adbShell "df /data")
                    .split("\n")[1]
            val info = line.split("\\s+".toRegex())
            try {
                total = info[1].lowercase().toLong()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                used = info[2].lowercase().toLong()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                avail = info[3].lowercase().toLong()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                useAngle = info[4].replace("%", "").toFloat()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return ExternalStorage(total, used, avail, useAngle)
    }

    companion object {
        fun kb2mb(kb: Long): Float = kb * SizeUnit.KB.mp / SizeUnit.MB.mp

        fun kb2gb(kb: Long): Float =
            kb * SizeUnit.KB.mp / SizeUnit.GB.mp

        fun kb2tb(kb: Long): Float = kb * SizeUnit.KB.mp / SizeUnit.TB.mp

        fun b2kb(b: Long): Float = b / SizeUnit.KB.mp
        fun b2mb(b: Long): Float = b / SizeUnit.MB.mp
        fun b2gb(b: Long): Float = b / SizeUnit.GB.mp
        fun b2tb(b: Long): Float = b / SizeUnit.TB.mp

        fun format(b: Long): String =
            if (b < SizeUnit.KB.mp) {
                String.format("%dB", b)
            } else if (b > SizeUnit.KB.mp && b < SizeUnit.MB.mp) {
                String.format("%.2fKB", b / SizeUnit.KB.mp)
            } else if (b > SizeUnit.MB.mp && b < SizeUnit.GB.mp) {
                String.format("%.2fMB", b / SizeUnit.MB.mp)
            }else if (b > SizeUnit.GB.mp && b < SizeUnit.TB.mp) {
                String.format("%.2fGB", b / SizeUnit.GB.mp)
            } else {
                String.format("%.2fTB", b / SizeUnit.TB.mp)
            }
    }
}