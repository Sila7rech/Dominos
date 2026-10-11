package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Reference-Matched Palette from Screenshots (Foodie / Dominos / HoumaFood)
val ForestGreenDark = Color(0xFF2D4415)
val ForestGreenMid = Color(0xFF4F7822)
val LimeGreenBright = Color(0xFF88C738)
val LimeGreenSoft = Color(0xFFE7F4D3)
val LimeGreenBorder = Color(0xFF96CC4F)

val CreamBackground = Color(0xFFF6F7F2)
val CardPureWhite = Color(0xFFFFFFFF)
val InputSurfaceSage = Color(0xFFF0F3EB)

val TextCharcoal = Color(0xFF1C2415)
val TextMutedOlive = Color(0xFF6C7466)
val TextSubtleGray = Color(0xFF8E9687)

val FlameOrange = Color(0xFFFF6B2B)
val PromoRibbonRed = Color(0xFFF04E1F)
val DealCardDark = Color(0xFF1B1F18)
val NotificationDotRed = Color(0xFFFF3B30)
val DominosBlue = Color(0xFF006491)
val DominosRed = Color(0xFFE31837)

val WarningAmber = Color(0xFFD97706)
val ErrorCrimson = Color(0xFFDC2626)
val SuccessEmerald = Color(0xFF16A34A)

// Signature Horizontal & Diagonal Gradients from Reference Screenshots
val HeaderGreenGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF314A16),
        Color(0xFF5A8A27),
        Color(0xFF86C537)
    )
)

val PrimaryButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF314A16),
        Color(0xFF5D8E29),
        Color(0xFF88C738)
    )
)

val SelectedCategoryGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF6DA72F),
        Color(0xFF3B591B)
    )
)

val DealCardOverlayGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xF2141811),
        Color(0xCC1E2419),
        Color(0x66252B1F)
    )
)
