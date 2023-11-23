package io.lumstudio.yohub.common.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object FileDownloader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // 连接超时时间，单位为秒
        .readTimeout(30, TimeUnit.SECONDS)    // 读取超时时间，单位为秒
        .writeTimeout(30, TimeUnit.SECONDS)   // 写入超时时间，单位为秒
        .build()

    fun downloadFile(url: String, savePath: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected HTTP response: ${response.code()}")
            }
            val responseBody = response.body()
            if (responseBody != null) {
                saveFile(responseBody, savePath)
            } else {
                throw IOException("Empty response body")
            }
        }
    }

    private fun saveFile(responseBody: ResponseBody, savePath: String) {
        val file = File(savePath)
        responseBody.byteStream().use { inputStream ->
            file.sink().buffer().use { outputStream ->
                outputStream.writeAll(inputStream.source())
                outputStream.flush()
            }
        }
    }
}