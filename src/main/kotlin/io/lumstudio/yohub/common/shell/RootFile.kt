package io.lumstudio.yohub.common.shell

import io.lumstudio.yohub.common.shared.RootFileInfo

/**
 * Created by Hello on 2018/07/06.
 */

class RootFile(
    private val keepShellStore: KeepShellStore
) {
    fun exists(path: String): Boolean {
        val result = keepShellStore adb "shell ls -d $path"
        return result.contains(path) && !result.contains("No such file")
    }

    fun deleteDirOrFile(path: String) {
        keepShellStore adb "shell rm -rf \"$path\""
    }

    // 处理像 "drwxrwx--x   3 root     root         4096 1970-07-14 17:13 vendor_de/" 这样的数据行
    private fun shellFileInfoRow(row: String, parent: String): RootFileInfo? {
        if (row.startsWith("total ")) {
            return null
        }

        try {
            val file = RootFileInfo(keepShellStore)

            val columns = row.trim().split(" ");
            val size = columns[0]
            file.fileSize = size.toLong() * 1024;

            //  8 /data/adb/modules/scene_systemless/ => /data/adb/modules/scene_systemless/
            val fileName = row.substring(row.indexOf(size) + size.length + 1);

            if (fileName == "./" || fileName == "../") {
                return null
            }

            // -F  append /dir *exe @sym |FIFO

            if (fileName.endsWith("/")) {
                file.filePath = fileName.substring(0, fileName.length - 1)
                file.isDirectory = true
            } else if (fileName.endsWith("@")) {
                file.filePath = fileName.substring(0, fileName.length - 1)
            } else if (fileName.endsWith("|")) {
                file.filePath = fileName.substring(0, fileName.length - 1)
            } else if (fileName.endsWith("*")) {
                file.filePath = fileName.substring(0, fileName.length - 1)
            } else {
                file.filePath = fileName
            }

            file.parentDir = parent

            return file
        } catch (ex: Exception) {
            return null
        }
    }

    fun list(path: String): ArrayList<RootFileInfo> {
        val absPath = if (path.endsWith("/")) path.subSequence(0, path.length - 1).toString() else path
        val files = ArrayList<RootFileInfo>()
        if (exists(absPath)) {
            val outputInfo = keepShellStore adb "shell ls -1Fs \"$absPath\""
            println(">>>> files$outputInfo")
            if (outputInfo != "error") {
                val rows = outputInfo.split("\n")
                for (row in rows) {
                    val file = shellFileInfoRow(row, absPath)
                    if (file != null) {
                        files.add(file)
                    } else {
                        println(">>>> Scene MapDirError Row -> $row")
                    }
                }
            }
        } else {
            println(">>>> dir lost $absPath")
        }

        return files
    }

    fun fileInfo(path: String): RootFileInfo? {
        val absPath = if (path.endsWith("/")) path.subSequence(0, path.length - 1).toString() else path
        val outputInfo = keepShellStore adb "ls -1dFs \"$absPath\""
        println(">>>> file $outputInfo")
        if (outputInfo != "error") {
            val rows = outputInfo.split("\n")
            for (row in rows) {
                val file = shellFileInfoRow(row, absPath)
                if (file != null) {
                    file.filePath = absPath.substring(absPath.lastIndexOf("/") + 1)
                    file.parentDir = absPath.substring(0, absPath.lastIndexOf("/"))
                    return file
                } else {
                    println(">>>> Scene MapDirError Row -> $row")
                }
            }
        }

        return null
    }
}
