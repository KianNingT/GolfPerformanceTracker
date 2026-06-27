package com.play.golf.perf.tracker.ui.theme

import androidx.compose.ui.graphics.Color

// Brand / Primary, Deep Golf Green
val Color.Companion.green_1A3C2E get() = Color(0xFF5CB290) // Primary dark green
val Color.Companion.green_2D6A4F get() = Color(0xFF2D6A4F) // Primary medium green
val Color.Companion.green_52B788 get() = Color(0xFF52B788) // Primary light green
val Color.Companion.green_95D5B2 get() = Color(0xFF95D5B2) // Surface tint green

// Accent, Gold / Fairway Gold
val Color.Companion.gold_C9A84C get() = Color(0xFFC9A84C) // Primary gold accent
val Color.Companion.gold_E8C96D get() = Color(0xFFE8C96D) // Light gold
val Color.Companion.gold_F5E6C0 get() = Color(0xFFF5E6C0) // Gold surface / tint

// Neutrals, Light Theme
val Color.Companion.white_FFFFFF get() = Color(0xFFFFFFFF)
val Color.Companion.grey_F5F5F5 get() = Color(0xFFF5F5F5) // Background light
val Color.Companion.grey_EEEEEE get() = Color(0xFFEEEEEE) // Surface light
val Color.Companion.grey_CCCCCC get() = Color(0xFFCCCCCC) // Border / divider
val Color.Companion.grey_999999 get() = Color(0xFF999999) // Placeholder text
val Color.Companion.grey_666666 get() = Color(0xFF666666) // Secondary text
val Color.Companion.grey_333333 get() = Color(0xFF333333) // Primary text light

// Neutrals, Dark Theme
val Color.Companion.dark_121212  get() = Color(0xFF121212) // Background dark
val Color.Companion.dark_1E1E1E  get() = Color(0xFF1E1E1E) // Surface dark
val Color.Companion.dark_2C2C2C  get() = Color(0xFF2C2C2C) // Card dark
val Color.Companion.dark_3A3A3A  get() = Color(0xFF3A3A3A) // Border dark
val Color.Companion.dark_B0B0B0  get() = Color(0xFFB0B0B0) // Secondary text dark
val Color.Companion.dark_E0E0E0  get() = Color(0xFFE0E0E0) // Primary text dark

// Semantic
val Color.Companion.error_D32F2F  get() = Color(0xFFD32F2F)
val Color.Companion.error_FFCDD2  get() = Color(0xFFFFCDD2)
val Color.Companion.success_388E3C get() = Color(0xFF388E3C)

// Material3 seed colors (used inside Theme.kt)
internal val PrimaryGreen       = Color(0xFF2D6A4F)
internal val OnPrimary          = Color(0xFFFFFFFF)
internal val PrimaryContainer   = Color(0xFF95D5B2)
internal val OnPrimaryContainer = Color(0xFF1A3C2E)

internal val SecondaryGold       = Color(0xFFC9A84C)
internal val OnSecondary         = Color(0xFF1A1200)
internal val SecondaryContainer  = Color(0xFFF5E6C0)
internal val OnSecondaryContainer = Color(0xFF251A00)

internal val TertiaryGreen       = Color(0xFF52B788)
internal val OnTertiary          = Color(0xFF003822)
internal val TertiaryContainer   = Color(0xFF72C9A0)
internal val OnTertiaryContainer = Color(0xFF00210F)

// Light scheme surfaces
internal val BackgroundLight     = Color(0xFFF5F5F5)
internal val SurfaceLight        = Color(0xFFFFFFFF)
internal val SurfaceVariantLight = Color(0xFFEEF3EE)
internal val OnBackgroundLight   = Color(0xFF1A1C1A)
internal val OnSurfaceLight      = Color(0xFF1A1C1A)
internal val OutlineLight        = Color(0xFFCCCCCC)

// Dark scheme surfaces
internal val BackgroundDark      = Color(0xFF121212)
internal val SurfaceDark         = Color(0xFF1E1E1E)
internal val SurfaceVariantDark  = Color(0xFF2C2C2C)
internal val OnBackgroundDark    = Color(0xFFE0E0E0)
internal val OnSurfaceDark       = Color(0xFFE0E0E0)
internal val OutlineDark         = Color(0xFF3A3A3A)

internal val ErrorLight          = Color(0xFFD32F2F)
internal val OnErrorLight        = Color(0xFFFFFFFF)
internal val ErrorContainerLight = Color(0xFFFFCDD2)

internal val ErrorDark           = Color(0xFFFF6E6E)
internal val OnErrorDark         = Color(0xFF690005)
internal val ErrorContainerDark  = Color(0xFF93000A)