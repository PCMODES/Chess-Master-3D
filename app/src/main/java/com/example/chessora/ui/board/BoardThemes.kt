package com.example.chessora.ui.board

import androidx.compose.ui.graphics.Color

enum class BoardThemeType(
  val id: String,
  val displayName: String,
  val lightSquare: Color,
  val darkSquare: Color,
  val borderOuter: Color,
  val borderInner: Color,
  val coordinateColor: Color
) {
  OBSIDIAN_GOLD(
    id = "OBSIDIAN_GOLD",
    displayName = "Obsidian & Gold",
    lightSquare = Color(0xFFC7A25D),
    darkSquare = Color(0xFF181D27),
    borderOuter = Color(0xFF0F1219),
    borderInner = Color(0xFFD6AF62),
    coordinateColor = Color(0xFFE8C888)
  ),
  WALNUT_IVORY(
    id = "WALNUT_IVORY",
    displayName = "Dark Walnut & Ivory",
    lightSquare = Color(0xFFE2D1B2),
    darkSquare = Color(0xFF5D3D25),
    borderOuter = Color(0xFF332014),
    borderInner = Color(0xFF8B5D39),
    coordinateColor = Color(0xFFE8DCC8)
  ),
  IMPERIAL_MARBLE(
    id = "IMPERIAL_MARBLE",
    displayName = "Imperial Marble",
    lightSquare = Color(0xFFE6EBF2),
    darkSquare = Color(0xFF283140),
    borderOuter = Color(0xFF181E27),
    borderInner = Color(0xFF90A4AE),
    coordinateColor = Color(0xFFCFD8DC)
  ),
  CYBER_SAPPHIRE(
    id = "CYBER_SAPPHIRE",
    displayName = "Cyber Sapphire",
    lightSquare = Color(0xFF3D6EB3),
    darkSquare = Color(0xFF10172B),
    borderOuter = Color(0xFF0A0F1D),
    borderInner = Color(0xFF648BFF),
    coordinateColor = Color(0xFF88A6FF)
  );

  companion object {
    fun fromId(id: String): BoardThemeType {
      return entries.find { it.id == id } ?: OBSIDIAN_GOLD
    }
  }
}

enum class PieceThemeType(
  val id: String,
  val displayName: String,
  val whiteBaseColor: Color,
  val whiteHighlightColor: Color,
  val blackBaseColor: Color,
  val blackHighlightColor: Color
) {
  ROYAL_GOLD(
    id = "ROYAL_GOLD",
    displayName = "Royal Gold & Silver",
    whiteBaseColor = Color(0xFFD8B264),
    whiteHighlightColor = Color(0xFFFFF2D0),
    blackBaseColor = Color(0xFF2E3544),
    blackHighlightColor = Color(0xFF7B889E)
  ),
  CLASSIC_WOOD(
    id = "CLASSIC_WOOD",
    displayName = "Handcrafted Wood",
    whiteBaseColor = Color(0xFFECD6AE),
    whiteHighlightColor = Color(0xFFFFF7EA),
    blackBaseColor = Color(0xFF482B17),
    blackHighlightColor = Color(0xFF8A5A35)
  ),
  MINIMAL_MARBLE(
    id = "MINIMAL_MARBLE",
    displayName = "Monochrome Marble",
    whiteBaseColor = Color(0xFFF0F4F8),
    whiteHighlightColor = Color(0xFFFFFFFF),
    blackBaseColor = Color(0xFF1E222A),
    blackHighlightColor = Color(0xFF5A6273)
  );

  companion object {
    fun fromId(id: String): PieceThemeType {
      return entries.find { it.id == id } ?: ROYAL_GOLD
    }
  }
}
