package com.example.chessora.ai

import com.example.chessora.model.ChessGame
import com.example.chessora.model.GameResult
import com.example.chessora.model.Move
import com.example.chessora.model.PieceColor
import com.example.chessora.model.PieceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.max
import kotlin.random.Random

enum class Difficulty(
  val displayName: String,
  val depth: Int,
  val estimatedElo: Int,
  val blunderChance: Double,
  val description: String
) {
  BEGINNER("Beginner", 1, 600, 0.35, "Casual play with frequent mistakes"),
  EASY("Easy", 2, 900, 0.15, "Basic tactical awareness"),
  MEDIUM("Medium", 2, 1300, 0.05, "Solid positional play and defense"),
  HARD("Hard", 3, 1600, 0.0, "Sharp calculation and center control"),
  EXPERT("Expert", 3, 1900, 0.0, "Deep tactics with quiescence capture search"),
  MASTER("Master", 3, 2200, 0.0, "Master level piece coordination"),
  GRANDMASTER("Grandmaster", 4, 2500, 0.0, "Tournament grade positional engine")
}

data class HintResult(
  val move: Move,
  val evaluation: Int,
  val pieceName: String,
  val fromAlgebraic: String,
  val toAlgebraic: String,
  val moveTitle: String,
  val explanation: String
)

object ChessAI {

  // Piece-Square Tables (8x8 from White's perspective; row 0 is White back rank)
  private val PAWN_PST = intArrayOf(
    0,  0,  0,  0,  0,  0,  0,  0,
    50, 50, 50, 50, 50, 50, 50, 50,
    10, 10, 20, 30, 30, 20, 10, 10,
     5,  5, 10, 25, 25, 10,  5,  5,
     0,  0,  0, 20, 20,  0,  0,  0,
     5, -5,-10,  0,  0,-10, -5,  5,
     5, 10, 10,-20,-20, 10, 10,  5,
     0,  0,  0,  0,  0,  0,  0,  0
  )

  private val KNIGHT_PST = intArrayOf(
    -50,-40,-30,-30,-30,-30,-40,-50,
    -40,-20,  0,  0,  0,  0,-20,-40,
    -30,  0, 10, 15, 15, 10,  0,-30,
    -30,  5, 15, 20, 20, 15,  5,-30,
    -30,  0, 15, 20, 20, 15,  0,-30,
    -30,  5, 10, 15, 15, 10,  5,-30,
    -40,-20,  0,  5,  5,  0,-20,-40,
    -50,-40,-30,-30,-30,-30,-40,-50
  )

  private val BISHOP_PST = intArrayOf(
    -20,-10,-10,-10,-10,-10,-10,-20,
    -10,  0,  0,  0,  0,  0,  0,-10,
    -10,  0,  5, 10, 10,  5,  0,-10,
    -10,  5,  5, 10, 10,  5,  5,-10,
    -10,  0, 10, 10, 10, 10,  0,-10,
    -10, 10, 10, 10, 10, 10, 10,-10,
    -10,  5,  0,  0,  0,  0,  5,-10,
    -20,-10,-10,-10,-10,-10,-10,-20
  )

  private val ROOK_PST = intArrayOf(
      0,  0,  0,  0,  0,  0,  0,  0,
      5, 10, 10, 10, 10, 10, 10,  5,
     -5,  0,  0,  0,  0,  0,  0, -5,
     -5,  0,  0,  0,  0,  0,  0, -5,
     -5,  0,  0,  0,  0,  0,  0, -5,
     -5,  0,  0,  0,  0,  0,  0, -5,
     -5,  0,  0,  0,  0,  0,  0, -5,
      0,  0,  0,  5,  5,  0,  0,  0
  )

  private val QUEEN_PST = intArrayOf(
    -20,-10,-10, -5, -5,-10,-10,-20,
    -10,  0,  0,  0,  0,  0,  0,-10,
    -10,  0,  5,  5,  5,  5,  0,-10,
     -5,  0,  5,  5,  5,  5,  0, -5,
      0,  0,  5,  5,  5,  5,  0, -5,
    -10,  5,  5,  5,  5,  5,  0,-10,
    -10,  0,  5,  0,  0,  0,  0,-10,
    -20,-10,-10, -5, -5,-10,-10,-20
  )

