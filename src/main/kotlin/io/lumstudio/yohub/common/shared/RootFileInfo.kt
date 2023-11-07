package io.lumstudio.yohub.common.shared

import io.lumstudio.yohub.common.shell.KeepShellStore
import io.lumstudio.yohub.common.shell.RootFile

class RootFileInfo {

    private val rootFile: RootFile
    private val keepShellStore: KeepShellStore

    constructor(keepShellStore: KeepShellStore) {
        this.keepShellStore = keepShellStore
        rootFile = RootFile(keepShellStore)
    }

    constructor(keepShellStore: KeepShellStore, path: String) {
        this.keepShellStore = keepShellStore
        rootFile = RootFile(keepShellStore)

        val file = RootFile(keepShellStore).fileInfo(path)
        if (file != null) {
            this.parentDir = file.parentDir
            this.filePath = file.filePath
            this.isDirectory = file.isDirectory
        }
    }

    var parentDir: String = ""
    var filePath: String = ""
    var isDirectory: Boolean = false
    var fileSize: Long = 0;

    val fileName: String
        get() {
            if (filePath.endsWith("/")) {
                return filePath.substring(0, filePath.length - 1)
            }
            return filePath
        }

    val absolutePath: String
        get() = "$parentDir/$fileName"


    fun exists(): Boolean {
        return rootFile.exists(this.absolutePath)
    }

    fun isFile(): Boolean {
        return !isDirectory
    }

    fun getParent(): String {
        return this.parentDir
    }

    fun getName(): String {
        return this.fileName
    }

    fun listFiles(): ArrayList<RootFileInfo> {
        if (this.isDirectory) {
            return rootFile.list(this.absolutePath)
        }
        return ArrayList()
    }

    fun length(): Long {
        return this.fileSize
    }
}
