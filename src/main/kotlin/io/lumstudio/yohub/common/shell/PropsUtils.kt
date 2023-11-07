package io.lumstudio.yohub.common.shell

/**
 * Created by Hello on 2017/8/8.
 */

class PropsUtils(
    private val keepShellStore: KeepShellStore
) {
    /**
     * 获取属性
     *
     * @param propName 属性名称
     * @return 内容
     */
    fun getProp(propName: String): String {
        return keepShellStore adb "shell getprop \"$propName\""
    }

    fun setProp(propName: String, value: String): Boolean {
        return keepShellStore adb "shell setprop \"$propName\" \"$value\"" != "error"
    }
}
