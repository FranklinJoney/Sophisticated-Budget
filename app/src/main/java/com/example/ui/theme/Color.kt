package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val LocalThemeIsDark = staticCompositionLocalOf { true }

// Dynamic design tokens for theme shifting support
object AppColors {
    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeIsDark.current

    val bg: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFDF7FF)

    val card: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF313033) else Color(0xFFF3EDF7)

    val category: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF2B2930) else Color(0xFFE8DEF8)

    val border: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF49454F) else Color(0xFFCAC4D0)

    val textLight: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)

    val textMuted: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF938F99) else Color(0xFF49454F)

    val textSub: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFFCAC4D0) else Color(0xFF49454F)
}

val DarkBG: Color
    @Composable
    @ReadOnlyComposable
    get() = AppColors.bg

val DarkCard: Color
    @Composable
    @ReadOnlyComposable
    get() = AppColors.card

val DarkCategory: Color
    @Composable
    @ReadOnlyComposable
    get() = AppColors.category

val DarkBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = AppColors.border

val TextLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AppColors.textLight

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = AppColors.textMuted

val TextSub: Color
    @Composable
    @ReadOnlyComposable
    get() = AppColors.textSub

val AccentPurple: Color
    @Composable
    @ReadOnlyComposable
    get() = if (AppColors.isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)

val AccentPurpleContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (AppColors.isDark) Color(0xFFEADDFF) else Color(0xFFEADDFF)

val AccentPurpleOnContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (AppColors.isDark) Color(0xFF21005D) else Color(0xFF21005D)

val AccentPurplePressed: Color
    @Composable
    @ReadOnlyComposable
    get() = if (AppColors.isDark) Color(0xFF49454F) else Color(0xFFCAC4D0)

// Category Specific Accent Colors (Matching design HTML styling)
val CatDiningBg = Color(0xFFE8DEF8)
val CatDiningText = Color(0xFF21005D)

val CatTransportBg = Color(0xFFD0BCFF)
val CatTransportText = Color(0xFF381E72)

val CatShoppingBg = Color(0xFFBAC2FF)
val CatShoppingOnBg = Color(0xFF000F5D)

val CatUtilitiesBg = Color(0xFFF2B8B5)
val CatUtilitiesOnBg = Color(0xFF601410)

val CatEntertainmentBg = Color(0xFFFFD8E4)
val CatEntertainmentOnBg = Color(0xFF31111D)

val CatOtherBg = Color(0xFFC2E7FF)
val CatOtherOnBg = Color(0xFF001D35)
