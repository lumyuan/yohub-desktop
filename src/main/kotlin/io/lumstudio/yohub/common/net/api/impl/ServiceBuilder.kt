package io.lumstudio.yohub.common.net.api.impl

import io.lumstudio.yohub.common.net.api.PrivacyService
import io.lumstudio.yohub.common.net.api.PublicService
import io.lumstudio.yohub.common.net.factory.ApiFactory

object ServiceBuilder {

    private const val baseUrl = "https://yohub.cn"
    private const val baseUrlBeta = "https://yohub.site"
    private const val baseUrlLocal = "http://192.168.2.4:8081"

    var appServerRunnable = true

    fun getUrl() = if (appServerRunnable) {
        baseUrl
    }else {
        baseUrlBeta
    }

    fun privacyService(): PrivacyService = ApiFactory.createService(getUrl(), PrivacyService::class.java)

    fun publicService(): PublicService = ApiFactory.createService(getUrl(), PublicService::class.java)

    fun publicService(baseUrl: String): PublicService = ApiFactory.createService(baseUrl, PublicService::class.java)

}