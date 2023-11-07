package io.lumstudio.yohub.common.shell

import org.jetbrains.skiko.hostOs

/**
 * 操作内核参数节点
 */
class KernelProp(private val keepShellStore: KeepShellStore) {
    /**
     * 获取属性
     * @param propName 属性名称
     * @return
     */
    fun getProp(propName: String): String {
        val cmd = "shell cat \"$propName\""
        return keepShellStore adb cmd
    }

    fun getProp(propName: String, grep: String): String {
        val cmd = "shell cat \"$propName\" | ${filter} \"$grep\""
        return keepShellStore adb cmd
    }

    /**
     * 保存属性
     * @param propName 属性名称（要永久保存，请以persist.开头）
     * @param value    属性值,值尽量是简单的数字或字母，避免出现错误
     */
    fun setProp(propName: String, value: String): Boolean {
        return keepShellStore adb "shell chmod 664 \"$propName\" 2 > /dev/null ${and}echo \"$value\" > \"$propName\"" != "error"
    }

    private val and by lazy {
        when {
            hostOs.isWindows -> " & "
            else -> "\n"
        }
    }
}

val filter by lazy {
    when {
        hostOs.isWindows -> "findstr"
        else -> "grep"
    }
}