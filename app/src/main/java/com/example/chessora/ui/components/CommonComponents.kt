package com.example.chessora.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chessora.model.GameResult
import com.example.chessora.model.Piece
import com.example.chessora.model.PieceColor
import com.example.chessora.model.PieceType
import com.example.chessora.ui.board.PieceRenderer3D
import com.example.chessora.ui.board.PieceThemeType
import com.example.chessora.ui.theme.ElectricBlue
import com.example.chessora.ui.theme.EmeraldGreen
import com.example.chessora.ui.theme.EmeraldLight
import com.example.chessora.ui.theme.GraphiteDark
import com.example.chessora.ui.theme.ObsidianBlack
import com.example.chessora.ui.theme.RoyalGold
import com.example.chessora.ui.theme.RoyalGoldLight
import com.example.chessora.ui.theme.RoyalSilver
import com.example.chessora.ui.theme.SoftRubyRed
import com.example.chessora.ui.theme.SurfaceBorder
import com.example.chessora.ui.theme.SurfaceDark
import com.example.chessora.ui.theme.SurfaceElevated
import com.example.chessora.ui.theme.TextMuted
import com.example.chessora.ui.theme.TextPrimary
import com.example.chessora.ui.theme.TextSecondary
import java.security.MessageDigest

@Composable
fun LuxuryCard(
  modifier: Modifier = Modifier,
  borderColor: Color = SurfaceBorder,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val baseModifier = modifier
    .clip(RoundedCornerShape(18.dp))
    .background(
      Brush.verticalGradient(
        colors = listOf(SurfaceDark, GraphiteDark)
      )
    )

  val clickableModifier = if (onClick != null) {
    baseModifier.clickable { onClick() }
  } else {
    baseModifier
  }

  Card(
    modifier = clickableModifier,
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.2.dp, borderColor),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
  ) {
    Box(modifier = Modifier.padding(18.dp)) {
      content()
    }
  }
}

@Composable
fun GoldButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  testTag: String = "gold_button"
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier
      .height(54.dp)
      .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = RoyalGold,
      contentColor = ObsidianBlack,
      disabledContainerColor = SurfaceElevated,
      disabledContentColor = TextMuted
    ),
    border = BorderStroke(1.5.dp, RoyalGoldLight)
  ) {
    if (icon != null) {
      Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = ObsidianBlack)
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      fontWeight = FontWeight.Black,
      fontSize = 15.sp,
      letterSpacing = 0.5.sp,
      color = if (enabled) ObsidianBlack else TextMuted
    )
  }
}

@Composable
fun DarkOutlinedButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  testTag: String = "dark_outlined_button"
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .height(50.dp)
      .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = SurfaceDark.copy(alpha = 0.85f),
      contentColor = TextPrimary
    ),
    border = BorderStroke(1.2.dp, SurfaceBorder)
  ) {
    if (icon != null) {
      Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(19.dp), tint = RoyalGold)
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp,
      color = TextPrimary
    )
  }
}

@Composable
fun LevelProgressBar(
  level: Int,
  xp: Int,
  modifier: Modifier = Modifier
) {
  val requiredXp = level * 250
  val progress = (xp.toFloat() / requiredXp.toFloat()).coerceIn(0f, 1f)

  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          color = RoyalGold,
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.padding(end = 8.dp)
        ) {
          Text(
            text = "LVL $level",
            color = ObsidianBlack,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
        Text(
          text = getLevelTitle(level),
          color = RoyalGoldLight,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp
        )
      }
      Text(
        text = "$xp / $requiredXp XP",
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    LinearProgressIndicator(
      progress = { progress },
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp)),
      color = RoyalGold,
      trackColor = SurfaceElevated
    )
  }
}

fun getLevelTitle(level: Int): String {
  return when {
    level >= 100 -> "Immortal Grandmaster"
    level >= 75 -> "Grandmaster"
    level >= 50 -> "International Master"
    level >= 30 -> "Chess Master"
    level >= 20 -> "Expert Strategist"
    level >= 10 -> "Tactician"
    level >= 5 -> "Apprentice"
    else -> "Novice Strategist"
  }
}