  private val KING_MIDDLE_PST = intArrayOf(
    -30,-40,-40,-50,-50,-40,-40,-30,
    -30,-40,-40,-50,-50,-40,-40,-30,
    -30,-40,-40,-50,-50,-40,-40,-30,
    -30,-40,-40,-50,-50,-40,-40,-30,
    -20,-30,-30,-40,-40,-30,-30,-20,
    -10,-20,-20,-20,-20,-20,-20,-10,
     20, 20,  0,  0,  0,  0, 20, 20,
     20, 30, 10,  0,  0, 10, 30, 20
  )

  suspend fun findBestMove(
    game: ChessGame,
    difficulty: Difficulty
  ): Move? = withContext(Dispatchers.Default) {
    val legalMoves = game.generateLegalMoves(game.activeColor)
    if (legalMoves.isEmpty()) return@withContext null

    // Check for random blunder on low difficulties
    if (Random.nextDouble() < difficulty.blunderChance && legalMoves.size > 1) {
      return@withContext legalMoves.random()
    }

    val orderedMoves = orderMoves(game, legalMoves)
    var bestMove = orderedMoves.first()

    // Strict 1.5s timeout so the AI NEVER hangs
    val computedBest = withTimeoutOrNull(1500L) {
      var bestScore = Int.MIN_VALUE + 1000
      var alpha = Int.MIN_VALUE + 1000
      val beta = Int.MAX_VALUE - 1000
      val aiColor = game.activeColor
      val searchDepth = difficulty.depth

      // Limit branching for higher depths to keep execution under 1.5s
      val candidateMoves = if (searchDepth >= 3 && orderedMoves.size > 12) {
        orderedMoves.take(12)
      } else {
        orderedMoves
      }

      for (move in candidateMoves) {
        val clone = game.copy()
        clone.makeMove(move)

        val score = -alphaBeta(
          game = clone,
          depth = searchDepth - 1,
          alpha = -beta,
          beta = -alpha,
          evaluatingFor = aiColor.opposite(),
          useQuiescence = difficulty >= Difficulty.EXPERT
        )

        if (score > bestScore) {
          bestScore = score
          bestMove = move
        }
        if (score > alpha) {
          alpha = score
        }
      }
      bestMove
    }

    computedBest ?: bestMove
  }

  suspend fun getHint(game: ChessGame): HintResult? = withContext(Dispatchers.Default) {
    val legalMoves = game.generateLegalMoves(game.activeColor)
    if (legalMoves.isEmpty()) return@withContext null

    val orderedMoves = orderMoves(game, legalMoves)
    var bestMove = orderedMoves.first()
    var bestScore = Int.MIN_VALUE + 1000
    val playerColor = game.activeColor

    withTimeoutOrNull(1000L) {
      for (move in orderedMoves.take(10)) {
        val clone = game.copy()
        clone.makeMove(move)
        val score = -alphaBeta(
          game = clone,
          depth = 2,
          alpha = Int.MIN_VALUE + 1000,
          beta = Int.MAX_VALUE - 1000,
          evaluatingFor = playerColor.opposite(),
          useQuiescence = false
        )
        if (score > bestScore) {
          bestScore = score
          bestMove = move
        }
      }
    }

    val chosenMove = bestMove
    val captured = chosenMove.capturedPiece
    val pieceNameEnglish = chosenMove.piece.type.displayName
    val pieceNameHindi = when (chosenMove.piece.type) {
      PieceType.PAWN -> "Pawn (Pyada)"
      PieceType.KNIGHT -> "Knight (Ghoda)"
      PieceType.BISHOP -> "Bishop (Oont)"
      PieceType.ROOK -> "Rook (Hathi)"
      PieceType.QUEEN -> "Queen (Wazir)"
      PieceType.KING -> "King (Raja)"
    }

    val fromAlg = chosenMove.from.algebraic
    val toAlg = chosenMove.to.algebraic
    val moveTitle = "$pieceNameEnglish $fromAlg ➔ $toAlg"

    val explanation = when {
      chosenMove.promotion != null -> "Promote pawn at $toAlg to Queen for decisive advantage."
      chosenMove.isCastling -> "Castle King to $toAlg for king safety and active rook."
      captured != null -> "Capture opponent's ${captured.type.displayName} on $toAlg to win material!"
      bestScore > 500 -> "Crushing tactical strike with $pieceNameEnglish to $toAlg."
      chosenMove.piece.type == PieceType.PAWN && toAlg in listOf("e4", "d4", "e5", "d5") -> "Advance pawn to $toAlg to control the vital center."
      chosenMove.piece.type == PieceType.KNIGHT && toAlg in listOf("f3", "c3", "f6", "c6") -> "Develop knight to $toAlg to dominate the center squares."
      bestScore > 0 -> "Strong positional move improving piece coordination."
      else -> "Defensive maneuver stabilizing your king and structure."
    }

    HintResult(
      move = chosenMove,
      evaluation = bestScore,
      pieceName = pieceNameHindi,
      fromAlgebraic = fromAlg,
      toAlgebraic = toAlg,
      moveTitle = moveTitle,
      explanation = explanation
    )
  }

