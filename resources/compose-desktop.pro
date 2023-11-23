# 保留 Compose 相关的类和方法
-keep class androidx.compose.** { *; }
-keep class androidx.ui.** { *; }
-keep class androidx.constraintlayout.** { *; }
-keep class androidx.window.** { *; }

# 保留 JNA 相关的类和方法
-keep class com.sun.jna.** { *; }
-keep class com.sun.jna.win32.** { *; }
-keep class net.java.dev.jna.** { *; }

# 保留 Retrofit 相关的类和方法
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepattributes Signature

# 保留 GSON 相关的类和方法
-keep class com.google.gson.** { *; }
-keep class com.squareup.okhttp3.** { *; }

# 保留 usb4java 相关的类和方法
-keep class org.usb4java.** { *; }

# 如果使用了其他库，需要根据实际情况添加相应的保留规则

# 指定入口类（请根据您的项目入口类名进行替换）
-keep class io.lumstudio.yohub.common.net.param.** { *; }
-keep class io.lumstudio.yohub.common.net.pojo.** { *; }
-keep class io.lumstudio.yohub.YoHubApplicationKt.** { *; }

# 忽略不需要混淆的类、方法或字段等
-dontwarn com.example.app.**
-dontnote com.example.app.**

# 其他混淆选项（根据需要进行调整）
-optimizations !code/simplification/variable
-allowaccessmodification
-keepattributes *Annotation*