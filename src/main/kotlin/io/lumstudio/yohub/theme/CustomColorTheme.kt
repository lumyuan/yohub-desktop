package io.lumstudio.yohub.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * 自定义主题颜色
 */
data class CustomColorTheme(
    val name: String,
    val type: String = "sRGB",
    val light: LocalhostColorTheme,
    val dark: LocalhostColorTheme
) {
    data class LocalhostColorTheme(
        val primary: String,
        val onPrimary: String,
        val primaryContainer: String,
        val onPrimaryContainer: String,
        val inversePrimary: String,
        val secondary: String,
        val onSecondary: String,
        val secondaryContainer: String,
        val onSecondaryContainer: String,
        val tertiary: String,
        val onTertiary: String,
        val tertiaryContainer: String,
        val onTertiaryContainer: String,
        val background: String,
        val onBackground: String,
        val surface: String,
        val onSurface: String,
        val surfaceVariant: String,
        val onSurfaceVariant: String,
        val surfaceTint: String,
        val inverseSurface: String,
        val inverseOnSurface: String,
        val error: String,
        val onError: String,
        val errorContainer: String,
        val onErrorContainer: String,
        val outline: String,
        val outlineVariant: String,
        val scrim: String,
    )

    fun getLightColorScheme() : ColorScheme = lightColorScheme(
        primary = light color light.primary,
        onPrimary = light color light.onPrimary,
        primaryContainer = light color light.primaryContainer,
        onPrimaryContainer = light color light.onPrimaryContainer,
        secondary = light color light.secondary,
        onSecondary = light color light.onSecondary,
        secondaryContainer = light color light.secondaryContainer,
        onSecondaryContainer = light color light.onSecondaryContainer,
        tertiary = light color light.tertiary,
        onTertiary = light color light.onTertiary,
        tertiaryContainer = light color light.tertiaryContainer,
        onTertiaryContainer = light color light.onTertiaryContainer,
        error = light color light.error,
        errorContainer = light color light.errorContainer,
        onError = light color light.onError,
        onErrorContainer = light color light.onErrorContainer,
        background = light color light.background,
        onBackground = light color light.onBackground,
        outline = light color light.outline,
        inverseOnSurface = light color light.inverseOnSurface,
        inverseSurface = light color light.inverseSurface,
        inversePrimary = light color light.inversePrimary,
        surfaceTint = light color light.surfaceTint,
        outlineVariant = light color light.outlineVariant,
        scrim = light color light.scrim,
        surface = light color light.surface,
        onSurface = light color light.onSurface,
        surfaceVariant = light color light.surfaceVariant,
        onSurfaceVariant = light color light.onSurfaceVariant,
    )

    fun getDarkColorScheme() : ColorScheme = darkColorScheme(
        primary = dark color dark.primary,
        onPrimary = dark color dark.onPrimary,
        primaryContainer = dark color dark.primaryContainer,
        onPrimaryContainer = dark color dark.onPrimaryContainer,
        secondary = dark color dark.secondary,
        onSecondary = dark color dark.onSecondary,
        secondaryContainer = dark color dark.secondaryContainer,
        onSecondaryContainer = dark color dark.onSecondaryContainer,
        tertiary = dark color dark.tertiary,
        onTertiary = dark color dark.onTertiary,
        tertiaryContainer = dark color dark.tertiaryContainer,
        onTertiaryContainer = dark color dark.onTertiaryContainer,
        error = dark color dark.error,
        errorContainer = dark color dark.errorContainer,
        onError = dark color dark.onError,
        onErrorContainer = dark color dark.onErrorContainer,
        background = dark color dark.background,
        onBackground = dark color dark.onBackground,
        outline = dark color dark.outline,
        inverseOnSurface = dark color dark.inverseOnSurface,
        inverseSurface = dark color dark.inverseSurface,
        inversePrimary = dark color dark.inversePrimary,
        surfaceTint = dark color dark.surfaceTint,
        outlineVariant = dark color dark.outlineVariant,
        scrim = dark color dark.scrim,
        surface = dark color dark.surface,
        onSurface = dark color dark.onSurface,
        surfaceVariant = dark color dark.surfaceVariant,
        onSurfaceVariant = dark color dark.onSurfaceVariant,
    )
}

infix fun CustomColorTheme.LocalhostColorTheme.color(colorString: String) : Color = Color(colorString.substring(1).toLong(16))

fun ColorScheme.color() : CustomColorTheme.LocalhostColorTheme = CustomColorTheme.LocalhostColorTheme(
    primary = primary.toArgbString(),
    onPrimary = onPrimary.toArgbString(),
    primaryContainer = primaryContainer.toArgbString(),
    onPrimaryContainer = onPrimaryContainer.toArgbString(),
    secondary = secondary.toArgbString(),
    onSecondary = onSecondary.toArgbString(),
    secondaryContainer = secondaryContainer.toArgbString(),
    onSecondaryContainer = onSecondaryContainer.toArgbString(),
    tertiary = tertiary.toArgbString(),
    onTertiary = onTertiary.toArgbString(),
    tertiaryContainer = tertiaryContainer.toArgbString(),
    onTertiaryContainer = onTertiaryContainer.toArgbString(),
    error = error.toArgbString(),
    errorContainer = errorContainer.toArgbString(),
    onError = onError.toArgbString(),
    onErrorContainer = onErrorContainer.toArgbString(),
    background = background.toArgbString(),
    onBackground = onBackground.toArgbString(),
    outline = outline.toArgbString(),
    inverseOnSurface = inverseOnSurface.toArgbString(),
    inverseSurface = inverseSurface.toArgbString(),
    inversePrimary = inversePrimary.toArgbString(),
    surfaceTint = surfaceTint.toArgbString(),
    outlineVariant = outlineVariant.toArgbString(),
    scrim = scrim.toArgbString(),
    surface = surface.toArgbString(),
    onSurface = onSurface.toArgbString(),
    surfaceVariant = surfaceVariant.toArgbString(),
    onSurfaceVariant = onSurfaceVariant.toArgbString(),
)

fun Color.toArgbString(): String {
    var color = Integer.toHexString(toArgb()).uppercase()
    if (color.length == 6) {
        color = "FF$color"
    }
    return "#$color"
}