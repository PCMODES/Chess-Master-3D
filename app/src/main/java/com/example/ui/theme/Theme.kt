package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ChessoraColorScheme = darkColorScheme(
  primary = RoyalGold,
  onPrimary = ObsidianBlack,
  primaryContainer = RoyalGoldDark,
  onPrimaryContainer = RoyalGoldLight,
  secondary = RoyalSilver,
  onSecondary = ObsidianBlack,
  secondaryContainer = SurfaceDark,
  onSecondaryContainer = SilverLight,
  tertiary = ElectricBlue,
  onTertiary = ObsidianBlack,
  background = ObsidianBlack,
  onBackground = TextPrimary,
  surface = GraphiteDark,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceDark,
  onSurfaceVariant = TextSecondary,
  error = SoftRubyRed,
  onError = TextPrimary,
  outline = SurfaceBorder
)

@Composable
fun ChessoraTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ChessoraColorScheme,
    typography = Typography,
    content = content
  )
}