@Composable
fun PromotionDialog(
  color: PieceColor,
  pieceTheme: PieceThemeType,
  onPieceSelected: (PieceType) -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = GraphiteDark,
      border = BorderStroke(1.5.dp, RoyalGold),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("promotion_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "PAWN PROMOTION",
          color = RoyalGold,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          letterSpacing = 1.sp
        )
        Text(
          text = "Select your royal reinforcement",
          color = TextSecondary,
          fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          val pieces = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
          for (pType in pieces) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .clickable { onPieceSelected(pType) }
                .padding(12.dp)
                .testTag("promo_${pType.name.lowercase()}")
            ) {
              Canvas(modifier = Modifier.size(48.dp)) {
                PieceRenderer3D.drawPiece(
                  drawScope = this,
                  piece = Piece(pType, color),
                  center = Offset(size.width * 0.5f, size.height * 0.5f),
                  size = size.width * 0.95f,
                  pieceTheme = pieceTheme,
                  is3D = true
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = pType.displayName,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun GameOverDialog(
  result: GameResult,
  winner: PieceColor?,
  playerColor: PieceColor,
  xpEarned: Int,
  leveledUp: Boolean,
  movesCount: Int,
  isCampaignMode: Boolean = false,
  campaignLevel: Int = 1,
  starsEarned: Int = 0,
  nextLevelUnlocked: Boolean = false,
  onNextLevel: (() -> Unit)? = null,
  onRematch: () -> Unit,
  onNewGame: () -> Unit,
  onHome: () -> Unit
) {
  val isWin = winner == playerColor
  val isDraw = winner == null && result != GameResult.IN_PROGRESS

  val title = when {
    isCampaignMode && isWin -> "LEVEL $campaignLevel COMPLETE!"
    isWin -> "VICTORY"
    isDraw -> "DRAW"
    else -> "DEFEAT"
  }

  val subtitle = when {
    isCampaignMode && isWin -> "Level ${campaignLevel + 1} is now unlocked!"
    result == GameResult.CHECKMATE -> if (isWin) "Checkmate delivered!" else "You were checkmated."
    result == GameResult.RESIGNATION -> if (isWin) "Opponent resigned." else "You resigned."
    result == GameResult.TIMEOUT -> if (isWin) "Opponent flagged on time." else "Time expired."
    result == GameResult.STALEMATE -> "Draw by Stalemate."
    result == GameResult.DRAW_FIFTY_MOVES -> "Draw by 50-move rule."
    result == GameResult.DRAW_REPETITION -> "Draw by Threefold Repetition."
    result == GameResult.DRAW_INSUFFICIENT_MATERIAL -> "Draw by Insufficient Material."
    result == GameResult.DRAW_AGREEMENT -> "Draw agreed by mutual consent."
    else -> "Game concluded."
  }

  val bannerColor = when {
    isWin -> RoyalGold
    isDraw -> ElectricBlue
    else -> SoftRubyRed
  }

  Dialog(onDismissRequest = {}) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = ObsidianBlack,
      border = BorderStroke(2.dp, bannerColor),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("game_over_dialog")
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Icon
        Surface(
          shape = CircleShape,
          color = bannerColor.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, bannerColor),
          modifier = Modifier.size(60.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (isWin) Icons.Default.EmojiEvents else Icons.Default.Star,
              contentDescription = null,
              tint = bannerColor,
              modifier = Modifier.size(34.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = title,
          color = bannerColor,
          fontWeight = FontWeight.Black,
          fontSize = if (isCampaignMode && isWin) 22.sp else 26.sp,
          letterSpacing = 1.sp,
          textAlign = TextAlign.Center
        )

        // Campaign Stars Display (⭐⭐⭐)
        if (isCampaignMode && isWin) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val stars = if (starsEarned > 0) starsEarned else 3
            for (i in 1..3) {
              val isEarned = i <= stars
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (isEarned) RoyalGold else SurfaceElevated,
                modifier = Modifier
                  .size(32.dp)
                  .padding(horizontal = 2.dp)
              )
            }
          }
        }

        Text(
          text = subtitle,
          color = TextSecondary,
          fontSize = 13.sp,
          textAlign = TextAlign.Center
        )

        // Unlocked Next Level Banner
        if (isCampaignMode && isWin && nextLevelUnlocked) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = EmeraldGreen.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, EmeraldGreen)
          ) {
            Text(
              text = "🔓 LEVEL ${campaignLevel + 1} UNLOCKED!",
              color = EmeraldLight,
              fontWeight = FontWeight.Black,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // XP & Moves Card
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = SurfaceDark,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "XP EARNED", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              Text(text = "+$xpEarned XP", color = RoyalGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "TOTAL MOVES", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              Text(text = "$movesCount", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
          }
        }

        if (leveledUp) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = RoyalGold.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, RoyalGold)
          ) {
            Text(
              text = "⭐ LEVEL UP! NEW STRATEGIST RANK ⭐",
              color = RoyalGoldLight,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Buttons
        if (isCampaignMode && isWin && onNextLevel != null) {
          GoldButton(
            text = "PLAY LEVEL ${campaignLevel + 1} ▶",
            onClick = onNextLevel,
            modifier = Modifier.fillMaxWidth(),
            testTag = "next_level_button"
          )
          Spacer(modifier = Modifier.height(8.dp))
        }

        GoldButton(
          text = if (isCampaignMode && isWin) "RETRY LEVEL $campaignLevel" else "PLAY REMATCH",
          onClick = onRematch,
          modifier = Modifier.fillMaxWidth(),
          testTag = "rematch_button"
        )

        Spacer(modifier = Modifier.height(8.dp))

        DarkOutlinedButton(
          text = if (isCampaignMode) "CAMPAIGN MAP" else "NEW OPPONENT",
          onClick = onNewGame,
          modifier = Modifier.fillMaxWidth(),
          testTag = "new_game_button"
        )

        Spacer(modifier = Modifier.height(8.dp))

        DarkOutlinedButton(
          text = "BACK TO HOME",
          onClick = onHome,
          modifier = Modifier.fillMaxWidth(),
          testTag = "home_button"
        )
      }
    }
  }
}

