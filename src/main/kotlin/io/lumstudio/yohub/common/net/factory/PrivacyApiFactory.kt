package io.lumstudio.yohub.common.net.factory

import com.google.gson.GsonBuilder
import io.lumstudio.yohub.common.net.converter.JsonConverterFactory
import io.lumstudio.yohub.common.net.interceptor.BusinessErrorInterceptor
import io.lumstudio.yohub.common.utils.DateDeserializer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 接口请求工厂
 */
object PrivacyApiFactory {

    // OkHttpClient客户端
    private val mClient: OkHttpClient by lazy { newClient() }

    /**
     * 创建API Service接口实例
     */
    fun <T> createService(baseUrl: String, clazz: Class<T>): T =
        Retrofit.Builder().baseUrl(baseUrl).client(mClient)
            .addConverterFactory(
                JsonConverterFactory(
                    GsonBuilder()
                        .registerTypeAdapter(Date::class.java, DateDeserializer())
                        .create()
                )
            )
            .build().create(clazz)

    /**
     * OkHttpClient客户端
     */
    private fun newClient(): OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(30, TimeUnit.SECONDS)// 连接时间：30s超时
        readTimeout(10, TimeUnit.SECONDS)// 读取时间：10s超时
        writeTimeout(10, TimeUnit.SECONDS)// 写入时间：10s超时
        addInterceptor(BusinessErrorInterceptor())
    }.build()
}