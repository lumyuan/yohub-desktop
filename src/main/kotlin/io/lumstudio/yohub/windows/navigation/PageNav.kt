package io.lumstudio.yohub.windows.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.*
import io.appoutlet.karavel.Page
import io.lumstudio.yohub.R
import io.lumstudio.yohub.lang.LocalLanguageType
import io.lumstudio.yohub.ui.component.Toolbar
import io.lumstudio.yohub.windows.*

enum class PageNav(
    val page: NavPage,
) {
    Home(page = HomePage()),
    PayloadDumper(
        page = PayloadPage(),
    ),
    MagicMaskModule(
        page = MagicMaskModulesPage()
    ),
    MagiskPatcher(
        page = MagiskPatcherPage().apply { parent = MagicMaskModule.page }
    ),
    MagiskRepository(
        page = MagiskRepositoryPage().apply { parent = MagicMaskModule.page }
    ),
    Adb(
        page = AdbPage()
    ),
    AdbInstallApk(
        page = AdbInstallApkPage().apply { parent = Adb.page }
    ),
    AdbAppPickup(
        page = AdbAppPickupPage().apply { parent = Adb.page }
    ),
    AdbActivate(
        page = AdbActivatePage().apply { parent = Adb.page }
    ),
    FlashImage(
        page = FlashImagePage()
    ),
    AdvancedFunction(
      page = AdvancedFunctionPage()
    ),
    ImageBackup(
        page = ImageBackupPage().apply { parent = AdvancedFunction.page }
    ),
    Settings(
        page = SettingsPage(),
    )
}

abstract class NavPage(
    var parent: NavPage? = null,
    var isNavigation: Boolean = true
) : Page() {
    var nestedItems: List<NavPage>? = null
    var label: String = ""
    var title: String? = null
    var subtitle: String? = null
    abstract fun icon(): @Composable () -> Unit
    abstract fun label(): String
    abstract fun title(): String?
    abstract fun subtitle(): String?
}

class HomePage : NavPage() {

    override fun icon(): @Composable () -> Unit = { Icon(Icons.Default.Home, null) }
    override fun label(): String = LocalLanguageType.value.lang.labelHome

    override fun title(): String? = null

    override fun subtitle(): String? = null

    @Composable
    override fun content() {
        HomeScreen(this)
    }
}

class PayloadPage : NavPage() {
    override fun icon(): @Composable () -> Unit = { Icon(Icons.Default.FolderZip, null) }
    override fun label(): String = LocalLanguageType.value.lang.labelPayload

    override fun title(): String = LocalLanguageType.value.lang.titlePayload

    override fun subtitle(): String = LocalLanguageType.value.lang.subtitlePayload

    @Composable
    override fun content() {
        PayloadScreen(this)
    }
}

class MagicMaskModulesPage : NavPage() {

    override fun icon(): @Composable () -> Unit = { Icon(painter = painterResource(R.icon.icMagisk), null) }
    override fun label(): String = LocalLanguageType.value.lang.labelMagiskArea

    override fun title(): String = LocalLanguageType.value.lang.titleMagiskArea

    override fun subtitle(): String = LocalLanguageType.value.lang.subtitleMagiskArea

    init {
        nestedItems = arrayListOf(
            MagiskPatcherPage().apply { parent = this@MagicMaskModulesPage },
            MagiskRepositoryPage().apply { parent = this@MagicMaskModulesPage }
        )
    }

    @Composable
    override fun content() {
        MagicMaskModulesScreen(this)
    }
}

class MagiskPatcherPage : NavPage(isNavigation = false) {
    override fun icon(): @Composable () -> Unit = { Icon(Icons.Default.MobileOptimized, null) }
    override fun label(): String = LocalLanguageType.value.lang.labelMagiskPatcher

    override fun title(): String = LocalLanguageType.value.lang.titleMagiskPatcher

    override fun subtitle(): String = LocalLanguageType.value.lang.subtitleMagiskPatcher

    @Composable
    override fun content() {
        MagiskPatcherScreen(this)
    }

}

class MagiskRepositoryPage : NavPage(isNavigation = false) {

    override fun icon(): @Composable () -> Unit = {
        Icon(Icons.Default.GroupList, null)
    }

    override fun label(): String = LocalLanguageType.value.lang.labelMagiskRepository

    override fun title(): String? = null

    override fun subtitle(): String? = null