@Composable
fun CapturedPiecesRow(
  capturedWhite: List<Piece>,
  capturedBlack: List<Piece>,
  pieceTheme: PieceThemeType,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // White captures
    Row(verticalAlignment = Alignment.CenterVertically) {
      val whitePawns = capturedWhite.count { it.type == PieceType.PAWN }
      val whiteOthers = capturedWhite.filter { it.type != PieceType.PAWN }
      for (p in whiteOthers.take(5)) {
        MiniPiece(p, pieceTheme)
      }
      if (whitePawns > 0) {
        Text(
          text = "x$whitePawns P",
          color = TextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 4.dp)
        )
      }
    }

    // Black captures
    Row(verticalAlignment = Alignment.CenterVertically) {
      val blackPawns = capturedBlack.count { it.type == PieceType.PAWN }
      val blackOthers = capturedBlack.filter { it.type != PieceType.PAWN }
      for (p in blackOthers.take(5)) {
        MiniPiece(p, pieceTheme)
      }
      if (blackPawns > 0) {
        Text(
          text = "x$blackPawns P",
          color = TextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 4.dp)
        )
      }
    }
  }
}

@Composable
fun MiniPiece(piece: Piece, pieceTheme: PieceThemeType) {
  Canvas(modifier = Modifier.size(20.dp).padding(1.dp)) {
    PieceRenderer3D.drawPiece(
      drawScope = this,
      piece = piece,
      center = Offset(size.width * 0.5f, size.height * 0.5f),
      size = size.width * 0.95f,
      pieceTheme = pieceTheme,
      is3D = false
    )
  }
}

@Composable
fun QrCodeCanvas(
  payload: String,
  modifier: Modifier = Modifier
) {
  val qrBitmap = remember(payload) {
    try {
      val cleanPayload = if (payload.isBlank()) "CHESSORA|127.0.0.1|8887|BLACK|ROOM" else payload
      val hints = mapOf(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
      )
      val bitMatrix = QRCodeWriter().encode(cleanPayload, BarcodeFormat.QR_CODE, 512, 512, hints)
      val w = bitMatrix.width
      val h = bitMatrix.height
      val pixels = IntArray(w * h)
      for (y in 0 until h) {
        val offset = y * w
        for (x in 0 until w) {
          pixels[offset + x] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
      }
      val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
      bmp.setPixels(pixels, 0, w, 0, 0, w, h)
      bmp.asImageBitmap()
    } catch (_: Exception) {
      null
    }
  }

  Box(
    modifier = modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(14.dp))
      .background(Color.White)
      .padding(14.dp)
      .testTag("qr_code_display"),
    contentAlignment = Alignment.Center
  ) {
    if (qrBitmap != null) {
      Image(
        bitmap = qrBitmap,
        contentDescription = "Room QR Code",
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}
