package com.example.somi.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MilitaryColorScheme = darkColorScheme(
    primary = ArmyGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = ArmyGreenContainer,
    onPrimaryContainer = ArmyGreenLight,
    secondary = ArmyGold,
    onSecondary = Color(0xFF1B1600),
    secondaryContainer = ArmyGoldContainer,
    onSecondaryContainer = ArmyGold,
    tertiary = TacticalAirwayBlue,
    onTertiary = Color.White,
    tertiaryContainer = TacticalAirwayBlueContainer,
    onTertiaryContainer = TacticalAirwayBlue,
    background = ArmyDarkBg,
    onBackground = TextPrimary,
    surface = ArmyCardBg,
    onSurface = TextPrimary,
    surfaceVariant = ArmyCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = ArmyBorder,
    outlineVariant = ArmyBorderBright,
    error = TacticalRed,
    onError = Color.White,
    errorContainer = TacticalRedContainer,
    onErrorContainer = TacticalRedBright
)

@Composable
fun SOMITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MilitaryColorScheme,
        typography = Typography,
        content = content
    )
}