    @Composable
    override fun content() {
        MagiskRepositoryScreen(this)
    }

}

class AdbPage : NavPage() {

    init {
        nestedItems = arrayListOf(
            AdbInstallApkPage().apply { parent = this@AdbPage },
            AdbAppPickupPage().apply { parent = this@AdbPage },
            AdbActivatePage().apply { parent = this@AdbPage },
        )
    }

    override fun icon(): @Composable () -> Unit = {
        Icon(androidx.compose.material.icons.Icons.Outlined.Android, null)
    }

    override fun label(): String = LocalLanguageType.value.lang.labelAdbArea

    override fun title(): String = LocalLanguageType.value.lang.titleAdbArea

    override fun subtitle(): String = LocalLanguageType.value.lang.subtitleAdbArea

    @Composable
    override fun content() {
        LinkedScaffold(this) {
            AdbScreen(this@AdbPage)
        }
    }

}

class AdbInstallApkPage : NavPage(isNavigation = false) {
    override fun icon(): @Composable () -> Unit = {
        Icon(Icons.Default.AppsAddIn, null)
    }

    override fun label(): String = LocalLanguageType.value.lang.labelAdbInstaller

    override fun title(): String? = null

    override fun subtitle(): String? = null

    @Composable
    override fun content() {
        LinkedScaffold(this) {
            AdbInstallApkScreen()
        }
    }

}

class AdbAppPickupPage : NavPage(isNavigation = false) {
    override fun icon(): @Composable () -> Unit = {
        Icon(Icons.Default.AppsListDetail, null)
    }

    override fun label(): String = LocalLanguageType.value.lang.labelAdbPicker

    override fun title(): String? = null

    override fun subtitle(): String? = null

    @Composable
    override fun content() {
        LinkedScaffold(this) {
            AdbAppPickupScreen()
        }
    }
}

class AdbActivatePage : NavPage(isNavigation = false) {
    override fun icon(): @Composable () -> Unit = {
        Icon(Icons.Default.Play, null)
    }

    override fun label(): String = LocalLanguageType.value.lang.labelAdbActiveArea

    override fun title(): String? = null

    override fun subtitle(): String? = null

    @Composable
    override fun content() {
        LinkedScaffold(this) {
            AdbActivateScreen()
        }
    }
}

class SettingsPage : NavPage(isNavigation = false) {
    override fun icon(): @Composable () -> Unit = { Icon(Icons.Default.Settings, null) }
    override fun label(): String = LocalLanguageType.value.lang.labelSettings

    override fun title(): String? = null

    override fun subtitle(): String? = null

    init {
        nestedItems = arrayListOf(
            LanguagePage(),
            ThemeSetting(),
            AdvancedSettingPage(),
            VersionSetting(),
            OpenSourceLicense(),
        )
    }

    @Composable
    override fun content() {
        SettingsScreen(this)
    }
}

class FlashImagePage : NavPage() {
    override fun icon(): @Composable () -> Unit = { Icon(Icons.Default.Flash, null) }
    override fun label(): String = LocalLanguageType.value.lang.labelFlashImage

    override fun title(): String = LocalLanguageType.value.lang.titleFlashImage

    override fun subtitle(): String = LocalLanguageType.value.lang.subtitleFlashImage

    @Composable
    override fun content() {
        FlashImageScreen(this)
    }
}

class AdvancedFunctionPage: NavPage() {
    override fun icon(): @Composable () -> Unit = {
        Icon(Icons.Default.Grid, null)
    }

    override fun label(): String = LocalLanguageType.value.lang.labelAdvancedFunction

    override fun title(): String? = null

    override fun subtitle(): String? = null

    init {
        nestedItems = arrayListOf(
            ImageBackupPage().apply { parent = this@AdvancedFunctionPage }
        )
    }

    @Composable
    override fun content() {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp)
            ) {
                Toolbar(label())
            }
            AdvancedFunctionScreen(this@AdvancedFunctionPage)
        }
    }
}

class ImageBackupPage: NavPage(isNavigation = false) {
    override fun icon(): @Composable () -> Unit = {
        Icon(Icons.Default.DocumentFolder, null)
    }

    override fun label(): String = LocalLanguageType.value.lang.labelImageBackup

    override fun title(): String? = null

    override fun subtitle(): String? = null

    @Composable
    override fun content() {
        LinkedScaffold(this) {
            ImageBackupScreen()
        }
    }

}