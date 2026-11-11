package com.example.chessora.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessora.ai.ChessAI
import com.example.chessora.ai.Difficulty
import com.example.chessora.ai.HintResult
import com.example.chessora.data.CampaignLevelProgressEntity
import com.example.chessora.data.ChessDatabase
import com.example.chessora.data.ChessRepository
import com.example.chessora.data.MatchHistoryEntity
import com.example.chessora.data.UserProfileEntity
import com.example.chessora.model.CampaignLevelsProvider
import com.example.chessora.model.ChessGame
import com.example.chessora.model.GameResult
import com.example.chessora.model.Move
import com.example.chessora.model.PgnUtil
import com.example.chessora.model.Piece
import com.example.chessora.model.PieceColor
import com.example.chessora.model.PieceType
import com.example.chessora.model.Square
import com.example.chessora.sound.ChessSoundManager
import com.example.chessora.ui.board.BoardThemeType
import com.example.chessora.ui.board.PieceThemeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class GameMode(val displayName: String) {
  VS_AI("Play vs AI"),
  CAMPAIGN("Levels Campaign"),
  LOCAL_MULTIPLAYER("Local Multiplayer"),
  PASS_AND_PLAY("Pass & Play"),
  PUZZLE("Daily Puzzle")
}

data class ChessUiState(
  val game: ChessGame = ChessGame(),
  val gameMode: GameMode = GameMode.VS_AI,
  val selectedSquare: Square? = null,
  val legalMovesForSelected: List<Move> = emptyList(),
  val pendingPromotionMove: Move? = null,
  val playerColor: PieceColor = PieceColor.WHITE,
  val opponentName: String = "Master AI",
  val aiDifficulty: Difficulty = Difficulty.MEDIUM,
  val isAiThinking: Boolean = false,
  val is3DMode: Boolean = true,
  val cameraRotation: Float = 0f,
  val boardTheme: BoardThemeType = BoardThemeType.OBSIDIAN_GOLD,
  val pieceTheme: PieceThemeType = PieceThemeType.ROYAL_GOLD,
  val showCoordinates: Boolean = true,
  val whiteTimeRemainingMs: Long = 600_000L, // 10 min default
  val blackTimeRemainingMs: Long = 600_000L,
  val isClockEnabled: Boolean = true,
  val timeIncrementMs: Long = 0L,
  val isGameOverDialogVisible: Boolean = false,
  val xpEarnedInMatch: Int = 0,
  val leveledUpInMatch: Boolean = false,
  val currentHint: HintResult? = null,
  val autoRotatePassAndPlay: Boolean = false,
  val currentCampaignLevel: Int = 1,
  val campaignStarsEarned: Int = 0,
  val nextLevelUnlocked: Boolean = false
)

class ChessViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: ChessRepository
  val soundManager: ChessSoundManager = ChessSoundManager(application)

  private val _uiState = MutableStateFlow(ChessUiState())
  val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

  val userProfile: StateFlow<UserProfileEntity?>
  val recentMatches: StateFlow<List<MatchHistoryEntity>>
  val allCampaignProgress: StateFlow<List<CampaignLevelProgressEntity>>

  private var timerJob: Job? = null
  private var matchStartTime: Long = System.currentTimeMillis()

  init {
    val database = ChessDatabase.getInstance(application)
    repository = ChessRepository(database.chessDao())
    userProfile = repository.userProfile.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = null
    )
    recentMatches = repository.recentMatches.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )
    allCampaignProgress = repository.allCampaignProgress.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

    viewModelScope.launch {
      repository.initializeStarterDataIfEmpty()
      // Load user preferences
      repository.userProfile.collect { profile ->
        if (profile != null) {
          soundManager.isSoundEnabled = profile.soundEnabled
          soundManager.isMusicEnabled = profile.musicEnabled
          soundManager.masterVolume = profile.soundVolume
          soundManager.isHapticsEnabled = profile.hapticsEnabled
          if (profile.musicEnabled) {
            soundManager.startAmbientMusic()
          } else {
            soundManager.stopAmbientMusic()
          }
          _uiState.value = _uiState.value.copy(
            is3DMode = profile.is3DMode,
            boardTheme = BoardThemeType.fromId(profile.selectedBoardTheme),
            pieceTheme = PieceThemeType.fromId(profile.selectedPieceTheme),
            showCoordinates = profile.showCoordinates
          )
        }
      }
    }
  }

  fun startNewGame(
    mode: GameMode,
    color: PieceColor = PieceColor.WHITE,
    difficulty: Difficulty = Difficulty.MEDIUM,
    timeMinutes: Int = 10,
    incrementSec: Int = 0,
    opponentName: String = if (mode == GameMode.VS_AI) "AI ${difficulty.displayName}" else "Player 2",
    autoRotate: Boolean = false,
    campaignLevel: Int = 1
  ) {
    timerJob?.cancel()
    matchStartTime = System.currentTimeMillis()
    val newGame = ChessGame()
    val timeMs = if (timeMinutes > 0) timeMinutes * 60_000L else 0L

    val rot = if (color == PieceColor.BLACK && mode != GameMode.PASS_AND_PLAY) 180f else 0f

    _uiState.value = _uiState.value.copy(
      game = newGame,
      gameMode = mode,
      playerColor = color,
      opponentName = opponentName,
      aiDifficulty = difficulty,
      selectedSquare = null,
      legalMovesForSelected = emptyList(),
      pendingPromotionMove = null,
      isAiThinking = false,
      cameraRotation = rot,
      whiteTimeRemainingMs = timeMs,
      blackTimeRemainingMs = timeMs,
      isClockEnabled = timeMinutes > 0,
      timeIncrementMs = incrementSec * 1000L,
      isGameOverDialogVisible = false,
      xpEarnedInMatch = 0,
      leveledUpInMatch = false,
      currentHint = null,
      autoRotatePassAndPlay = autoRotate,
      currentCampaignLevel = campaignLevel,
      campaignStarsEarned = 0,
      nextLevelUnlocked = false
    )

    soundManager.playClickSound()

    if (timeMinutes > 0) {
      startClockTimer()
    }

    // If AI is White and player is Black, AI moves first
    if ((mode == GameMode.VS_AI || mode == GameMode.CAMPAIGN) && color == PieceColor.BLACK) {
      triggerAiMove()
    }
  }

  fun startCampaignLevel(levelNumber: Int) {
    val maxUnlocked = userProfile.value?.maxUnlockedLevel ?: 1
    val level = CampaignLevelsProvider.getLevel(levelNumber, maxUnlocked)
    startNewGame(
      mode = GameMode.CAMPAIGN,
      color = PieceColor.WHITE,
      difficulty = level.aiDifficulty,
      timeMinutes = level.timeMinutes,
      incrementSec = 0,
      opponentName = level.opponentName,
      campaignLevel = levelNumber
    )
  }

  fun playNextCampaignLevel() {
    val nextLevel = _uiState.value.currentCampaignLevel + 1
    startCampaignLevel(nextLevel)
  }

  fun onSquareTapped(square: Square) {
    val state = _uiState.value
    val game = state.game

    if (game.gameResult != GameResult.IN_PROGRESS || state.isAiThinking) return

    // In AI mode, prevent moving during AI turn
    if (state.gameMode == GameMode.VS_AI && game.activeColor != state.playerColor) return

    val selected = state.selectedSquare
    if (selected == null) {
      // Pick piece
      val piece = game.getPiece(square)
      if (piece != null && piece.color == game.activeColor) {
        val allLegal = game.generateLegalMoves(game.activeColor)
        val pieceLegal = allLegal.filter { it.from == square }
        _uiState.value = state.copy(
          selectedSquare = square,
          legalMovesForSelected = pieceLegal
        )
        soundManager.playClickSound()
      }
    } else {
      // Square already selected
      if (square == selected) {
        // Deselect
        _uiState.value = state.copy(selectedSquare = null, legalMovesForSelected = emptyList())
        return
      }

      val pieceAtTarget = game.getPiece(square)
      if (pieceAtTarget != null && pieceAtTarget.color == game.activeColor) {
        // Switch selection to another piece of same color
        val allLegal = game.generateLegalMoves(game.activeColor)
        val pieceLegal = allLegal.filter { it.from == square }
        _uiState.value = state.copy(
          selectedSquare = square,
          legalMovesForSelected = pieceLegal
        )
        soundManager.playClickSound()
        return
      }

      // Try to execute move
      val matchingMove = state.legalMovesForSelected.find { it.to == square }
      if (matchingMove != null) {
        // Check for promotion
        if (matchingMove.piece.type == PieceType.PAWN && (square.row == 7 || square.row == 0)) {
          val autoQueen = userProfile.value?.autoQueen ?: false
          if (autoQueen) {
            executeMove(matchingMove.copy(promotion = PieceType.QUEEN))
          } else {
            _uiState.value = state.copy(pendingPromotionMove = matchingMove)
          }
        } else {
          executeMove(matchingMove)
        }
      } else {
        // Illegal target - clear selection
        _uiState.value = state.copy(selectedSquare = null, legalMovesForSelected = emptyList())
      }
    }
  }

  fun selectPromotion(type: PieceType) {
    val pending = _uiState.value.pendingPromotionMove ?: return
    _uiState.value = _uiState.value.copy(pendingPromotionMove = null)
    executeMove(pending.copy(promotion = type))
  }

  fun dismissPromotion() {
    _uiState.value = _uiState.value.copy(pendingPromotionMove = null, selectedSquare = null, legalMovesForSelected = emptyList())
  }

  private fun executeMove(move: Move) {
    val state = _uiState.value
    val game = state.game.copy()
    val success = game.makeMove(move)
    if (!success) return

    // Sound effect
    if (move.capturedPiece != null || move.isEnPassant) {
      soundManager.playCaptureSound()
    } else {
      soundManager.playMoveSound()
    }

    if (game.isCheck(game.activeColor)) {
      soundManager.playCheckSound()
    }

    // Add time increment
    val newWhiteTime = if (game.activeColor == PieceColor.BLACK && state.isClockEnabled) {
      state.whiteTimeRemainingMs + state.timeIncrementMs
    } else state.whiteTimeRemainingMs

    val newBlackTime = if (game.activeColor == PieceColor.WHITE && state.isClockEnabled) {
      state.blackTimeRemainingMs + state.timeIncrementMs
    } else state.blackTimeRemainingMs

    // Pass & Play auto rotate board
    var rot = state.cameraRotation
    if (state.gameMode == GameMode.PASS_AND_PLAY && state.autoRotatePassAndPlay) {
      rot = if (game.activeColor == PieceColor.BLACK) 180f else 0f
    }

    _uiState.value = state.copy(
      game = game,
      selectedSquare = null,
      legalMovesForSelected = emptyList(),
      pendingPromotionMove = null,
      whiteTimeRemainingMs = newWhiteTime,
      blackTimeRemainingMs = newBlackTime,
      cameraRotation = rot,
      currentHint = null
    )

    // Check game termination
    if (game.gameResult != GameResult.IN_PROGRESS) {
      handleGameOver(game)
    } else if ((state.gameMode == GameMode.VS_AI || state.gameMode == GameMode.CAMPAIGN) && game.activeColor != state.playerColor) {
      triggerAiMove()
    }
  }

  private fun triggerAiMove() {
    val state = _uiState.value
    val game = state.game
    if (game.gameResult != GameResult.IN_PROGRESS) return

    _uiState.value = state.copy(isAiThinking = true)

    viewModelScope.launch {
      // Natural thinking delay between 500ms and 1100ms
      val thinkDelay = when (state.aiDifficulty) {
        Difficulty.BEGINNER -> 400L
        Difficulty.EASY -> 600L
        Difficulty.MEDIUM -> 800L
        else -> 1100L
      }
      delay(thinkDelay)

      val legalMoves = game.generateLegalMoves(game.activeColor)
      val bestMove = try {
        ChessAI.findBestMove(game, state.aiDifficulty)
      } catch (e: Exception) {
        null
      } ?: legalMoves.randomOrNull()

      _uiState.value = _uiState.value.copy(isAiThinking = false)

      if (bestMove != null && _uiState.value.game.gameResult == GameResult.IN_PROGRESS) {
        val updatedGame = _uiState.value.game.copy()
        updatedGame.makeMove(bestMove)

        if (bestMove.capturedPiece != null || bestMove.isEnPassant) {
          soundManager.playCaptureSound()
        } else {
          soundManager.playMoveSound()
        }

        if (updatedGame.isCheck(updatedGame.activeColor)) {
          soundManager.playCheckSound()
        }

        val curr = _uiState.value
        val newWhiteTime = if (updatedGame.activeColor == PieceColor.BLACK && curr.isClockEnabled) {
          curr.whiteTimeRemainingMs + curr.timeIncrementMs
        } else curr.whiteTimeRemainingMs
        val newBlackTime = if (updatedGame.activeColor == PieceColor.WHITE && curr.isClockEnabled) {
          curr.blackTimeRemainingMs + curr.timeIncrementMs
        } else curr.blackTimeRemainingMs

        _uiState.value = curr.copy(
          game = updatedGame,
          selectedSquare = null,
          legalMovesForSelected = emptyList(),
          whiteTimeRemainingMs = newWhiteTime,
          blackTimeRemainingMs = newBlackTime
        )

        if (updatedGame.gameResult != GameResult.IN_PROGRESS) {
          handleGameOver(updatedGame)
        }
      }
    }
  }

  private fun handleGameOver(game: ChessGame) {
    timerJob?.cancel()
    val state = _uiState.value
    val durationSeconds = ((System.currentTimeMillis() - matchStartTime) / 1000).toInt()
    val pgn = PgnUtil.exportToPgn(
      game = game,
      whiteName = if (state.playerColor == PieceColor.WHITE) "You" else state.opponentName,
      blackName = if (state.playerColor == PieceColor.BLACK) "You" else state.opponentName,
      event = "${state.gameMode.displayName} (${state.aiDifficulty.displayName})"
    )

    val isWin = game.winner == state.playerColor
    if (isWin) {
      soundManager.playVictorySound()
    } else if (game.winner != null) {
      soundManager.playDefeatSound()
    }

    viewModelScope.launch {
      if (state.gameMode == GameMode.CAMPAIGN && isWin) {
        val (campXp, campLvlUp, stars) = repository.recordCampaignLevelVictory(
          levelNumber = state.currentCampaignLevel,
          movesCount = game.moveHistory.size
        )
        repository.recordMatchFinished(
          opponentName = state.opponentName,
          gameMode = "CAMPAIGN",
          playerColor = state.playerColor,
          difficulty = state.aiDifficulty.displayName,
          result = game.gameResult,
          winner = game.winner,
          movesCount = game.moveHistory.size,
          durationSeconds = durationSeconds,
          pgn = pgn,
          fen = game.toFen()
        )
        _uiState.value = _uiState.value.copy(
          isGameOverDialogVisible = true,
          xpEarnedInMatch = campXp,
          leveledUpInMatch = campLvlUp,
          campaignStarsEarned = stars,
          nextLevelUnlocked = true
        )
      } else {
        val (xpGained, leveledUp) = repository.recordMatchFinished(
          opponentName = state.opponentName,
          gameMode = state.gameMode.name,
          playerColor = state.playerColor,
          difficulty = state.aiDifficulty.displayName,
          result = game.gameResult,
          winner = game.winner,
          movesCount = game.moveHistory.size,
          durationSeconds = durationSeconds,
          pgn = pgn,
          fen = game.toFen()
        )

        _uiState.value = _uiState.value.copy(
          isGameOverDialogVisible = true,
          xpEarnedInMatch = xpGained,
          leveledUpInMatch = leveledUp,
          campaignStarsEarned = 0,
          nextLevelUnlocked = false
        )
      }
    }
  }

  fun requestHint() {
    viewModelScope.launch {
      val hint = ChessAI.getHint(_uiState.value.game)
      if (hint != null) {
        soundManager.playClickSound()
        _uiState.value = _uiState.value.copy(
          currentHint = hint,
          selectedSquare = hint.move.from,
          legalMovesForSelected = listOf(hint.move)
        )
      }
    }
  }

  fun dismissHint() {
    _uiState.value = _uiState.value.copy(currentHint = null)
  }

  fun applyHintMove() {
    val hint = _uiState.value.currentHint ?: return
    _uiState.value = _uiState.value.copy(currentHint = null)
    executeMove(hint.move)
  }

  fun undoMove() {
    val state = _uiState.value
    val history = state.game.moveHistory
    if (history.isEmpty()) return

    val movesToRevert = if (state.gameMode == GameMode.VS_AI || state.gameMode == GameMode.CAMPAIGN) {
      if (history.size >= 2) 2 else 1
    } else 1

    val newHistory = history.dropLast(movesToRevert)
    val restoredGame = ChessGame()
    for (m in newHistory) {
      restoredGame.makeMove(m)
    }

    _uiState.value = state.copy(
      game = restoredGame,
      selectedSquare = null,
      legalMovesForSelected = emptyList(),
      currentHint = null
    )
    soundManager.playClickSound()
  }

  fun resign() {
    val state = _uiState.value
    val game = state.game.copy()
    game.resign(state.playerColor)
    _uiState.value = state.copy(game = game)
    handleGameOver(game)
  }

  fun agreeDraw() {
    val state = _uiState.value
    val game = state.game.copy()
    game.agreeDraw()
    _uiState.value = state.copy(game = game)
    handleGameOver(game)
  }

  fun toggle2D3D() {
    val current = _uiState.value.is3DMode
    _uiState.value = _uiState.value.copy(is3DMode = !current)
    viewModelScope.launch {
      repository.updateSettings(is3DMode = !current)
    }
  }

  fun rotateBoard() {
    val currentRot = _uiState.value.cameraRotation
    _uiState.value = _uiState.value.copy(cameraRotation = (currentRot + 180f) % 360f)
  }

  fun setBoardTheme(theme: BoardThemeType) {
    _uiState.value = _uiState.value.copy(boardTheme = theme)
    viewModelScope.launch { repository.updateSettings(boardTheme = theme.id) }
  }

  fun setPieceTheme(theme: PieceThemeType) {
    _uiState.value = _uiState.value.copy(pieceTheme = theme)
    viewModelScope.launch { repository.updateSettings(pieceTheme = theme.id) }
  }

  fun toggleSound(enabled: Boolean) {
    soundManager.isSoundEnabled = enabled
    viewModelScope.launch { repository.updateSettings(soundEnabled = enabled) }
  }

  fun toggleHaptics(enabled: Boolean) {
    soundManager.isHapticsEnabled = enabled
    viewModelScope.launch { repository.updateSettings(hapticsEnabled = enabled) }
  }

  fun toggleMusic(enabled: Boolean) {
    soundManager.isMusicEnabled = enabled
    if (enabled) {
      soundManager.startAmbientMusic()
    } else {
      soundManager.stopAmbientMusic()
    }
    viewModelScope.launch { repository.updateSettings(musicEnabled = enabled) }
  }

  fun setVolume(volume: Float) {
    soundManager.masterVolume = volume.coerceIn(0f, 1f)
    viewModelScope.launch { repository.updateSettings(soundVolume = volume) }
  }

  fun playButtonClick() {
    soundManager.playClickSound()
  }

  fun toggleCoordinates(enabled: Boolean) {
    _uiState.value = _uiState.value.copy(showCoordinates = enabled)
    viewModelScope.launch { repository.updateSettings(showCoordinates = enabled) }
  }

  fun dismissGameOverDialog() {
    _uiState.value = _uiState.value.copy(isGameOverDialogVisible = false)
  }

  private fun startClockTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (true) {
        delay(100L)
        val state = _uiState.value
        val game = state.game
        if (game.gameResult != GameResult.IN_PROGRESS || !state.isClockEnabled) break

        if (game.activeColor == PieceColor.WHITE) {
          val remaining = state.whiteTimeRemainingMs - 100L
          if (remaining <= 0) {
            val timedOutGame = game.copy()
            timedOutGame.timeout(PieceColor.WHITE)
            _uiState.value = state.copy(whiteTimeRemainingMs = 0, game = timedOutGame)
            handleGameOver(timedOutGame)
            break
          } else {
            _uiState.value = state.copy(whiteTimeRemainingMs = remaining)
          }
        } else {
          val remaining = state.blackTimeRemainingMs - 100L
          if (remaining <= 0) {
            val timedOutGame = game.copy()
            timedOutGame.timeout(PieceColor.BLACK)
            _uiState.value = state.copy(blackTimeRemainingMs = 0, game = timedOutGame)
            handleGameOver(timedOutGame)
            break
          } else {
            _uiState.value = state.copy(blackTimeRemainingMs = remaining)
          }
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}
