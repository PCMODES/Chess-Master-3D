package com.example.chessora.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ChessoraTypography = Typography(
  bodyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
  )
)

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
    typography = ChessoraTypography,
    content = content
  )
}
