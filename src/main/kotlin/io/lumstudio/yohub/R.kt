package io.lumstudio.yohub

import io.lumstudio.yohub.common.BaseResource


object R : BaseResource("res") {

    val font = Fonts

    val icon = Icons

    val raw = Raws

    object Fonts: ChildResource(resId, "fonts") {
        val miSansVf by path("mi_sans_vf.ttf")
        val jetBrainsMonoRegular by path("JetBrainsMono-Regular.ttf")
    }

    object Icons: ChildResource(resId, "icons") {
        val ktFlutter by path("kt-flutter.svg")
        val logo by path("ic_launcher_logo.png")
        val logoRound by path("ic_launcher_logo_round.png")
        val icMagisk by path("ic_magisk.svg")
    }

    object Raws: ChildResource(resId, "raws") {
        val adbWin by path("windows", "adb.zip")
        val magiskPatcherWin by path("windows", "magisk_patcher.zip")
        val payloadDumperWin by path("windows", "payload_dumper.zip")
        val pythonWin by path("windows", "py_3.11.5.zip")
        val fastbootDriverWin by path("windows", "usb_driver.zip")

        val adbMacOs by path("macOs", "adb.zip")
        val magiskPatcherMacOs by path("macOs", "magisk_patcher.zip")
        val payloadDumperMacOs by path("macOs", "payload_dumper.zip")
        val pythonMacOs by path("macOs", "py_3.11.5.zip")
        val fastbootDriverMacOs by path("windows", "usb_driver.zip")

        val adbLinux by path("linux", "adb.zip")
        val magiskPatcherLinux by path("linux", "magisk_patcher.zip")
        val payloadDumperLinux by path("linux", "payload_dumper.zip")
        val pythonLinux by path("linux", "py_3.11.5.zip")
        val fastbootDriverLinux by path("windows", "usb_driver.zip")
    }
}