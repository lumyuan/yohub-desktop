package io.lumstudio.yohub.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.*
import io.appoutlet.karavel.Page

enum class PageNav(
    val page: NavPage,
) {
    Home(page = HomePage()),
    PayloadDumper(
        page = PayloadPage(),
    ),
    MagiskPatcher(
        page = MagiskPatcherPage(),
    ),
    FlashImage(
        page = FlashImagePage()
    ),
    Settings(
        page = SettingsPage(),
    )
}

abstract class NavPage(
    val label: String,
    val icon: ImageVector? = null,
    val title: String? = null,
    val subtitle: String? = null,
) : Page() {
    var nestedItems: List<NavPage>? = null
}

class HomePage : NavPage("首页", icon = Icons.Default.Home) {

    @Composable
    override fun content() {
        HomeScreen(this)
    }
}

class PayloadPage : NavPage("Payload镜像提取", icon = Icons.Default.FolderZip, "镜像文件提取", "点击右侧【Payload文件提取】") {
    @Composable
    override fun content() {
        PayloadScreen(this)
    }
}

class MagiskPatcherPage : NavPage("Boot修补（topjohnwu）", icon = Icons.Default.MobileOptimized, "修补Boot镜像（Root）", "点击右侧【Boot修补（topjohnwu）】") {
    @Composable
    override fun content() {
        MagiskPatcherScreen(this)
    }

}

class SettingsPage : NavPage("设置", icon = Icons.Default.Settings,) {

    init {
        nestedItems = arrayListOf(
            ThemeSetting()
        )
    }

    @Composable
    override fun content() {
        SettingsScreen(this)
    }
}

class FlashImagePage : NavPage("刷写镜像", icon = Icons.Default.Flash, title = "为设备刷入镜像文件", "点击右侧【刷写镜像】") {

    @Composable
    override fun content() {
        FlashImageScreen(this)
    }
}