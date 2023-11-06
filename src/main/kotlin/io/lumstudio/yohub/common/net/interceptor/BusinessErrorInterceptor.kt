package io.lumstudio.yohub.common.net.interceptor

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.lumstudio.yohub.common.net.param.response.SpringBootErrorBean
import io.lumstudio.yohub.common.net.pojo.ApiException
import okhttp3.Interceptor
import okhttp3.Response
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8

class BusinessErrorInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val responseBody = response.body()!!
        val source = responseBody.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        val buffer = source.buffer
        val contentType = responseBody.contentType()
        val charset: Charset = contentType?.charset(UTF_8) ?: UTF_8
        val resultString = buffer.clone().readString(charset)
        if (response.code() in 500 until 600 && validate(resultString)){
            val springBootErrorBean = Gson().fromJson(resultString, SpringBootErrorBean::class.java)
            throw ApiException(
                status = response.code(),
                timestamp = springBootErrorBean.timestamp,
                message = springBootErrorBean.message,
                error = springBootErrorBean.error
            )
        }
        return response
    }

    private fun validate(jsonStr: String?): Boolean {
        val jsonElement: JsonElement = try {
            JsonParser().parse(jsonStr)
        } catch (e: Exception) {
            return false
        } ?: return false
        return jsonElement.isJsonObject
    }
}