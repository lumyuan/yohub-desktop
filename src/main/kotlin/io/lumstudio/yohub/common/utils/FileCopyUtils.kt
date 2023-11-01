package io.lumstudio.yohub.common.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

object FileCopyUtils {
    private const val BUFFER_SIZE = 4 * 1024 * 1024 // 缓冲区大小为4MB

    fun copyFile(source: File, destination: File) {
        FileInputStream(source).use { inputStream ->
            FileOutputStream(destination).use { outputStream ->
                val sourceChannel = inputStream.channel
                val destinationChannel = outputStream.channel
                sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel)
            }
        }
    }

    // 并行复制目录下的所有文件
    fun copyDirectory(sourceDir: File, destDir: File) {
        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        val files = sourceDir.listFiles() ?: return

        files.forEach { file ->
            val destFile = File(destDir, file.name)

            if (file.isDirectory) {
                copyDirectory(file, destFile)
            } else {
                copyFile(file, destFile)
            }
        }
    }
}