  private fun alphaBeta(
    game: ChessGame,
    depth: Int,
    alpha: Int,
    beta: Int,
    evaluatingFor: PieceColor,
    useQuiescence: Boolean
  ): Int {
    if (game.gameResult == GameResult.CHECKMATE) {
      return -50000 - depth
    }
    if (game.gameResult != GameResult.IN_PROGRESS) {
      return 0 // Draw
    }

    if (depth <= 0) {
      return if (useQuiescence) {
        quiescence(game, alpha, beta, evaluatingFor, maxPly = 2)
      } else {
        evaluate(game, evaluatingFor)
      }
    }

    var currentAlpha = alpha
    val moves = orderMoves(game, game.generateLegalMoves(game.activeColor))
    if (moves.isEmpty()) {
      return if (game.isCheck(game.activeColor)) -50000 - depth else 0
    }

    // Limit moves to evaluate to avoid exponential explosion
    val movesToSearch = if (depth >= 2 && moves.size > 14) moves.take(14) else moves

    for (move in movesToSearch) {
      val clone = game.copy()
      clone.makeMove(move)
      val score = -alphaBeta(
        game = clone,
        depth = depth - 1,
        alpha = -beta,
        beta = -currentAlpha,
        evaluatingFor = evaluatingFor.opposite(),
        useQuiescence = useQuiescence
      )

      if (score >= beta) return beta // Beta cutoff
      if (score > currentAlpha) {
        currentAlpha = score
      }
    }
    return currentAlpha
  }

  private fun quiescence(
    game: ChessGame,
    alpha: Int,
    beta: Int,
    evaluatingFor: PieceColor,
    maxPly: Int
  ): Int {
    val standPat = evaluate(game, evaluatingFor)
    if (standPat >= beta) return beta
    var currentAlpha = max(alpha, standPat)

    if (maxPly <= 0) return currentAlpha

    val captures = game.generateLegalMoves(game.activeColor).filter { it.capturedPiece != null }
    val orderedCaptures = orderMoves(game, captures)

    for (move in orderedCaptures.take(6)) {
      val clone = game.copy()
      clone.makeMove(move)
      val score = -quiescence(clone, -beta, -currentAlpha, evaluatingFor.opposite(), maxPly - 1)
      if (score >= beta) return beta
      if (score > currentAlpha) {
        currentAlpha = score
      }
    }
    return currentAlpha
  }

  private fun orderMoves(game: ChessGame, moves: List<Move>): List<Move> {
    return moves.sortedByDescending { move ->
      var score = 0
      // MVV-LVA (Most Valuable Victim - Least Valuable Attacker)
      if (move.capturedPiece != null) {
        score += move.capturedPiece.type.value * 10 - move.piece.type.value
      }
      if (move.promotion != null) {
        score += move.promotion.value * 8
      }
      if (move.isCastling) {
        score += 80
      }
      score
    }
  }

  fun evaluate(game: ChessGame, perspective: PieceColor): Int {
    var whiteScore = 0
    var blackScore = 0

    for (r in 0..7) {
      for (c in 0..7) {
        val p = game.board[r][c] ?: continue
        val pieceVal = p.type.value
        val pstVal = getPieceSquareValue(p.type, p.color, c, r)

        if (p.color == PieceColor.WHITE) {
          whiteScore += pieceVal + pstVal
        } else {
          blackScore += pieceVal + pstVal
        }
      }
    }

    val totalScore = whiteScore - blackScore
    return if (perspective == PieceColor.WHITE) totalScore else -totalScore
  }

  private fun getPieceSquareValue(type: PieceType, color: PieceColor, col: Int, row: Int): Int {
    val effectiveRow = if (color == PieceColor.WHITE) row else 7 - row
    val index = (7 - effectiveRow) * 8 + col
    if (index !in 0..63) return 0

    return when (type) {
      PieceType.PAWN -> PAWN_PST[index]
      PieceType.KNIGHT -> KNIGHT_PST[index]
      PieceType.BISHOP -> BISHOP_PST[index]
      PieceType.ROOK -> ROOK_PST[index]
      PieceType.QUEEN -> QUEEN_PST[index]
      PieceType.KING -> KING_MIDDLE_PST[index]
    }
  }
}
