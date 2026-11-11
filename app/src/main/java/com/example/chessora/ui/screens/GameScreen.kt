package com.example.chessora.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessora.model.GameResult
import com.example.chessora.model.Piece
import com.example.chessora.model.PieceColor
import com.example.chessora.ui.board.ChessBoard3D
import com.example.chessora.ui.components.CapturedPiecesRow
import com.example.chessora.ui.components.GameOverDialog
import com.example.chessora.ui.components.PromotionDialog
import com.example.chessora.ui.theme.ElectricBlue
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
import com.example.chessora.viewmodel.ChessUiState
import com.example.chessora.viewmodel.ChessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
  viewModel: ChessViewModel,
  uiState: ChessUiState,
  onBackClicked: () -> Unit,
  onRematch: () -> Unit,
  onNewGame: () -> Unit
) {
  var showResignConfirm by remember { mutableStateOf(false) }
  var showDrawConfirm by remember { mutableStateOf(false) }

  val game = uiState.game
  val isMyTurn = if (uiState.gameMode == com.example.chessora.viewmodel.GameMode.VS_AI || uiState.gameMode == com.example.chessora.viewmodel.GameMode.CAMPAIGN) {
    game.activeColor == uiState.playerColor
  } else true

  val headerTitle = if (uiState.gameMode == com.example.chessora.viewmodel.GameMode.CAMPAIGN) {
    "CAMPAIGN • LEVEL ${uiState.currentCampaignLevel}"
  } else {
    uiState.gameMode.displayName.uppercase()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = headerTitle,
              color = RoyalGold,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            if (uiState.isAiThinking) {
              Spacer(modifier = Modifier.width(10.dp))
              CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = RoyalGold,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Calculating...",
                color = RoyalGoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClicked) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = TextPrimary
            )
          }
        },
        actions = {
          // 2D / 3D Toggle
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (uiState.is3DMode) RoyalGold else SurfaceDark,
            border = BorderStroke(1.dp, if (uiState.is3DMode) RoyalGold else SurfaceBorder),
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { viewModel.toggle2D3D() }
              .testTag("toggle_3d_button")
          ) {
            Text(
              text = if (uiState.is3DMode) "3D" else "2D",
              color = if (uiState.is3DMode) ObsidianBlack else TextPrimary,
              fontWeight = FontWeight.Black,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          IconButton(
            onClick = { viewModel.rotateBoard() },
            modifier = Modifier.testTag("rotate_board_button")
          ) {
            Icon(
              imageVector = Icons.Default.ScreenRotation,
              contentDescription = "Rotate Board",
              tint = RoyalSilver
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBlack)
      )
    },
    containerColor = ObsidianBlack
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .testTag("gameplay_screen"),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // 1. Opponent Player Header Card
      PlayerHeaderCard(
        name = uiState.opponentName,
        rating = if (uiState.gameMode == com.example.chessora.viewmodel.GameMode.VS_AI) "Elo ${uiState.aiDifficulty.estimatedElo}" else "1200",
        color = uiState.playerColor.opposite(),
        isActiveTurn = game.activeColor == uiState.playerColor.opposite(),
        timeRemainingMs = if (uiState.playerColor == PieceColor.WHITE) uiState.blackTimeRemainingMs else uiState.whiteTimeRemainingMs,
        isClockEnabled = uiState.isClockEnabled,
        capturedPieces = if (uiState.playerColor == PieceColor.WHITE) game.capturedWhitePieces else game.capturedBlackPieces,
        pieceTheme = uiState.pieceTheme
      )

      // 2. Center 3D Board
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        ChessBoard3D(
          game = game,
          selectedSquare = uiState.selectedSquare,
          legalMoves = uiState.legalMovesForSelected,
          lastMove = game.moveHistory.lastOrNull(),
          is3DMode = uiState.is3DMode,
          cameraRotationDegrees = uiState.cameraRotation,
          boardTheme = uiState.boardTheme,
          pieceTheme = uiState.pieceTheme,
          showCoordinates = uiState.showCoordinates,
          hintMove = uiState.currentHint?.move,
          onSquareTapped = { square -> viewModel.onSquareTapped(square) }
        )
      }

      // 3. User Player Bottom Card
      PlayerHeaderCard(
        name = "You",
        rating = "1200",
        color = uiState.playerColor,
        isActiveTurn = game.activeColor == uiState.playerColor,
        timeRemainingMs = if (uiState.playerColor == PieceColor.WHITE) uiState.whiteTimeRemainingMs else uiState.blackTimeRemainingMs,
        isClockEnabled = uiState.isClockEnabled,
        capturedPieces = if (uiState.playerColor == PieceColor.WHITE) game.capturedBlackPieces else game.capturedWhitePieces,
        pieceTheme = uiState.pieceTheme
      )

      // 4. Move History Strip
      if (game.moveHistory.isNotEmpty()) {
        MoveHistoryStrip(game.moveHistory)
      }

      // 5. Tactical Hint Banner (if requested)
      AnimatedVisibility(visible = uiState.currentHint != null) {
        uiState.currentHint?.let { hint ->
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceDark,
            border = BorderStroke(1.5.dp, RoyalGold),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp)
              .testTag("hint_banner")
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = CircleShape,
                    color = RoyalGold.copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = RoyalGold,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "RECOMMENDED: ${hint.moveTitle.uppercase()}",
                      color = RoyalGold,
                      fontWeight = FontWeight.Black,
                      fontSize = 13.sp
                    )
                    Text(
                      text = "${hint.pieceName} ko ${hint.fromAlgebraic} se ${hint.toAlgebraic} par move karein",
                      color = RoyalGoldLight,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 11.sp
                    )
                  }
                }

                IconButton(
                  onClick = { viewModel.dismissHint() },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = hint.explanation,
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp
              )

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = com.example.chessora.ui.theme.EmeraldGreen,
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.applyHintMove() }
                    .testTag("play_hint_move_button")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "PLAY THIS MOVE",
                      color = ObsidianBlack,
                      fontWeight = FontWeight.Black,
                      fontSize = 12.sp
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 6. Action Bar Controls
      GameplayActionBar(
        onUndo = { viewModel.undoMove() },
        onHint = { viewModel.requestHint() },
        onDraw = { showDrawConfirm = true },
        onResign = { showResignConfirm = true },
        canUndo = game.moveHistory.isNotEmpty() && !uiState.isAiThinking,
        canHint = isMyTurn && !uiState.isAiThinking && game.gameResult == GameResult.IN_PROGRESS
      )
    }

    // Pawn Promotion Dialog
    if (uiState.pendingPromotionMove != null) {
      PromotionDialog(
        color = uiState.pendingPromotionMove!!.piece.color,
        pieceTheme = uiState.pieceTheme,
        onPieceSelected = { promoType -> viewModel.selectPromotion(promoType) },
        onDismiss = { viewModel.dismissPromotion() }
      )
    }

    // Game Over Dialog
    if (uiState.isGameOverDialogVisible) {
      GameOverDialog(
        result = game.gameResult,
        winner = game.winner,
        playerColor = uiState.playerColor,
        xpEarned = uiState.xpEarnedInMatch,
        leveledUp = uiState.leveledUpInMatch,
        movesCount = game.moveHistory.size,
        isCampaignMode = uiState.gameMode == com.example.chessora.viewmodel.GameMode.CAMPAIGN,
        campaignLevel = uiState.currentCampaignLevel,
        starsEarned = uiState.campaignStarsEarned,
        nextLevelUnlocked = uiState.nextLevelUnlocked,
        onNextLevel = {
          viewModel.dismissGameOverDialog()
          viewModel.playNextCampaignLevel()
        },
        onRematch = onRematch,
        onNewGame = onNewGame,
        onHome = {
          viewModel.dismissGameOverDialog()
          onBackClicked()
        }
      )
    }

    // Resign Confirmation Dialog
    if (showResignConfirm) {
      AlertDialog(
        onDismissRequest = { showResignConfirm = false },
        title = { Text("Resign Match?", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to concede this game to your opponent?", color = TextSecondary) },
        confirmButton = {
          TextButton(onClick = {
            showResignConfirm = false
            viewModel.resign()
          }) {
            Text("Resign", color = SoftRubyRed, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showResignConfirm = false }) {
            Text("Continue", color = TextSecondary)
          }
        },
        containerColor = GraphiteDark
      )
    }

    // Draw Offer Dialog
    if (showDrawConfirm) {
      AlertDialog(
        onDismissRequest = { showDrawConfirm = false },
        title = { Text("Offer Draw?", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = { Text("Propose an honorable draw to end this game?", color = TextSecondary) },
        confirmButton = {
          TextButton(onClick = {
            showDrawConfirm = false
            viewModel.agreeDraw()
          }) {
            Text("Offer Draw", color = RoyalGold, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showDrawConfirm = false }) {
            Text("Cancel", color = TextSecondary)
          }
        },
        containerColor = GraphiteDark
      )
    }
  }
}

@Composable
fun PlayerHeaderCard(
  name: String,
  rating: String,
  color: PieceColor,
  isActiveTurn: Boolean,
  timeRemainingMs: Long,
  isClockEnabled: Boolean,
  capturedPieces: List<Piece>,
  pieceTheme: com.example.chessora.ui.board.PieceThemeType
) {
  val isLowTime = isClockEnabled && timeRemainingMs in 1..30_000L
  val timeSec = (timeRemainingMs / 1000).coerceAtLeast(0)
  val mins = timeSec / 60
  val secs = timeSec % 60
  val timeFormatted = String.format("%02d:%02d", mins, secs)

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isActiveTurn) SurfaceDark else GraphiteDark,
    border = BorderStroke(
      width = if (isActiveTurn) 1.5.dp else 1.dp,
      color = if (isActiveTurn) RoyalGold else SurfaceBorder
    ),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Color emblem
        Surface(
          shape = CircleShape,
          color = if (color == PieceColor.WHITE) Color(0xFFD6AF62) else Color(0xFF1E2430),
          border = BorderStroke(1.dp, SurfaceBorder),
          modifier = Modifier.size(24.dp)
        ) {}

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = name,
              color = if (isActiveTurn) RoyalGoldLight else TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "($rating)",
              color = TextMuted,
              fontSize = 11.sp
            )
          }

          if (capturedPieces.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "+${capturedPieces.size} pcs",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }

      // Timer Pill
      if (isClockEnabled) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = when {
            isLowTime -> SoftRubyRed.copy(alpha = 0.2f)
            isActiveTurn -> RoyalGold.copy(alpha = 0.18f)
            else -> SurfaceElevated
          },
          border = BorderStroke(
            1.dp,
            if (isLowTime) SoftRubyRed else if (isActiveTurn) RoyalGold else SurfaceBorder
          )
        ) {
          Text(
            text = timeFormatted,
            color = if (isLowTime) SoftRubyRed else if (isActiveTurn) RoyalGold else TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }
    }
  }
}

