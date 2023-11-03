package io.lumstudio.yohub.common.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

suspend fun readBytes(inputStream: InputStream): ByteArray = withContext(Dispatchers.IO) {
    val bufferedInputStream = BufferedInputStream(inputStream)
    val bytes = bufferedInputStream.readBytes()
    try {
        bufferedInputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    try {
        inputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    bytes
}

suspend fun writeBytes(outputStream: OutputStream, byteArray: ByteArray) = withContext(Dispatchers.IO) {
    val bufferedOutputStream = BufferedOutputStream(outputStream)
    bufferedOutputStream.write(byteArray)
    bufferedOutputStream.flush()
    try {
        bufferedOutputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    try {
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun copy(inputStream: InputStream, outputStream: OutputStream) = withContext(Dispatchers.IO) {
    val bytes = readBytes(inputStream)
    writeBytes(outputStream, bytes)
}

suspend fun cut(targetPath: String, outputPath: String) = withContext(Dispatchers.IO) {
    val targetFile = File(targetPath)
    try {
        val inputStream = FileInputStream(targetFile)
        val outputStream = FileOutputStream(outputPath)
        copy(inputStream, outputStream)
        targetFile.delete()
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}