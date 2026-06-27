package com.play.golf.perf.tracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DefaultFontFamily = FontFamily.Default

val TextStyle.Companion.golfDisplayLarge get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize   = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp
)

val TextStyle.Companion.golfDisplayMedium get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize   = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
)

val TextStyle.Companion.golfHeadlineLarge get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize   = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)

val TextStyle.Companion.golfHeadlineMedium get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize   = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

val TextStyle.Companion.golfTitleLarge get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize   = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp
)

val TextStyle.Companion.golfTitleMedium get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize   = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
)

val TextStyle.Companion.golfBodyLargeRegular get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize   = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)

val TextStyle.Companion.golfBodyMediumRegular get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize   = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp
)

val TextStyle.Companion.golfBodySmallRegular get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize   = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp
)

val TextStyle.Companion.golfLabelLarge get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize   = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
)

val TextStyle.Companion.golfLabelMedium get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize   = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)

val TextStyle.Companion.golfLabelSmall get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize   = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.5.sp
)

val TextStyle.Companion.golfStatValue get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize   = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.25).sp
)

val TextStyle.Companion.golfStatLabel get() = TextStyle(
    fontFamily = DefaultFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize   = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.4.sp
)

// ── Material3 Typography object passed into MaterialTheme ─────────────────────
val GolfTypography = Typography(
    displayLarge = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = DefaultFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.5.sp
    )
)