@Composable
fun MoveHistoryStrip(moves: List<com.example.chessora.model.Move>) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = GraphiteDark,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 2.dp)
  ) {
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      itemsIndexed(moves) { index, move ->
        val moveNum = (index / 2) + 1
        val isWhite = index % 2 == 0
        val text = if (isWhite) "$moveNum. ${move.san}" else move.san
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = if (index == moves.lastIndex) RoyalGold.copy(alpha = 0.25f) else SurfaceDark,
          modifier = Modifier.padding(horizontal = 2.dp)
        ) {
          Text(
            text = text,
            color = if (index == moves.lastIndex) RoyalGoldLight else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}

@Composable
fun GameplayActionBar(
  onUndo: () -> Unit,
  onHint: () -> Unit,
  onDraw: () -> Unit,
  onResign: () -> Unit,
  canUndo: Boolean,
  canHint: Boolean
) {
  Surface(
    color = GraphiteDark,
    border = BorderStroke(1.dp, SurfaceBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      ActionBarItem(
        icon = Icons.Default.Undo,
        label = "Undo",
        onClick = onUndo,
        enabled = canUndo,
        testTag = "undo_button"
      )

      ActionBarItem(
        icon = Icons.Default.Lightbulb,
        label = "Hint",
        onClick = onHint,
        enabled = canHint,
        testTag = "hint_button"
      )

      ActionBarItem(
        icon = Icons.Default.Handshake,
        label = "Draw",
        onClick = onDraw,
        enabled = true,
        testTag = "draw_button"
      )

      ActionBarItem(
        icon = Icons.Default.Flag,
        label = "Resign",
        onClick = onResign,
        enabled = true,
        testTag = "resign_button"
      )
    }
  }
}

@Composable
fun ActionBarItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  onClick: () -> Unit,
  enabled: Boolean,
  testTag: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable(enabled = enabled) { onClick() }
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .testTag(testTag)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (enabled) RoyalGold else TextMuted,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      color = if (enabled) TextPrimary else TextMuted,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
