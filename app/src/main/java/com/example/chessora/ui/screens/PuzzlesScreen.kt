package com.example.chessora.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chessora.data.PuzzleEntity
import com.example.chessora.model.PieceColor
import com.example.chessora.ui.board.BoardThemeType
import com.example.chessora.ui.board.ChessBoard3D
import com.example.chessora.ui.board.PieceThemeType
import com.example.chessora.ui.components.DarkOutlinedButton
import com.example.chessora.ui.components.GoldButton
import com.example.chessora.ui.components.LuxuryCard
import com.example.chessora.ui.components.PromotionDialog
import com.example.chessora.ui.theme.EmeraldGreen
import com.example.chessora.ui.theme.GraphiteDark
import com.example.chessora.ui.theme.ObsidianBlack
import com.example.chessora.ui.theme.RoyalGold
import com.example.chessora.ui.theme.RoyalGoldLight
import com.example.chessora.ui.theme.SoftRubyRed
import com.example.chessora.ui.theme.SurfaceBorder
import com.example.chessora.ui.theme.SurfaceDark
import com.example.chessora.ui.theme.SurfaceElevated
import com.example.chessora.ui.theme.TextMuted
import com.example.chessora.ui.theme.TextPrimary
import com.example.chessora.ui.theme.TextSecondary
import com.example.chessora.viewmodel.PuzzleStatus
import com.example.chessora.viewmodel.PuzzlesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzlesScreen(
  viewModel: PuzzlesViewModel,
  onBackClicked: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val allPuzzles by viewModel.allPuzzles.collectAsStateWithLifecycle()

  val activePuzzle = uiState.activePuzzle
  val game = uiState.game

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "DAILY TACTICAL PUZZLES",
            color = RoyalGold,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBlack)
      )
    },
    containerColor = ObsidianBlack
  ) { padding ->
    var selectedCategory by remember { mutableStateOf("ALL") }
    val todayDaily = viewModel.getDailyPuzzle(allPuzzles)

    val filteredPuzzles = when (selectedCategory) {
      "MATE_1" -> allPuzzles.filter { it.theme == "Mate in 1" }
      "MATE_2" -> allPuzzles.filter { it.theme == "Mate in 2" }
      "FORKS" -> allPuzzles.filter { it.theme == "Forks & Pins" || it.theme == "Fork Attack" }
      "ENDGAME" -> allPuzzles.filter { it.theme == "Endgame" || it.theme == "Tactics" }
      else -> allPuzzles
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .testTag("puzzles_screen"),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Today's Daily Challenge Card
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceDark,
        border = BorderStroke(1.2.dp, RoyalGold),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .testTag("daily_challenge_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = RoyalGold,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "TODAY'S DAILY CHALLENGE",
                color = RoyalGold,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 1.sp
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = RoyalGold.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, RoyalGold)
            ) {
              Text(
                text = "ROTATES DAILY",
                color = RoyalGoldLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = viewModel.todayFormatted,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )

          Spacer(modifier = Modifier.height(10.dp))
          todayDaily?.let { td ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = td.title,
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
                Text(
                  text = "${td.theme} • Rating ${td.rating} • +${td.xpReward} XP",
                  color = RoyalGoldLight,
                  fontSize = 12.sp
                )
              }

              if (activePuzzle?.id == td.id) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = EmeraldGreen.copy(alpha = 0.2f),
                  border = BorderStroke(1.dp, EmeraldGreen)
                ) {
                  Text(
                    text = if (td.isCompleted) "✓ SOLVED" else "ACTIVE",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              } else {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = RoyalGold,
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.loadPuzzle(td) }
                ) {
                  Text(
                    text = "PLAY TODAY'S",
                    color = ObsidianBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 2. Category Filter Chips
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val categories = listOf(
          Pair("ALL", "All Puzzles (${allPuzzles.size})"),
          Pair("MATE_1", "Mate in 1"),
          Pair("MATE_2", "Mate in 2"),
          Pair("FORKS", "Forks & Pins"),
          Pair("ENDGAME", "Endgames")
        )
        items(categories) { (catId, label) ->
          val isSel = selectedCategory == catId
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSel) RoyalGold.copy(alpha = 0.2f) else GraphiteDark,
            border = BorderStroke(1.dp, if (isSel) RoyalGold else SurfaceBorder),
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable { selectedCategory = catId }
          ) {
            Text(
              text = label,
              color = if (isSel) RoyalGoldLight else TextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }

      // 3. Horizontal Puzzle Selector Strip
      Text(
        text = "PUZZLE ARCHIVE (${filteredPuzzles.size})",
        color = RoyalGold,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 20.dp, top = 10.dp, bottom = 6.dp)
      )

      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filteredPuzzles) { p ->
          val isCurrent = activePuzzle?.id == p.id
          val isDaily = todayDaily?.id == p.id
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isCurrent) RoyalGold.copy(alpha = 0.25f) else GraphiteDark,
            border = BorderStroke(1.dp, if (isCurrent) RoyalGold else if (isDaily) RoyalGoldLight.copy(alpha = 0.5f) else SurfaceBorder),
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable { viewModel.loadPuzzle(p) }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (p.isCompleted) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = EmeraldGreen,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
              }
              Text(
                text = if (isDaily) "★ #${p.id} Today's" else "#${p.id} ${p.theme}",
                color = if (isCurrent) RoyalGoldLight else TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Active Puzzle Info Card
      activePuzzle?.let { p ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = SurfaceDark,
          border = BorderStroke(1.dp, SurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = p.title,
                color = RoyalGold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = SurfaceElevated
              ) {
                Text(
                  text = "Rating ${p.rating} • +${p.xpReward} XP",
                  color = RoyalGoldLight,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = p.description,
              color = TextSecondary,
              fontSize = 12.sp,
              lineHeight = 16.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3. Status Bar
      val statusBannerColor = when (uiState.status) {
        PuzzleStatus.SOLVING -> RoyalGold
        PuzzleStatus.SOLVED -> EmeraldGreen
        PuzzleStatus.WRONG_MOVE -> SoftRubyRed
      }
      val statusBannerText = when (uiState.status) {
        PuzzleStatus.SOLVING -> if (game.activeColor == PieceColor.WHITE) "White to move & win" else "Black to move & win"
        PuzzleStatus.SOLVED -> "⭐ SOLVED! You gained +${uiState.xpAwarded} XP ⭐"
        PuzzleStatus.WRONG_MOVE -> "Incorrect move. Try another tactic!"
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = statusBannerColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, statusBannerColor.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = statusBannerText,
            color = statusBannerColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4. Interactive Board
      ChessBoard3D(
        game = game,
        selectedSquare = uiState.selectedSquare,
        legalMoves = uiState.legalMoves,
        lastMove = game.moveHistory.lastOrNull(),
        is3DMode = true,
        cameraRotationDegrees = if (game.activeColor == PieceColor.BLACK && activePuzzle?.fen?.contains(" b ") == true) 180f else 0f,
        boardTheme = BoardThemeType.OBSIDIAN_GOLD,
        pieceTheme = PieceThemeType.ROYAL_GOLD,
        showCoordinates = true,
        onSquareTapped = { sq -> viewModel.onSquareTapped(sq) },
        modifier = Modifier.padding(horizontal = 8.dp)
      )

      // 5. Hint text display
      AnimatedVisibility(visible = uiState.hintText != null) {
        uiState.hintText?.let { hint ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, RoyalGold),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp)
          ) {
            Text(
              text = hint,
              color = RoyalGoldLight,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(10.dp),
              textAlign = TextAlign.Center
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 6. Action Controls
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        DarkOutlinedButton(
          text = "RETRY",
          icon = Icons.Default.Refresh,
          onClick = { viewModel.retryPuzzle() },
          modifier = Modifier.weight(1f),
          testTag = "puzzle_retry_button"
        )

        DarkOutlinedButton(
          text = "HINT",
          icon = Icons.Default.Lightbulb,
          onClick = { viewModel.showHint() },
          modifier = Modifier.weight(1f),
          testTag = "puzzle_hint_button"
        )

        if (uiState.status == PuzzleStatus.SOLVED) {
          GoldButton(
            text = "NEXT",
            icon = Icons.Default.NavigateNext,
            onClick = {
              val currentIdx = allPuzzles.indexOfFirst { it.id == activePuzzle?.id }
              if (currentIdx in allPuzzles.indices && currentIdx + 1 < allPuzzles.size) {
                viewModel.loadPuzzle(allPuzzles[currentIdx + 1])
              } else {
                viewModel.loadPuzzle(allPuzzles.first())
              }
            },
            modifier = Modifier.weight(1f),
            testTag = "puzzle_next_button"
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }

    // Pawn Promotion Dialog for Puzzle
    if (uiState.pendingPromotionMove != null) {
      PromotionDialog(
        color = uiState.pendingPromotionMove!!.piece.color,
        pieceTheme = PieceThemeType.ROYAL_GOLD,
        onPieceSelected = { pType -> viewModel.selectPromotion(pType) },
        onDismiss = { viewModel.dismissPromotion() }
      )
    }
  }
}
