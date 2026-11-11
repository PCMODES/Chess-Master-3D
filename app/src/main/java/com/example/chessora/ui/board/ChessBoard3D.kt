package com.example.chessora.ui.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.chessora.model.ChessGame
import com.example.chessora.model.Move
import com.example.chessora.model.PieceColor
import com.example.chessora.model.Square
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChessBoard3D(
  game: ChessGame,
  selectedSquare: Square?,
  legalMoves: List<Move>,
  lastMove: Move?,
  is3DMode: Boolean = true,
  cameraRotationDegrees: Float = 0f, // 0 = White on bottom, 180 = Black on bottom
  boardTheme: BoardThemeType = BoardThemeType.OBSIDIAN_GOLD,
  pieceTheme: PieceThemeType = PieceThemeType.ROYAL_GOLD,
  showCoordinates: Boolean = true,
  hintMove: Move? = null,
  onSquareTapped: (Square) -> Unit,
  modifier: Modifier = Modifier
) {
  // Pulsing animation for check danger & selected piece
  val infiniteTransition = rememberInfiniteTransition(label = "BoardPulsing")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "PulseAlpha"
  )

  // Piece lift animation when selected
  val liftAnim = remember { Animatable(0f) }
  LaunchedEffect(selectedSquare) {
    if (selectedSquare != null) {
      liftAnim.animateTo(12f, tween(180, easing = FastOutSlowInEasing))
    } else {
      liftAnim.animateTo(0f, tween(120))
    }
  }

  val isWhiteKingInCheck = game.isCheck(PieceColor.WHITE)
  val isBlackKingInCheck = game.isCheck(PieceColor.BLACK)
  val whiteKingSq = if (isWhiteKingInCheck) game.findKing(PieceColor.WHITE) else null
  val blackKingSq = if (isBlackKingInCheck) game.findKing(PieceColor.BLACK) else null

  val isFlipped = cameraRotationDegrees >= 90f && cameraRotationDegrees <= 270f

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(if (is3DMode) 1.0f else 1.0f)
      .padding(8.dp)
      .testTag("chess_board_canvas")
  ) {
    val widthPx = constraints.maxWidth.toFloat()
    val heightPx = constraints.maxHeight.toFloat()

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1.0f)
        .pointerInput(isFlipped, is3DMode) {
          detectTapGestures { tapOffset ->
            val boardPadding = widthPx * 0.06f
            val playableSize = widthPx - 2 * boardPadding
            val cellSize = playableSize / 8f

            // Inverse projection
            var relX = (tapOffset.x - boardPadding) / cellSize
            var relY = (tapOffset.y - boardPadding) / cellSize

            if (is3DMode) {
              // Inverse 3D perspective mapping
              val centerY = heightPx * 0.5f
              val perspectiveFactor = 0.82f
              val adjustedY = (tapOffset.y - (centerY - playableSize * 0.5f * perspectiveFactor)) / (playableSize * perspectiveFactor)
              relY = adjustedY * 8f
            }

            val col = relX.toInt()
            val row = relY.toInt()

            if (col in 0..7 && row in 0..7) {
              val actualCol = if (isFlipped) 7 - col else col
              val actualRow = if (isFlipped) row else 7 - row
              onSquareTapped(Square(actualCol, actualRow))
            }
          }
        }
    ) {
      val canvasW = size.width
      val canvasH = size.height
      val borderThickness = canvasW * 0.055f
      val boardSize = canvasW - 2 * borderThickness
      val cellSize = boardSize / 8f

      val pitchScaleY = if (is3DMode) 0.85f else 1.0f
      val boardTopY = if (is3DMode) (canvasH - boardSize * pitchScaleY) * 0.5f - canvasH * 0.02f else borderThickness

      // 1. Draw 3D Wooden/Obsidian Border with Bevel Depth
      if (is3DMode) {
        val bevelDepth = borderThickness * 0.45f
        val bottomEdgePath = Path().apply {
          moveTo(0f, boardTopY + boardSize * pitchScaleY + borderThickness)
          lineTo(borderThickness, boardTopY + boardSize * pitchScaleY)
          lineTo(canvasW - borderThickness, boardTopY + boardSize * pitchScaleY)
          lineTo(canvasW, boardTopY + boardSize * pitchScaleY + borderThickness)
          lineTo(canvasW, boardTopY + boardSize * pitchScaleY + borderThickness + bevelDepth)
          lineTo(0f, boardTopY + boardSize * pitchScaleY + borderThickness + bevelDepth)
          close()
        }
        drawPath(bottomEdgePath, color = boardTheme.borderOuter.copy(alpha = 0.95f))
      }

      // Outer border frame
      drawRect(
        color = boardTheme.borderOuter,
        topLeft = Offset(0f, boardTopY - borderThickness * (if (is3DMode) 0.5f else 1.0f)),
        size = Size(canvasW, boardSize * pitchScaleY + 2 * borderThickness)
      )

      // Inner metallic gold/silver accent line
      drawRect(
        color = boardTheme.borderInner,
        topLeft = Offset(borderThickness * 0.6f, boardTopY - borderThickness * 0.2f),
        size = Size(canvasW - borderThickness * 1.2f, boardSize * pitchScaleY + borderThickness * 0.4f),
        style = Stroke(width = 2.5f)
      )

      // 2. Draw Squares
      for (displayRow in 0..7) {
        for (displayCol in 0..7) {
          val actualCol = if (isFlipped) 7 - displayCol else displayCol
          val actualRow = if (isFlipped) displayRow else 7 - displayRow
          val sq = Square(actualCol, actualRow)

          val sqLeft = borderThickness + displayCol * cellSize
          val sqTop = boardTopY + displayRow * cellSize * pitchScaleY
          val sqWidth = cellSize
          val sqHeight = cellSize * pitchScaleY

          val isLight = sq.isLight
          val sqColor = if (isLight) boardTheme.lightSquare else boardTheme.darkSquare

          // Base square fill
          drawRect(
            color = sqColor,
            topLeft = Offset(sqLeft, sqTop),
            size = Size(sqWidth, sqHeight)
          )

          // Last move indicator
          if (lastMove != null && (lastMove.from == sq || lastMove.to == sq)) {
            drawRect(
              color = Color(0x66FFD700), // Amber gold glow
              topLeft = Offset(sqLeft, sqTop),
              size = Size(sqWidth, sqHeight)
            )
          }

          // Selected square indicator
          if (selectedSquare == sq) {
            drawRect(
              color = Color(0x88FFD700).copy(alpha = pulseAlpha),
              topLeft = Offset(sqLeft, sqTop),
              size = Size(sqWidth, sqHeight)
            )
            drawRect(
              color = Color(0xFFFFF2A8),
              topLeft = Offset(sqLeft + 2f, sqTop + 2f),
              size = Size(sqWidth - 4f, sqHeight - 4f),
              style = Stroke(width = 3.dp.toPx())
            )
          }

          // King in check warning indicator
          if (sq == whiteKingSq || sq == blackKingSq) {
            drawRect(
              color = Color(0x99FF2E2E).copy(alpha = pulseAlpha),
              topLeft = Offset(sqLeft, sqTop),
              size = Size(sqWidth, sqHeight)
            )
          }

          // Legal move highlights
          val matchingLegal = legalMoves.find { it.to == sq }
          if (matchingLegal != null) {
            val sqCenter = Offset(sqLeft + sqWidth * 0.5f, sqTop + sqHeight * 0.5f)
            if (matchingLegal.capturedPiece != null || matchingLegal.isEnPassant) {
              // Capture ring
              drawCircle(
                color = Color(0xDDFF4D4D),
                radius = cellSize * 0.42f,
                center = sqCenter,
                style = Stroke(width = 3.5.dp.toPx())
              )
            } else {
              // Move destination dot
              drawCircle(
                color = Color(0xCC648BFF),
                radius = cellSize * 0.16f,
                center = sqCenter
              )
              drawCircle(
                color = Color(0xFFFFFFFF),
                radius = cellSize * 0.08f,
                center = sqCenter
              )
            }
          }

          // Hint markers: FROM and TO
          if (hintMove != null) {
            if (sq == hintMove.from) {
              drawRect(
                color = Color(0x66FFD700).copy(alpha = pulseAlpha),
                topLeft = Offset(sqLeft, sqTop),
                size = Size(sqWidth, sqHeight)
              )
              drawRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(sqLeft + 2f, sqTop + 2f),
                size = Size(sqWidth - 4f, sqHeight - 4f),
                style = Stroke(width = 3.dp.toPx())
              )
            } else if (sq == hintMove.to) {
              drawRect(
                color = Color(0x662ECC71).copy(alpha = pulseAlpha),
                topLeft = Offset(sqLeft, sqTop),
                size = Size(sqWidth, sqHeight)
              )
              drawRect(
                color = Color(0xFF2ECC71),
                topLeft = Offset(sqLeft + 2f, sqTop + 2f),
                size = Size(sqWidth - 4f, sqHeight - 4f),
                style = Stroke(width = 3.dp.toPx())
              )
            }
          }
        }
      }

      // Draw Hint Directional Arrow
      if (hintMove != null) {
        val fromDisplayCol = if (isFlipped) 7 - hintMove.from.col else hintMove.from.col
        val fromDisplayRow = if (isFlipped) hintMove.from.row else 7 - hintMove.from.row
        val toDisplayCol = if (isFlipped) 7 - hintMove.to.col else hintMove.to.col
        val toDisplayRow = if (isFlipped) hintMove.to.row else 7 - hintMove.to.row

        val fromCenter = Offset(
          borderThickness + fromDisplayCol * cellSize + cellSize * 0.5f,
          boardTopY + fromDisplayRow * cellSize * pitchScaleY + cellSize * pitchScaleY * 0.5f
        )
        val toCenter = Offset(
          borderThickness + toDisplayCol * cellSize + cellSize * 0.5f,
          boardTopY + toDisplayRow * cellSize * pitchScaleY + cellSize * pitchScaleY * 0.5f
        )

        drawLine(
          color = Color(0xFF2ECC71).copy(alpha = 0.85f),
          start = fromCenter,
          end = toCenter,
          strokeWidth = 4.dp.toPx()
        )
        drawCircle(
          color = Color(0xFF2ECC71),
          radius = cellSize * 0.22f,
          center = toCenter,
          style = Stroke(width = 3.dp.toPx())
        )
      }

      // 3. Draw Coordinates
      if (showCoordinates) {
        val paint = android.graphics.Paint().apply {
          color = boardTheme.coordinateColor.hashCode()
          textSize = cellSize * 0.22f
          isAntiAlias = true
          textAlign = android.graphics.Paint.Align.CENTER
          typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        // Files 'a' - 'h' at bottom
        for (col in 0..7) {
          val fileChar = if (isFlipped) ('h' - col) else ('a' + col)
          val x = borderThickness + col * cellSize + cellSize * 0.5f
          val y = boardTopY + boardSize * pitchScaleY + borderThickness * 0.65f
          drawContext.canvas.nativeCanvas.drawText(fileChar.toString(), x, y, paint)
        }

        // Ranks '1' - '8' at left
        for (row in 0..7) {
          val rankNum = if (isFlipped) (row + 1) else (8 - row)
          val x = borderThickness * 0.45f
          val y = boardTopY + row * cellSize * pitchScaleY + cellSize * pitchScaleY * 0.6f
          drawContext.canvas.nativeCanvas.drawText(rankNum.toString(), x, y, paint)
        }
      }

      // 4. Draw Pieces (sorted from back row to front row for correct 3D occlusion)
      for (displayRow in 0..7) {
        for (displayCol in 0..7) {
          val actualCol = if (isFlipped) 7 - displayCol else displayCol
          val actualRow = if (isFlipped) displayRow else 7 - displayRow
          val sq = Square(actualCol, actualRow)
          val piece = game.getPiece(sq) ?: continue

          val sqLeft = borderThickness + displayCol * cellSize
          val sqTop = boardTopY + displayRow * cellSize * pitchScaleY
          val pieceCenter = Offset(sqLeft + cellSize * 0.5f, sqTop + cellSize * pitchScaleY * 0.5f)

          val elevation = if (sq == selectedSquare) liftAnim.value.dp.toPx() else 0f

          PieceRenderer3D.drawPiece(
            drawScope = this,
            piece = piece,
            center = pieceCenter,
            size = cellSize * 0.92f,
            pieceTheme = pieceTheme,
            is3D = is3DMode,
            elevationPx = elevation
          )
        }
      }
    }
  }
}
