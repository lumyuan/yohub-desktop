package io.lumstudio.yohub.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.WindowScope
import com.google.gson.Gson
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowCornerPreference
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.mayakapps.compose.windowstyler.WindowStyleManager
import io.lumstudio.yohub.R
import io.lumstudio.yohub.common.utils.LocalPreferences
import io.lumstudio.yohub.common.utils.PreferencesName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
)


val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
)

val LocalTheme = compositionLocalOf<ThemeStore> { error("Not provided.") }
val LocalDark = compositionLocalOf<DarkStore> { error("Not provided.") }
val LocalTypography = compositionLocalOf<TypographyStore> { error("Not provided.") }

val LocalColorTheme = compositionLocalOf<ColorThemeStore> { error("Not provided.") }

class ColorThemeStore(colorTheme: ColorTheme) {

    var colorSchemes by mutableStateOf(colorTheme)

    data class ColorTheme(
        val lightColorScheme: ColorScheme,
        val darkColorScheme: ColorScheme
    )
}

class ThemeStore(darkTheme: DarkTheme) {
    var theme by mutableStateOf(darkTheme)
}

class DarkStore(systemDarkMode: Boolean) {
    var darkMode by mutableStateOf(systemDarkMode)
}

class TypographyStore(font: Font) {
    var typography by mutableStateOf(font)
}

@Stable
enum class DarkTheme(val annotation: String) {
    SYSTEM("跟随系统"), LIGHT("浅色"), DARK("深色")
}

private val gson by lazy { Gson() }

@Composable
fun WindowScope.MicaTheme(
    hasSurface: Boolean = true,
    content: @Composable () -> Unit
) {

    val colorThemeStore = LocalColorTheme.current
    val checkDarkState = remember { mutableStateOf(System.currentTimeMillis()) }
    val themeStore = remember { ThemeStore(DarkTheme.SYSTEM) }
    val systemDarkMode = isSystemInDarkTheme()
    val darkStore =
        remember { DarkStore(if (themeStore.theme == DarkTheme.SYSTEM) systemDarkMode else themeStore.theme == DarkTheme.DARK) }
    val typographyStore = remember { TypographyStore(Font(R.font.miSansVf)) }

    val preferencesStore = LocalPreferences.current
    val model = preferencesStore.preference[PreferencesName.DARK_MODEL.toString()]

    model?.also { themeStore.theme = gson.fromJson(it, DarkTheme::class.java) }

    CompositionLocalProvider(
        LocalTheme provides themeStore,
        LocalDark provides darkStore,
        LocalTypography provides typographyStore,
    ) {

        darkStore.darkMode =
            if (themeStore.theme == DarkTheme.SYSTEM) isSystemInDarkTheme() else themeStore.theme == DarkTheme.DARK

        LaunchedEffect(darkStore) {
            withContext(Dispatchers.IO) {
                while (isActive) {
                    if (themeStore.theme == DarkTheme.SYSTEM)
                        checkDarkState.value = System.currentTimeMillis() //如果全局主题设置为
                    delay(500)
                }
            }
        }

        CheckSystemDarkMode(checkDarkState, themeStore, darkStore)

        val type = MaterialTheme.typography
        val typography = type.copy(
            displayLarge = type.displayLarge.copy(fontFamily = FontFamily(typographyStore.typography)),
            displayMedium = type.displayMedium.copy(fontFamily = FontFamily(typographyStore.typography)),
            displaySmall = type.displaySmall.copy(fontFamily = FontFamily(typographyStore.typography)),
            headlineLarge = type.headlineLarge.copy(fontFamily = FontFamily(typographyStore.typography)),
            headlineMedium = type.headlineMedium.copy(fontFamily = FontFamily(typographyStore.typography)),
            headlineSmall = type.headlineSmall.copy(fontFamily = FontFamily(typographyStore.typography)),
            titleLarge = type.titleLarge.copy(fontFamily = FontFamily(typographyStore.typography)),
            titleMedium = type.titleMedium.copy(fontFamily = FontFamily(typographyStore.typography)),
            titleSmall = type.titleSmall.copy(fontFamily = FontFamily(typographyStore.typography)),
            bodyLarge = type.bodyLarge.copy(fontFamily = FontFamily(typographyStore.typography)),
            bodyMedium = type.bodyMedium.copy(fontFamily = FontFamily(typographyStore.typography)),
            bodySmall = type.bodySmall.copy(fontFamily = FontFamily(typographyStore.typography)),
            labelLarge = type.labelLarge.copy(fontFamily = FontFamily(typographyStore.typography)),
            labelMedium = type.labelMedium.copy(fontFamily = FontFamily(typographyStore.typography)),
            labelSmall = type.labelSmall.copy(fontFamily = FontFamily(typographyStore.typography))
        )

        val targetTheme =
            if (darkStore.darkMode) colorThemeStore.colorSchemes.darkColorScheme else colorThemeStore.colorSchemes.lightColorScheme

        val durationMillis = 400
        val animationSpec = TweenSpec<Color>(durationMillis = durationMillis, easing = FastOutSlowInEasing)

        val primary by animateColorAsState(
            targetValue = targetTheme.primary, animationSpec, label = "primary"
        )
        val primaryContainer by animateColorAsState(
            targetValue = targetTheme.primaryContainer, animationSpec, label = "primaryContainer"
        )
        val tertiary by animateColorAsState(
            targetValue = targetTheme.tertiary, animationSpec, label = "tertiary"
        )
        val tertiaryContainer by animateColorAsState(
            targetValue = targetTheme.tertiaryContainer, animationSpec, label = "tertiaryContainer"
        )
        val background by animateColorAsState(
            targetValue = targetTheme.background, animationSpec, label = "background"
        )
        val onBackground by animateColorAsState(
            targetValue = targetTheme.onBackground, animationSpec, label = "onBackground"
        )
        val outline by animateColorAsState(
            targetValue = targetTheme.outline, animationSpec, label = "outline"
        )
        val outlineVariant by animateColorAsState(
            targetValue = targetTheme.outlineVariant, animationSpec, label = "outlineVariant"
        )
        val surface by animateColorAsState(
            targetValue = targetTheme.surface, animationSpec, label = "surface"
        )
        val surfaceVariant by animateColorAsState(
            targetValue = targetTheme.surfaceVariant, animationSpec, label = "surfaceVariant"
        )

        MaterialTheme(
            colorScheme = targetTheme.copy(
                primary = primary,
                primaryContainer = primaryContainer,
                tertiary = tertiary,
                tertiaryContainer = tertiaryContainer,
                background = background,
                onBackground = onBackground,
                outline = outline,
                outlineVariant = outlineVariant,
                surface = surface,
                surfaceVariant = surfaceVariant,
            ),
            typography = typography,
        ) {
            if (hasSurface) {
                MaterialWindowStyle(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}

@Composable
private fun CheckSystemDarkMode(checkDarkState: MutableState<Long>, themeStore: ThemeStore, darkStore: DarkStore) {
    checkDarkState.value
    if (themeStore.theme == DarkTheme.SYSTEM) darkStore.darkMode = isSystemInDarkTheme()
}

@Composable
private fun WindowScope.MaterialWindowStyle(
    barColor: Color,
    captionColor: Color
) {
    val darkStore = LocalDark.current

    WindowStyleManager(
        window,
        isDarkTheme = darkStore.darkMode,
        backdropType = WindowBackdrop.Solid(MaterialTheme.colorScheme.background),
        frameStyle = WindowFrameStyle(
            titleBarColor = barColor,
            captionColor = captionColor,
            cornerPreference = WindowCornerPreference.ROUNDED
        )
    )
}