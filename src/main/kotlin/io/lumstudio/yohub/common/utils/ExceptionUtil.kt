package io.lumstudio.yohub.common.utils

import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import io.lumstudio.yohub.common.net.pojo.ApiException
import io.lumstudio.yohub.common.sendNotice
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 异常工具类
 * @author ssq
 */
object ExceptionUtil {

    /**
     * 处理异常，toast提示错误信息
     */
    fun catchException(showError: Boolean = true, e: Throwable) {
        e.printStackTrace()
        when (e) {
            is ApiException -> showToast(
                showError,
                e.message
            )

            is HttpException -> {
                catchHttpException(
                    showError,
                    e
                )
                return
            }

            is SocketTimeoutException -> showToast(
                showError,
                "网络超时"
            )

            is UnknownHostException, -> showToast(
                showError,
                "网络异常,请检查网络连接！"
            )

            is MalformedJsonException, is JsonSyntaxException -> showToast(
                showError,
                "服务器错误:Json格式错误"
            )
            // 接口异常
            else -> showToast(
                showError,
                "操作异常：$e"
            )
        }
    }

    /**
     * 处理异常，toast提示错误信息
     */
    fun catchException(e: Throwable) {
        catchException(true, e)
    }

    /**
     * 处理网络异常
     */
    private fun catchHttpException(showError: Boolean = true, httpException: HttpException) {
        if (httpException.code() in 200 until 300) return// 成功code则不处理
        showToast(
            showError,
            catchHttpExceptionCode(
                httpException.code()
            ), httpException.code()
        )
    }

    /**
     * toast提示
     */
    private fun showToast(showError: Boolean = true, errorMsg: String?, errorCode: Int = -1) {
        if (!showError) {
            return
        }
        if (errorCode == -1) {
            sendNotice("网络错误", errorMsg.toString())
        } else {
            sendNotice("网络错误", "$errorCode：$errorMsg")
        }
    }

    /**
     * 处理网络异常
     */
    private fun catchHttpExceptionCode(errorCode: Int): String = when (errorCode) {
        in 500..600 -> "服务器错误"
        in 400 until 500 -> "请求错误"
        else -> "请求错误"
    }
}