package io.lumstudio.yohub.common.net.factory

import io.lumstudio.yohub.common.net.interceptor.BusinessErrorInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 接口请求工厂
 */
object PublicApiFactory {

    // OkHttpClient客户端
    private val mClient: OkHttpClient by lazy { newClient() }
    /**
     * 创建API Service接口实例
     */
    fun <T> createService(baseUrl: String, clazz: Class<T>): T = Retrofit.Builder().baseUrl(baseUrl).client(mClient)
        .addConverterFactory(GsonConverterFactory.create())
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