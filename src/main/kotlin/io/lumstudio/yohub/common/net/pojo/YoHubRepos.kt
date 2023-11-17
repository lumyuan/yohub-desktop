package io.lumstudio.yohub.common.net.pojo

import java.util.Date

/**
 * YoHub仓库
 */
data class YoHubRepos(
    val body: String,
    val assets: List<Asset>,
    val tag_name: String,
    val name: String,
    val created_at: Date,
    val prerelease: Boolean
) {
    data class Asset(
        val browser_download_url: String,
        val size: Long,
        val name: String,
        val uploader: Uploader,
        val download_count: Int,
    ) {
        data class Uploader(
            val login: String,
            val avatar_url: String,
            val html_url: String,
        )
    }
}