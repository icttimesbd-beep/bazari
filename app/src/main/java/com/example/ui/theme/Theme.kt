package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BazariPrimaryDark,
    onPrimary = BazariOnPrimaryDark,
    primaryContainer = BazariPrimaryContainerDark,
    onPrimaryContainer = BazariOnPrimaryContainerDark,
    secondary = BazariSecondaryDark,
    onSecondary = BazariOnSecondaryDark,
    secondaryContainer = BazariSecondaryContainerDark,
    onSecondaryContainer = BazariOnSecondaryContainerDark,
    tertiary = BazariTertiaryDark,
    onTertiary = BazariOnTertiaryDark,
    tertiaryContainer = BazariTertiaryContainerDark,
    onTertiaryContainer = BazariOnTertiaryContainerDark,
    background = BazariBackgroundDark,
    onBackground = BazariOnBackgroundDark,
    surface = BazariSurfaceDark,
    onSurface = BazariOnSurfaceDark,
    surfaceVariant = BazariSurfaceVariantDark,
    onSurfaceVariant = BazariOnSurfaceVariantDark,
    outline = BazariOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = BazariPrimaryLight,
    onPrimary = BazariOnPrimaryLight,
    primaryContainer = BazariPrimaryContainerLight,
    onPrimaryContainer = BazariOnPrimaryContainerLight,
    secondary = BazariSecondaryLight,
    onSecondary = BazariOnSecondaryLight,
    secondaryContainer = BazariSecondaryContainerLight,
    onSecondaryContainer = BazariOnSecondaryContainerLight,
    tertiary = BazariTertiaryLight,
    onTertiary = BazariOnTertiaryLight,
    tertiaryContainer = BazariTertiaryContainerLight,
    onTertiaryContainer = BazariOnTertiaryContainerLight,
    background = BazariBackgroundLight,
    onBackground = BazariOnBackgroundLight,
    surface = BazariSurfaceLight,
    onSurface = BazariOnSurfaceLight,
    surfaceVariant = BazariSurfaceVariantLight,
    onSurfaceVariant = BazariOnSurfaceVariantLight,
    outline = BazariOutlineLight
)

@Composable
fun BazariTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent brand emerald palette by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
