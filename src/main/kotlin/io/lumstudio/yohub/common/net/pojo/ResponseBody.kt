package io.lumstudio.yohub.common.net.pojo

import java.io.Serializable

data class ResponseBody<T>(
    var code: Int? = null,
    var message: String? = null,
    var data: T? = null,
    var responseTime: Long? = null
): Serializable {
    constructor() : this(null)
}
