package io.lumstudio.yohub.common.utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object IOUtils {

    @Throws(IOException::class)
    fun writeBytes(outputStream: OutputStream, bytes: ByteArray) {
        val bufferedOutputStream = BufferedOutputStream(outputStream)
        bufferedOutputStream.write(bytes)
        bufferedOutputStream.close()
        outputStream.close()
    }

    @Throws(IOException::class)
    fun readBytes(inputStream: InputStream): ByteArray {
        val bufferedInputStream = BufferedInputStream(inputStream)
        val bytes = bufferedInputStream.readBytes()
        bufferedInputStream.close()
        inputStream.close()
        return bytes
    }

    @Throws(IOException::class)
    fun writeBytes(path: String, bytes: ByteArray) {
        val fileOutputStream = FileOutputStream(path)
        val bufferedOutputStream = BufferedOutputStream(fileOutputStream)
        bufferedOutputStream.write(bytes)
        bufferedOutputStream.close()
        fileOutputStream.close()
    }

    @Throws(IOException::class)
    fun readBytes(path: String): ByteArray {
        val inputStream = FileInputStream(path)
        val bufferedInputStream = BufferedInputStream(inputStream)
        val bytes = bufferedInputStream.readBytes()
        bufferedInputStream.close()
        inputStream.close()
        return bytes
    }
}