package com.example.chessora.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.chessora.model.Piece
import com.example.chessora.model.PieceColor
import com.example.chessora.model.PieceType

object PieceRenderer3D {

  fun drawPiece(
    drawScope: DrawScope,
    piece: Piece,
    center: Offset,
    size: Float,
    pieceTheme: PieceThemeType,
    is3D: Boolean,
    elevationPx: Float = 0f
  ) {
    val isWhite = piece.color == PieceColor.WHITE
    val baseColor = if (isWhite) pieceTheme.whiteBaseColor else pieceTheme.blackBaseColor
    val highlightColor = if (isWhite) pieceTheme.whiteHighlightColor else pieceTheme.blackHighlightColor
    val shadowColor = if (isWhite) Color(0xFF8A6C30) else Color(0xFF0F1117)

    val effectiveCenter = Offset(center.x, center.y - elevationPx)

    // 1. Draw Piece Drop Shadow on board
    if (is3D || elevationPx > 0f) {
      val shadowRadiusX = size * 0.38f
      val shadowRadiusY = size * (if (is3D) 0.18f else 0.12f)
      drawScope.drawOval(
        color = Color(0x77000000),
        topLeft = Offset(center.x - shadowRadiusX, center.y - shadowRadiusY + size * 0.28f),
        size = Size(shadowRadiusX * 2f, shadowRadiusY * 2f)
      )
    }

    val bodyBrush = Brush.radialGradient(
      colors = listOf(highlightColor, baseColor, shadowColor),
      center = Offset(effectiveCenter.x - size * 0.12f, effectiveCenter.y - size * 0.16f),
      radius = size * 0.55f
    )

    when (piece.type) {
      PieceType.PAWN -> drawPawn(drawScope, effectiveCenter, size, bodyBrush, highlightColor, shadowColor)
      PieceType.KNIGHT -> drawKnight(drawScope, effectiveCenter, size, bodyBrush, highlightColor, shadowColor, isWhite)
      PieceType.BISHOP -> drawBishop(drawScope, effectiveCenter, size, bodyBrush, highlightColor, shadowColor)
      PieceType.ROOK -> drawRook(drawScope, effectiveCenter, size, bodyBrush, highlightColor, shadowColor)
      PieceType.QUEEN -> drawQueen(drawScope, effectiveCenter, size, bodyBrush, highlightColor, shadowColor)
      PieceType.KING -> drawKing(drawScope, effectiveCenter, size, bodyBrush, highlightColor, shadowColor)
    }
  }

  private fun drawPedestal(
    drawScope: DrawScope,
    center: Offset,
    size: Float,
    brush: Brush,
    shadowColor: Color
  ) {
    val baseY = center.y + size * 0.32f
    val baseWidth = size * 0.65f
    val baseHeight = size * 0.14f

    // Base tier 1 (Lower rim)
    drawScope.drawOval(
      color = shadowColor.copy(alpha = 0.5f),
      topLeft = Offset(center.x - baseWidth * 0.5f, baseY - baseHeight * 0.3f),
      size = Size(baseWidth, baseHeight)
    )
    drawScope.drawOval(
      brush = brush,
      topLeft = Offset(center.x - baseWidth * 0.48f, baseY - baseHeight * 0.45f),
      size = Size(baseWidth * 0.96f, baseHeight * 0.85f)
    )

    // Tier 2 (Collar ring)
    val tier2Width = size * 0.48f
    val tier2Height = size * 0.09f
    drawScope.drawOval(
      brush = brush,
      topLeft = Offset(center.x - tier2Width * 0.5f, baseY - baseHeight * 0.85f),
      size = Size(tier2Width, tier2Height)
    )
  }

