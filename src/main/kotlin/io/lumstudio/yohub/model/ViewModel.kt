package io.lumstudio.yohub.model

import io.lumstudio.yohub.common.utils.ExceptionUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun request(
    block: suspend CoroutineScope.() -> Unit,
    onError: (e: Throwable) -> Unit = {},
    onComplete: () -> Unit = {}
) {
    CoroutineScope(Dispatchers.IO).launch(
        CoroutineExceptionHandler { _, throwable ->
            run {
                ExceptionUtil.catchException(throwable)
                onError(throwable)
                throwable.printStackTrace()
            }
        }
    ) {
        try {
            block.invoke(this)
        } finally {
            onComplete()
        }
    }
}

fun request(
    showError: Boolean = true,
    block: suspend CoroutineScope.() -> Unit,
    onError: (e: Throwable) -> Unit = {},
    onComplete: () -> Unit = {}
) {
    CoroutineScope(Dispatchers.IO).launch(
        CoroutineExceptionHandler { _, throwable ->
            run {
                //¥¶¿Ì“Ï≥£
                ExceptionUtil.catchException(showError, throwable)
                onError(throwable)
            }
        }
    ) {
        try {
            block.invoke(this)
        } finally {
            onComplete()
        }
    }
}