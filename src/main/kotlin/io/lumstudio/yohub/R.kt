package io.lumstudio.yohub

import io.lumstudio.yohub.common.BaseResource


object R : BaseResource("res") {

    val font = Fonts

    val icon = Icons

    val raw = Raws

    object Fonts: ChildResource(resId, "fonts") {
        val jetBrainsMonoRegular by path("JetBrainsMono-Regular.ttf")
        val sourceHanSans by path("SourceHanSansCN-Normal.otf")
    }

    object Icons: ChildResource(resId, "icons") {
        val logo by path("ic_launcher_logo.png")
        val logoRound by path("ic_launcher_logo_round.png")
        val icMagisk by path("ic_magisk.svg")
        val icShizuku by path("ic_shizuku.png")
        val icBlackScope by path("ic_black_scope.svg")
    }

    object Raws: ChildResource(resId, "raws") {
        val adbWin by path("windows", "adb.zip")
        val magiskPatcherWin by path("windows", "magisk-pacher-script.zip")
        val payloadDumperWin by path("windows", "payload_dumper.zip")
        val fastbootDriverWin by path("windows", "usb_driver.zip")

        val adbMacOs by path("macOs", "adb.zip")
        val magiskPatcherMacOs by path("macOs", "magisk-pacher-script.zip")
        val payloadDumperMacOs by path("macOs", "payload_dumper.zip")
        val fastbootDriverMacOs by path("windows", "usb_driver.zip")

        val adbLinux by path("linux", "adb.zip")
        val magiskPatcherLinux by path("linux", "magisk-pacher-script.zip")
        val payloadDumperLinux by path("linux", "payload_dumper.zip")
        val fastbootDriverLinux by path("windows", "usb_driver.zip")

        val socJson by path("socs.json")

        val androidKit by path("android_toolkit.zip")
    }
}