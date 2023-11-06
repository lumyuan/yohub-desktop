package io.lumstudio.yohub.common

abstract class BaseResource(val resId: String) {
    abstract class ChildResource(private val resId: String, private val childResId: String) {
        private val separator: String = "/"
        fun path(vararg names: String) = lazy {
            val path = StringBuilder("$resId$separator$childResId")
            for (name in names) {
                path.append(separator)
                    .append(name)
            }; path.toString()
        }
    }
}