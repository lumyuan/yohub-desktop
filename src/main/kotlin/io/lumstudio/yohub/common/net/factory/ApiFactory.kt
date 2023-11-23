package io.lumstudio.yohub.common.net.factory

import com.google.gson.*
import io.lumstudio.yohub.common.net.converter.JsonConverterFactory
import io.lumstudio.yohub.common.net.interceptor.BusinessErrorInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * 接口请求工厂
 */
object ApiFactory {

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
                        .registerTypeAdapter(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime> {
                            override fun serialize(
                                src: LocalDateTime,
                                typeOfSrc: Type,
                                context: JsonSerializationContext
                            ): JsonElement = context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        })
                        .registerTypeAdapter(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime> {
                            override fun deserialize(
                                json: JsonElement,
                                typeOfT: Type,
                                context: JsonDeserializationContext
                            ): LocalDateTime = LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        })
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