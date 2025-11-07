package com.permissionflow.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = Gray900,
    onPrimaryContainer = Gray100,

    secondary = Gray700,
    onSecondary = White,
    secondaryContainer = Gray200,
    onSecondaryContainer = Gray900,

    tertiary = Gray600,
    onTertiary = White,
    tertiaryContainer = Gray300,
    onTertiaryContainer = Gray800,

    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,

    error = RedError,
    onError = White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Gray400,
    outlineVariant = Gray300,
)

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = Gray100,
    onPrimaryContainer = Gray900,

    secondary = Gray300,
    onSecondary = Black,
    secondaryContainer = Gray800,
    onSecondaryContainer = Gray100,

    tertiary = Gray400,
    onTertiary = Black,
    tertiaryContainer = Gray700,
    onTertiaryContainer = Gray200,

    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = Gray900,
    onSurfaceVariant = Gray300,

    error = RedError,
    onError = Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Gray600,
    outlineVariant = Gray700,
)

@Composable
fun PermissionFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
