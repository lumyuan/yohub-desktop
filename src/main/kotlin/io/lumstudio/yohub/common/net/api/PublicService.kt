package io.lumstudio.yohub.common.net.api

import io.lumstudio.yohub.common.net.pojo.YoHubRepos
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

@JvmSuppressWildcards
interface PublicService {

    companion object {
        private const val AUTHORIZATION = "Authorization"
    }

    @GET("/repos/topjohnwu/Magisk/releases")
    suspend fun releaseByTopjohnwu(): List<Map<String, Any?>>

    @GET("/repos/HuskyDG/magisk-files/releases")
    suspend fun releaseByHuskyDG(): List<Map<String, Any?>>

    @GET("/repos/lumyuan/yohub-desktop/releases")
    suspend fun appRepos(): List<YoHubRepos>

    @GET
    fun downloadFile(@Url url: String): Call<ResponseBody>
}