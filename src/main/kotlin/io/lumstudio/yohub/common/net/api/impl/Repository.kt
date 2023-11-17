package io.lumstudio.yohub.common.net.api.impl

import io.lumstudio.yohub.common.net.pojo.*

/**
 * 预处理数据(错误)
 */
fun <T> ResponseBody<T>.preprocessData(): ResponseBody<T> =
    if (code == 200) {// 成功
        // 返回数据
        this
    } else {// 失败
        // 抛出接口异常
        throw ApiException(status = code, message = message)
    }

/**
 * 接口仓库（挂起函数集）
 */
object Repository {

    suspend fun releaseByTopjohnwu(): List<MagiskRepo> =
        ServiceBuilder.publicService("https://api.github.com").releaseByTopjohnwu().toMagiskRepos()

    suspend fun releaseByHuskyDG(): List<MagiskRepo> =
        ServiceBuilder.publicService("https://api.github.com").releaseByHuskyDG().toMagiskRepos(false)

    suspend fun appRepos(): List<YoHubRepos> =
        ServiceBuilder.publicService("https://api.github.com").appRepos()

}