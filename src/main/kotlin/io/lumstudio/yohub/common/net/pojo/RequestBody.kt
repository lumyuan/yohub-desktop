package io.lumstudio.yohub.common.net.pojo

import java.io.Serializable

data class RequestBody(
    var data: Any? = null,
    var requestTime: Long? = null
): Serializable {
    constructor(): this(null, null)
}
