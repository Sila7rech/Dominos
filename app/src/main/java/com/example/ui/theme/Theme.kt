package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DominosLightColorScheme = lightColorScheme(
    primary = DominosBlue,
    onPrimary = CardPureWhite,
    primaryContainer = DominosBlueSoft,
    onPrimaryContainer = DominosBlueDark,
    secondary = DominosRed,
    onSecondary = CardPureWhite,
    secondaryContainer = DominosRedSoft,
    onSecondaryContainer = DominosRedDark,
    tertiary = DominosRed,
    onTertiary = CardPureWhite,
    background = CreamBackground,
    onBackground = TextCharcoal,
    surface = CardPureWhite,
    onSurface = TextCharcoal,
    surfaceVariant = InputSurfaceSage,
    onSurfaceVariant = TextMutedOlive,
    outline = DominosBlueLight,
    error = ErrorCrimson,
    onError = CardPureWhite
)

val DominosShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    isArabic: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DominosLightColorScheme,
        typography = buildDominosTypography(isArabic = isArabic),
        shapes = DominosShapes,
        content = content
    )
}
