package io.lumstudio.yohub.common.net.pojo

import java.io.IOException
import java.io.Serializable

class ApiException(
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String? = null,
    override val message: String? = null
): IOException(message), Serializable {
    constructor(): this(null)
}