package com.play.golf.perf.tracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Light Color Scheme
private val LightColorScheme: ColorScheme = lightColorScheme(
    primary              = PrimaryGreen,
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryContainer,
    onPrimaryContainer   = OnPrimaryContainer,

    secondary            = SecondaryGold,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary             = TertiaryGreen,
    onTertiary           = OnTertiary,
    tertiaryContainer    = TertiaryContainer,
    onTertiaryContainer  = OnTertiaryContainer,

    background           = BackgroundLight,
    onBackground         = OnBackgroundLight,

    surface              = SurfaceLight,
    onSurface            = OnSurfaceLight,
    surfaceVariant       = SurfaceVariantLight,

    outline              = OutlineLight,

    error                = ErrorLight,
    onError              = OnErrorLight,
    errorContainer       = ErrorContainerLight,
)

// Dark Color Scheme
private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary              = PrimaryContainer,       // lighter green becomes primary in dark
    onPrimary            = OnPrimaryContainer,
    primaryContainer     = PrimaryGreen,
    onPrimaryContainer   = PrimaryContainer,

    secondary            = SecondaryContainer,     // lighter gold in dark
    onSecondary          = OnSecondaryContainer,
    secondaryContainer   = SecondaryGold,
    onSecondaryContainer = SecondaryContainer,

    tertiary             = TertiaryContainer,
    onTertiary           = OnTertiaryContainer,
    tertiaryContainer    = TertiaryGreen,
    onTertiaryContainer  = OnTertiaryContainer,

    background           = BackgroundDark,
    onBackground         = OnBackgroundDark,

    surface              = SurfaceDark,
    onSurface            = OnSurfaceDark,
    surfaceVariant       = SurfaceVariantDark,

    outline              = OutlineDark,

    error                = ErrorDark,
    onError              = OnErrorDark,
    errorContainer       = ErrorContainerDark,
)

// App Theme
@Composable
fun GolfPerfTrackerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = GolfTypography,
        content     = content,
    )
}