package com.r1n1os.jetpackcomposetemplateopensource.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * App colors adapted from the Figma shadcn design system.
 *
 * Primary palette:
 * - Verde: #4CAF50 (primary action, best prices, success)
 * - Azul: #2196F3 (secondary action, links, info)
 * - Blanco: #FFFFFF (backgrounds, cards)
 *
 * Additional colors derived from default_shadcn_theme.css
 */
object AppColors {
    // Primary palette (user-defined)
    val GreenPrimary = Color(0xFF4CAF50)
    val GreenLight = Color(0xFF81C784)
    val GreenDark = Color(0xFF388E3C)
    val BlueSecondary = Color(0xFF2196F3)
    val BlueLight = Color(0xFF64B5F6)
    val BlueDark = Color(0xFF1976D2)
    val White = Color(0xFFFFFFFF)

    // shadcn theme adaptation
    val Background = Color(0xFFFFFFFF)
    val Foreground = Color(0xFF030213)
    val Card = Color(0xFFFFFFFF)
    val CardForeground = Color(0xFF030213)
    val Muted = Color(0xFFECECF0)
    val MutedForeground = Color(0xFF717182)
    val Accent = Color(0xFFE9EBEF)
    val AccentForeground = Color(0xFF030213)
    val Border = Color(0x1A000000) // rgba(0,0,0,0.1)
    val Destructive = Color(0xFFD4183D)
    val DestructiveForeground = Color(0xFFFFFFFF)
    val SwitchBackground = Color(0xFFCBCED4)
    val InputBackground = Color(0xFFF3F3F5)

    // Semantic colors
    val StarYellow = Color(0xFFFFC107)
    val SavingsGreen = Color(0xFF4CAF50)
    val PriceRed = Color(0xFFE53935)
    val TextPrimary = Color(0xFF030213)
    val TextSecondary = Color(0xFF717182)
    val TextTertiary = Color(0xFFA0A0B0)
    val Divider = Color(0xFFECECF0)
}
