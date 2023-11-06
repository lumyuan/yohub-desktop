package io.lumstudio.yohub.common.net.pojo

data class MagiskRepo(
    val name: String?,
    val avatarUrl: String?,
    val authorName: String?,
    val tagName: String?,
    val downloadCount: Long?,
    val createdAt: String?,
    val updatedAt: String?,
    val size: Long?,
    val downloadUrl: String?,
    val downloadUrl2: String?,
)

fun List<Map<String, Any?>>.toMagiskRepos(isTopjohnwu: Boolean = true): List<MagiskRepo> =
    this.filter {
        (it["name"] as String?)?.contains("Manager") != true
    }.map {
        val name = it["name"] as String?
        val author = it["author"] as Map<*, *>?
        val avatarUrl = author?.get("avatar_url") as String?
        val authorName = author?.get("login") as String?
        val tagName = it["tag_name"] as String?
        val index = if (isTopjohnwu) 0 else 1
        val browserDownloadUrl =
            ((it["assets"] as List<*>?)?.get(index) as Map<*, *>?)?.get("browser_download_url") as String?
        val downloadCount =
            (((it["assets"] as List<*>?)?.get(index) as Map<*, *>?)?.get("download_count") as Double?)?.toLong()
        val createdAt = ((it["assets"] as List<*>?)?.get(index) as Map<*, *>?)?.get("created_at") as String?
        val updatedAt = ((it["assets"] as List<*>?)?.get(index) as Map<*, *>?)?.get("updated_at") as String?
        val byte = try {
            (((it["assets"] as List<*>?)?.get(index) as Map<*, *>?)?.get("size") as Double?)?.toLong() ?: -1
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }

        val downloadUrl =
            if (browserDownloadUrl?.endsWith("apk") == true && isTopjohnwu)
                "https://cdn.jsdelivr.net/gh/topjohnwu/magisk-files@${tagName?.substring(1)}/app-release.apk"
            else
                browserDownloadUrl

        MagiskRepo(
            name = name,
            avatarUrl = avatarUrl,
            authorName = authorName,
            tagName = tagName,
            downloadCount = downloadCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
            size = byte,
            downloadUrl = downloadUrl, //CDNº”ÀŸ
            downloadUrl2 = browserDownloadUrl //GitHub
        )
    }