import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("java")
}

group = "io.lumstudio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven(url = "https://jitpack.io")
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.runtime)
    implementation(compose.animation)
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    implementation(compose.desktop.components.animatedImage)

    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    implementation(compose.desktop.components.splitPane)

    implementation(compose.preview)

    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    implementation(compose.components.resources)

    implementation("com.mayakapps.compose:window-styler:0.3.2")

    implementation("com.konyaco:fluent:0.0.1-dev.6")
    implementation("com.konyaco:fluent-icons-extended:0.0.1-dev.6")

    implementation("net.java.dev.jna:jna-platform:5.13.0")
    implementation("net.java.dev.jna:jna-platform-jpms:5.13.0")
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-jpms:5.13.0")

    implementation("io.github.app-outlet:karavel:1.0.2")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.9.0")

    implementation("com.github.ltttttttttttt:load-the-image:1.0.8")
}

compose.desktop {

    application {
        mainClass = "io.lumstudio.yohub.YoHubApplicationKt"

        javaHome = System.getenv("JDK_21")

        buildTypes.release {
            proguard {
                configurationFiles.from(project.file("compose-desktop.pro"))
            }
        }

        nativeDistributions {

            includeAllModules = true

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "YoHubDesktop"
            packageVersion = "1.0.4"

            description = "YoHubTool For Desktop"
            copyright = "Copyright 2023 YoTanYun. All rights reserved."
            vendor = "YoTanYun"

            windows {
//                console = true
                dirChooser = true
                perUserInstall = true
                menuGroup = "start-menu-group"
                upgradeUuid = "1B6938C2-AB9B-47A9-889F-C8DD2AA317A4"
                this.shortcut = true
                iconFile.set(file("resources/win/icons/ic_launcher_logo.ico"))

            }
        }
    }
}
