package io.lumstudio.yohub.common.shell

object MemoryUtil {

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


    private fun analysisLine(line: String): MemoryInfo {
        val type = line.substring(0, line.indexOf(":"))
        val tempInfo = line.substring(line.indexOf(":") + 1, line.lastIndexOf(" ")).replace(" ", "")
        val info = try {
            tempInfo.toLong()
        }catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
        return MemoryInfo(MemoryType.string2type(type), info)
    }

    fun kb2mb(kb: Long): Float = kb * SizeUnit.KB.mp / SizeUnit.MB.mp

    fun kb2gb(kb: Long): Float =
        kb * SizeUnit.KB.mp / SizeUnit.GB.mp

    fun kb2tb(kb: Long): Float = kb * SizeUnit.KB.mp / SizeUnit.TB.mp

    fun b2kb(b: Long): Float = b / SizeUnit.KB.mp
    fun b2mb(b: Long): Float = b / SizeUnit.MB.mp
    fun b2gb(b: Long): Float = b / SizeUnit.GB.mp
    fun b2tb(b: Long): Float = b / SizeUnit.TB.mp
}