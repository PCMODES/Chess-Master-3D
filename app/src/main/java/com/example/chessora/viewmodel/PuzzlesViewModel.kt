package com.example.chessora.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessora.data.ChessDatabase
import com.example.chessora.data.ChessRepository
import com.example.chessora.data.PuzzleEntity
import com.example.chessora.model.ChessGame
import com.example.chessora.model.Move
import com.example.chessora.model.PieceType
import com.example.chessora.model.Square
import com.example.chessora.sound.ChessSoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class PuzzleStatus {
  SOLVING,
  WRONG_MOVE,
  SOLVED
}

data class PuzzleUiState(
  val activePuzzle: PuzzleEntity? = null,
  val game: ChessGame = ChessGame(),
  val selectedSquare: Square? = null,
  val legalMoves: List<Move> = emptyList(),
  val pendingPromotionMove: Move? = null,
  val status: PuzzleStatus = PuzzleStatus.SOLVING,
  val solutionStepIndex: Int = 0,
  val hintText: String? = null,
  val xpAwarded: Int = 0
)

class PuzzlesViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: ChessRepository
  val soundManager = ChessSoundManager(application)

  val allPuzzles: StateFlow<List<PuzzleEntity>>

  private val _uiState = MutableStateFlow(PuzzleUiState())
  val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

  val todayFormatted: String = run {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    sdf.format(cal.time)
  }

  val todayShortDate: String = run {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
    sdf.format(cal.time)
  }

  fun getDailyPuzzle(list: List<PuzzleEntity>): PuzzleEntity? {
    if (list.isEmpty()) return null
    val cal = Calendar.getInstance()
    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR) // 1 to 365
    val year = cal.get(Calendar.YEAR)
    // Deterministic daily rotation formula guaranteeing a fresh puzzle every single day of the year
    val seed = (year * 365) + dayOfYear
    val index = Math.floorMod(seed, list.size)
    return list[index]
  }

  init {
    val db = ChessDatabase.getInstance(application)
    repository = ChessRepository(db.chessDao())
    allPuzzles = repository.allPuzzles.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

    viewModelScope.launch {
      repository.initializeStarterDataIfEmpty()
      allPuzzles.collect { list ->
        if (_uiState.value.activePuzzle == null && list.isNotEmpty()) {
          // Automatically load today's daily puzzle on screen open
          val today = getDailyPuzzle(list) ?: list.first()
          loadPuzzle(today)
        }
      }
    }
  }

  fun loadDailyPuzzle() {
    val list = allPuzzles.value
    if (list.isNotEmpty()) {
      val today = getDailyPuzzle(list) ?: list.first()
      loadPuzzle(today)
      soundManager.playClickSound()
    }
  }

  fun loadPuzzle(puzzle: PuzzleEntity) {
    val puzzleGame = ChessGame.fromFen(puzzle.fen)
    _uiState.value = PuzzleUiState(
      activePuzzle = puzzle,
      game = puzzleGame,
      selectedSquare = null,
      legalMoves = emptyList(),
      status = PuzzleStatus.SOLVING,
      solutionStepIndex = 0,
      hintText = null
    )
  }

  fun onSquareTapped(square: Square) {
    val state = _uiState.value
    if (state.status == PuzzleStatus.SOLVED) return

    val game = state.game
    val selected = state.selectedSquare

    if (selected == null) {
      val piece = game.getPiece(square)
      if (piece != null && piece.color == game.activeColor) {
        val legal = game.generateLegalMoves(game.activeColor).filter { it.from == square }
        _uiState.value = state.copy(selectedSquare = square, legalMoves = legal)
        soundManager.playClickSound()
      }
    } else {
      if (square == selected) {
        _uiState.value = state.copy(selectedSquare = null, legalMoves = emptyList())
        return
      }

      val pieceAtTarget = game.getPiece(square)
      if (pieceAtTarget != null && pieceAtTarget.color == game.activeColor) {
        val legal = game.generateLegalMoves(game.activeColor).filter { it.from == square }
        _uiState.value = state.copy(selectedSquare = square, legalMoves = legal)
        soundManager.playClickSound()
        return
      }

      val matching = state.legalMoves.find { it.to == square }
      if (matching != null) {
        if (matching.piece.type == PieceType.PAWN && (square.row == 7 || square.row == 0)) {
          _uiState.value = state.copy(pendingPromotionMove = matching)
        } else {
          validatePuzzleMove(matching)
        }
      } else {
        _uiState.value = state.copy(selectedSquare = null, legalMoves = emptyList())
      }
    }
  }

  fun selectPromotion(type: PieceType) {
    val pending = _uiState.value.pendingPromotionMove ?: return
    _uiState.value = _uiState.value.copy(pendingPromotionMove = null)
    validatePuzzleMove(pending.copy(promotion = type))
  }

  fun dismissPromotion() {
    _uiState.value = _uiState.value.copy(pendingPromotionMove = null, selectedSquare = null, legalMoves = emptyList())
  }

  private fun validatePuzzleMove(move: Move) {
    val state = _uiState.value
    val puzzle = state.activePuzzle ?: return

    val updatedGame = state.game.copy()
    val success = updatedGame.makeMove(move)
    if (!success) return

    val playedSan = updatedGame.moveHistory.lastOrNull()?.san ?: ""
    val expectedMoves = puzzle.solutionMoves.split(",").map { it.trim() }
    val expectedMove = expectedMoves.getOrNull(state.solutionStepIndex) ?: ""

    // Normalize comparison (ignore + or # or x variations)
    val isCorrect = playedSan.replace("+", "").replace("#", "") == expectedMove.replace("+", "").replace("#", "") ||
        playedSan == expectedMove ||
        "${move.from.algebraic}${move.to.algebraic}" == expectedMove

    if (isCorrect) {
      soundManager.playMoveSound()
      val nextStep = state.solutionStepIndex + 1

      if (nextStep >= expectedMoves.size) {
        // Solved!
        soundManager.playVictorySound()
        _uiState.value = state.copy(
          game = updatedGame,
          selectedSquare = null,
          legalMoves = emptyList(),
          status = PuzzleStatus.SOLVED,
          solutionStepIndex = nextStep,
          xpAwarded = puzzle.xpReward
        )
        viewModelScope.launch {
          repository.recordPuzzleSolved(puzzle.id)
        }
      } else {
        // Opponent counter-move in multi-move puzzle
        _uiState.value = state.copy(
          game = updatedGame,
          selectedSquare = null,
          legalMoves = emptyList(),
          solutionStepIndex = nextStep
        )
      }
    } else {
      // Wrong move
      soundManager.playDefeatSound()
      _uiState.value = state.copy(
        game = updatedGame,
        selectedSquare = null,
        legalMoves = emptyList(),
        status = PuzzleStatus.WRONG_MOVE
      )
    }
  }

  fun retryPuzzle() {
    val puzzle = _uiState.value.activePuzzle ?: return
    loadPuzzle(puzzle)
  }

  fun showHint() {
    val puzzle = _uiState.value.activePuzzle ?: return
    val expectedMoves = puzzle.solutionMoves.split(",").map { it.trim() }
    val expected = expectedMoves.getOrNull(_uiState.value.solutionStepIndex) ?: return
    _uiState.value = _uiState.value.copy(hintText = "Next move starts with: ${expected.take(2)}")
    soundManager.playClickSound()
  }
}
