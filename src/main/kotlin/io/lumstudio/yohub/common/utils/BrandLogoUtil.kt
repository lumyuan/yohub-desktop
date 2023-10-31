package io.lumstudio.yohub.common.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

object BrandLogoUtil {

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun getLogoPainterByBrand(brand: String): Painter =
        when (brand.lowercase()) {
            "xiaomi", "redmi" -> {
                painterResource("res/icons/ic_xiaomi.png")
            }
            "huawei", "honor" -> {
                painterResource("res/icons/ic_huawei.png")
            }
            "vivo" -> {
                painterResource("res/icons/ic_vivo.png")
            }
            "oppo" -> {
                painterResource("res/icons/ic_oppo.png")
            }
            "samsung" -> {
                painterResource("res/icons/ic_samsung.png")
            }
            "sony" -> {
                painterResource("res/icons/ic_sony.png")
            }
            "meizu" -> {
                painterResource("res/icons/ic_meizu.png")
            }
            "blackshark" -> {
                painterResource("res/icons/ic_blackshark.png")
            }
            "asus" -> {
                painterResource("res/icons/ic_asus.png")
            }
            "lenovo" -> {
                painterResource("res/icons/ic_lenovo.png")
            }
            "htc" -> {
                painterResource("res/icons/ic_htc.png")
            }
            "moto" -> {
                painterResource("res/icons/ic_moto.png")
            }
            "nokia" -> {
                painterResource("res/icons/ic_nokia.png")
            }
            "razer" -> {
                painterResource("res/icons/ic_razer.png")
            }
            "sharp" -> {
                painterResource("res/icons/ic_sharp.png")
            }
            "smartisan" -> {
                painterResource("res/icons/ic_smartisan.png")
            }
            "tcl" -> {
                painterResource("res/icons/ic_tcl.png")
            }
            "zte" -> {
                painterResource("res/icons/ic_zte.png")
            }
            "gionee" -> {
                painterResource("res/icons/ic_gionee.png")
            }
            "lg" -> {
                painterResource("res/icons/ic_lg.png")
            }
            "leeco" -> {
                painterResource("res/icons/ic_leeco.png")
            }
            else -> {
                painterResource("res/icons/ic_device.png")
            }
        }

}