
package com.lumi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LumiLightColorScheme = lightColorScheme(
    primary = LumiColors.YellowStrong,
    onPrimary = LumiColors.Dark,

    secondary = LumiColors.Yellow,
    onSecondary = LumiColors.Dark,

    tertiary = LumiColors.WarmGray,
    onTertiary = LumiColors.Dark,

    background = LumiColors.Cream,
    onBackground = LumiColors.Dark,

    surface = LumiColors.White,
    onSurface = LumiColors.Dark
)

private val LumiDarkColorScheme = darkColorScheme(
    primary = LumiColors.Yellow,
    onPrimary = LumiColors.Dark,

    secondary = LumiColors.YellowStrong,
    onSecondary = LumiColors.Dark,

    tertiary = LumiColors.WarmGray,
    onTertiary = LumiColors.Dark,

    background = LumiColors.Dark,
    onBackground = LumiColors.Cream,

    surface = LumiColors.Dark,
    onSurface = LumiColors.Cream
)

@Composable
fun LumiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        LumiDarkColorScheme
    } else {
        LumiLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
