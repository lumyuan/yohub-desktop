package io.lumstudio.yohub.common.net.param.response

import java.io.Serializable

class SpringBootErrorBean(
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null
): Serializable {
    constructor(): this(null)
}