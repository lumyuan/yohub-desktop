package io.lumstudio.yohub.common.net.converter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import io.lumstudio.yohub.common.utils.AesCbcPkcs5Padding
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

class JsonConverterFactory(private val gson: Gson) : Converter.Factory() {

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        return JsonRequestBodyConverter<Any>(gson) //请求
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val adapter: TypeAdapter<*> = gson.getAdapter(TypeToken.get(type))
        return JsonResponseBodyConverter(adapter) //响应
    }

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, String> {
        return EncryptionSingleConverter<Any>()
    }

    class EncryptionSingleConverter<T : Any> : Converter<T, String> {
        override fun convert(value: T): String {
            return `value`.toString()
        }
    }
}

/**
 * 自定义请求RequestBody
 */
open class JsonRequestBodyConverter<T : Any>(
    private val gson: Gson
) : Converter<T, RequestBody> {

    @Throws(IOException::class)
    override fun convert(value: T): RequestBody {
        val postBody = gson.toJson(
            io.lumstudio.yohub.common.net.pojo.RequestBody(
                data = value,
                requestTime = System.currentTimeMillis()
            )
        )
        val encrypt = AesCbcPkcs5Padding.encrypt(postBody)
        return RequestBody.create(
            MediaType.parse("application/json; charset=UTF-8"),
            encrypt.toString()
        )
    }
}

/**
 * 自定义响应ResponseBody
 */
class JsonResponseBodyConverter<T>(
    private val adapter: TypeAdapter<T>
) : Converter<ResponseBody, T> {
    @Throws(IOException::class)
    override fun convert(responseBody: ResponseBody): T {
        var string = responseBody.string()
        if (string.length > 2 && string[0] == '"') {
            string = string.substring(1, string.lastIndexOf("\""))
            string = AesCbcPkcs5Padding.decrypt(string).toString()
        }
        return adapter.fromJson(string)
    }
}