  private fun drawPawn(
    drawScope: DrawScope,
    center: Offset,
    size: Float,
    brush: Brush,
    highlight: Color,
    shadow: Color
  ) {
    drawPedestal(drawScope, center, size, brush, shadow)

    // Body (Conical torso)
    val bodyPath = Path().apply {
      moveTo(center.x - size * 0.16f, center.y + size * 0.22f)
      quadraticTo(center.x - size * 0.08f, center.y - size * 0.05f, center.x - size * 0.12f, center.y - size * 0.15f)
      lineTo(center.x + size * 0.12f, center.y - size * 0.15f)
      quadraticTo(center.x + size * 0.08f, center.y - size * 0.05f, center.x + size * 0.16f, center.y + size * 0.22f)
      close()
    }
    drawScope.drawPath(bodyPath, brush)

    // Collar ring
    drawScope.drawOval(
      brush = brush,
      topLeft = Offset(center.x - size * 0.18f, center.y - size * 0.18f),
      size = Size(size * 0.36f, size * 0.08f)
    )

    // Spherical head
    val headRadius = size * 0.17f
    val headCenter = Offset(center.x, center.y - size * 0.26f)
    drawScope.drawCircle(
      brush = brush,
      radius = headRadius,
      center = headCenter
    )

    // Specular shine
    drawScope.drawCircle(
      color = highlight.copy(alpha = 0.7f),
      radius = headRadius * 0.28f,
      center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f)
    )
  }

  private fun drawRook(
    drawScope: DrawScope,
    center: Offset,
    size: Float,
    brush: Brush,
    highlight: Color,
    shadow: Color
  ) {
    drawPedestal(drawScope, center, size, brush, shadow)

    // Tower trunk
    val towerPath = Path().apply {
      moveTo(center.x - size * 0.2f, center.y + size * 0.22f)
      lineTo(center.x - size * 0.16f, center.y - size * 0.18f)
      lineTo(center.x + size * 0.16f, center.y - size * 0.18f)
      lineTo(center.x + size * 0.2f, center.y + size * 0.22f)
      close()
    }
    drawScope.drawPath(towerPath, brush)

    // Castle parapet cornice
    drawScope.drawRect(
      brush = brush,
      topLeft = Offset(center.x - size * 0.24f, center.y - size * 0.25f),
      size = Size(size * 0.48f, size * 0.1f)
    )

    // Merlons (3 battlements with embrasures)
    val merlonWidth = size * 0.09f
    val merlonHeight = size * 0.12f
    val merlonY = center.y - size * 0.36f

    // Left merlon
    drawScope.drawRect(brush, Offset(center.x - size * 0.23f, merlonY), Size(merlonWidth, merlonHeight))
    // Center merlon
    drawScope.drawRect(brush, Offset(center.x - merlonWidth * 0.5f, merlonY), Size(merlonWidth, merlonHeight))
    // Right merlon
    drawScope.drawRect(brush, Offset(center.x + size * 0.23f - merlonWidth, merlonY), Size(merlonWidth, merlonHeight))
  }

  private fun drawKnight(
    drawScope: DrawScope,
    center: Offset,
    size: Float,
    brush: Brush,
    highlight: Color,
    shadow: Color,
    isWhite: Boolean
  ) {
    drawPedestal(drawScope, center, size, brush, shadow)

    // Stylized horse head & arched mane
    val horsePath = Path().apply {
      // Start at base right
      moveTo(center.x + size * 0.18f, center.y + size * 0.22f)
      // Arched spine and mane
      cubicTo(
        center.x + size * 0.28f, center.y,
        center.x + size * 0.18f, center.y - size * 0.28f,
        center.x + size * 0.04f, center.y - size * 0.38f // Top of ear
      )
      // Ear tip to brow
      lineTo(center.x - size * 0.02f, center.y - size * 0.32f)
      // Forehead down to snout
      lineTo(center.x - size * 0.24f, center.y - size * 0.14f)
      // Muzzle
      lineTo(center.x - size * 0.22f, center.y - size * 0.04f)
      // Jaw into throat
      quadraticTo(
        center.x - size * 0.08f, center.y - size * 0.02f,
        center.x - size * 0.04f, center.y + size * 0.08f
      )
      // Chest to base left
      lineTo(center.x - size * 0.16f, center.y + size * 0.22f)
      close()
    }
    drawScope.drawPath(horsePath, brush)

    // Eye accent
    drawScope.drawCircle(
      color = highlight.copy(alpha = 0.9f),
      radius = size * 0.028f,
      center = Offset(center.x - size * 0.08f, center.y - size * 0.18f)
    )

    // Mane ridges
    val maneStroke = Stroke(width = size * 0.035f)
    drawScope.drawLine(
      color = highlight.copy(alpha = 0.5f),
      start = Offset(center.x + size * 0.08f, center.y - size * 0.28f),
      end = Offset(center.x + size * 0.16f, center.y - size * 0.24f),
      strokeWidth = maneStroke.width
    )
    drawScope.drawLine(
      color = highlight.copy(alpha = 0.5f),
      start = Offset(center.x + size * 0.12f, center.y - size * 0.16f),
      end = Offset(center.x + size * 0.22f, center.y - size * 0.10f),
      strokeWidth = maneStroke.width
    )
  }

  private fun drawBishop(
    drawScope: DrawScope,
    center: Offset,
    size: Float,
    brush: Brush,
    highlight: Color,
    shadow: Color
  ) {
    drawPedestal(drawScope, center, size, brush, shadow)

    // Waist collar
    drawScope.drawOval(
      brush = brush,
      topLeft = Offset(center.x - size * 0.18f, center.y + size * 0.08f),
      size = Size(size * 0.36f, size * 0.08f)
    )

    // Mitre oval body
    val mitreCenter = Offset(center.x, center.y - size * 0.12f)
    val mitreWidth = size * 0.36f
    val mitreHeight = size * 0.44f

    drawScope.drawOval(
      brush = brush,
      topLeft = Offset(mitreCenter.x - mitreWidth * 0.5f, mitreCenter.y - mitreHeight * 0.5f),
      size = Size(mitreWidth, mitreHeight)
    )

    // Signature diagonal cleft / slit
    val slitStroke = Stroke(width = size * 0.032f)
    drawScope.drawLine(
      color = shadow,
      start = Offset(center.x - size * 0.04f, center.y - size * 0.24f),
      end = Offset(center.x + size * 0.10f, center.y - size * 0.08f),
      strokeWidth = slitStroke.width
    )

    // Top spherical finial
    val finialRadius = size * 0.06f
    val finialCenter = Offset(center.x, center.y - size * 0.38f)
    drawScope.drawCircle(brush, radius = finialRadius, center = finialCenter)
    drawScope.drawCircle(
      color = highlight.copy(alpha = 0.8f),
      radius = finialRadius * 0.4f,
      center = Offset(finialCenter.x - finialRadius * 0.3f, finialCenter.y - finialRadius * 0.3f)
    )
  }

  private fun drawQueen(
    drawScope: DrawScope,
    center: Offset,
    size: Float,
    brush: Brush,
    highlight: Color,
    shadow: Color
  ) {
    drawPedestal(drawScope, center, size, brush, shadow)

    // Flared gown / trunk
    val gownPath = Path().apply {
      moveTo(center.x - size * 0.22f, center.y + size * 0.22f)
      quadraticTo(center.x - size * 0.1f, center.y, center.x - size * 0.18f, center.y - size * 0.18f)
      lineTo(center.x + size * 0.18f, center.y - size * 0.18f)
      quadraticTo(center.x + size * 0.1f, center.y, center.x + size * 0.22f, center.y + size * 0.22f)
      close()
    }
    drawScope.drawPath(gownPath, brush)

    // Coronet with 5 spikes & rounded jewels
    val crownY = center.y - size * 0.36f
    val coronetPath = Path().apply {
      moveTo(center.x - size * 0.22f, center.y - size * 0.18f)
      lineTo(center.x - size * 0.22f, crownY + size * 0.04f)
      lineTo(center.x - size * 0.11f, center.y - size * 0.24f)
      lineTo(center.x, crownY)
      lineTo(center.x + size * 0.11f, center.y - size * 0.24f)
      lineTo(center.x + size * 0.22f, crownY + size * 0.04f)
      lineTo(center.x + size * 0.22f, center.y - size * 0.18f)
      close()
    }
    drawScope.drawPath(coronetPath, brush)

    // Jewel orbs on crown tips
    val orbRadius = size * 0.038f
    drawScope.drawCircle(brush, orbRadius, Offset(center.x - size * 0.22f, crownY + size * 0.04f))
    drawScope.drawCircle(brush, orbRadius, Offset(center.x, crownY))
    drawScope.drawCircle(brush, orbRadius, Offset(center.x + size * 0.22f, crownY + size * 0.04f))

    // Top Queen pearl
    drawScope.drawCircle(
      color = highlight.copy(alpha = 0.9f),
      radius = orbRadius * 0.7f,
      center = Offset(center.x, crownY - size * 0.04f)
    )
  }

  private fun drawKing(
    drawScope: DrawScope,
    center: Offset,
    size: Float,
    brush: Brush,
    highlight: Color,
    shadow: Color
  ) {
    drawPedestal(drawScope, center, size, brush, shadow)

    // Broad royal body
    val bodyPath = Path().apply {
      moveTo(center.x - size * 0.24f, center.y + size * 0.22f)
      quadraticTo(center.x - size * 0.12f, center.y, center.x - size * 0.20f, center.y - size * 0.16f)
      lineTo(center.x + size * 0.20f, center.y - size * 0.16f)
      quadraticTo(center.x + size * 0.12f, center.y, center.x + size * 0.24f, center.y + size * 0.22f)
      close()
    }
    drawScope.drawPath(bodyPath, brush)

    // Royal collar band
    drawScope.drawOval(
      brush = brush,
      topLeft = Offset(center.x - size * 0.22f, center.y - size * 0.18f),
      size = Size(size * 0.44f, size * 0.08f)
    )

    // Dome crown
    val crownCenter = Offset(center.x, center.y - size * 0.28f)
    val crownW = size * 0.38f
    val crownH = size * 0.24f
    drawScope.drawOval(
      brush = brush,
      topLeft = Offset(crownCenter.x - crownW * 0.5f, crownCenter.y - crownH * 0.5f),
      size = Size(crownW, crownH)
    )

    // Royal Cross Finial
    val crossY = center.y - size * 0.44f
    val crossStroke = Stroke(width = size * 0.045f)
    // Vertical beam
    drawScope.drawLine(
      color = highlight,
      start = Offset(center.x, crossY + size * 0.08f),
      end = Offset(center.x, crossY - size * 0.04f),
      strokeWidth = crossStroke.width
    )
    // Horizontal beam
    drawScope.drawLine(
      color = highlight,
      start = Offset(center.x - size * 0.06f, crossY + size * 0.02f),
      end = Offset(center.x + size * 0.06f, crossY + size * 0.02f),
      strokeWidth = crossStroke.width
    )
  }
}
