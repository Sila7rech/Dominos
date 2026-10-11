package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Official Domino's Pizza Brand Palette (Domino's Royal Blue, Crimson Red & Crisp White)
val DominosBlue = Color(0xFF006491)
val DominosBlueDark = Color(0xFF004363)
val DominosBlueLight = Color(0xFF0078AE)
val DominosRed = Color(0xFFE31837)
val DominosRedDark = Color(0xFFB8102A)
val DominosBlueSoft = Color(0xFFE3F2FA)
val DominosRedSoft = Color(0xFFFDE8EC)

// Mapped semantic theme tokens used across screens -> now in Domino's iconic Blue & Red!
val ForestGreenDark = DominosBlueDark
val ForestGreenMid = DominosBlue
val LimeGreenBright = DominosRed
val LimeGreenSoft = DominosBlueSoft
val LimeGreenBorder = DominosBlueLight

val CreamBackground = Color(0xFFF4F7FA)
val CardPureWhite = Color(0xFFFFFFFF)
val InputSurfaceSage = Color(0xFFEBF2F7)

val TextCharcoal = Color(0xFF0F1E29)
val TextMutedOlive = Color(0xFF526675)
val TextSubtleGray = Color(0xFF7B8E9C)

val FlameOrange = Color(0xFFE31837)
val PromoRibbonRed = Color(0xFFE31837)
val DealCardDark = Color(0xFF0B2233)
val NotificationDotRed = Color(0xFFE31837)

val WarningAmber = Color(0xFFD97706)
val ErrorCrimson = Color(0xFFD3112E)
val SuccessEmerald = Color(0xFF15803D)

// Signature Domino's Gradients (Royal Blue -> Deep Blue / Crimson Red Accent)
val HeaderGreenGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF003B57),
        Color(0xFF006491),
        Color(0xFF0078AE)
    )
)

val PrimaryButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFE31837),
        Color(0xFFC8102E),
        Color(0xFF006491)
    )
)

val SelectedCategoryGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE31837),
        Color(0xFFB30E27)
    )
)

val DealCardOverlayGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xF2071A29),
        Color(0xCC004363),
        Color(0x66006491)
    )
)
