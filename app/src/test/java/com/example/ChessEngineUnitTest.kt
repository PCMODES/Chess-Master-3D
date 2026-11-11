package com.example

import com.example.chessora.ai.ChessAI
import com.example.chessora.ai.Difficulty
import com.example.chessora.model.ChessGame
import com.example.chessora.model.GameResult
import com.example.chessora.model.Move
import com.example.chessora.model.PieceColor
import com.example.chessora.model.PieceType
import com.example.chessora.model.Square
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessEngineUnitTest {

  @Test
  fun testInitialBoardSetup() {
    val game = ChessGame()
    assertEquals(PieceColor.WHITE, game.activeColor)
    // In initial position, White has 16 pawns/pieces moves: 8 pawns * 2 + 2 knights * 2 = 20 legal moves
    val legalMoves = game.generateLegalMoves()
    assertEquals(20, legalMoves.size)
  }

  @Test
  fun testScholarsMateCheckmate() {
    val game = ChessGame()

    // 1. e4 e5
    val e4 = game.generateLegalMoves().find { it.from == Square(4, 1) && it.to == Square(4, 3) }!!
    game.makeMove(e4)
    val e5 = game.generateLegalMoves().find { it.from == Square(4, 6) && it.to == Square(4, 4) }!!
    game.makeMove(e5)

    // 2. Bc4 Nc6
    val bc4 = game.generateLegalMoves().find { it.from == Square(5, 0) && it.to == Square(2, 3) }!!
    game.makeMove(bc4)
    val nc6 = game.generateLegalMoves().find { it.from == Square(1, 7) && it.to == Square(2, 5) }!!
    game.makeMove(nc6)

    // 3. Qh5 Nf6
    val qh5 = game.generateLegalMoves().find { it.from == Square(3, 0) && it.to == Square(7, 4) }!!
    game.makeMove(qh5)
    val nf6 = game.generateLegalMoves().find { it.from == Square(6, 7) && it.to == Square(5, 5) }!!
    game.makeMove(nf6)

    // 4. Qxf7#
    val qxf7 = game.generateLegalMoves().find { it.from == Square(7, 4) && it.to == Square(5, 6) }!!
    game.makeMove(qxf7)

    // Verify Checkmate
    assertEquals(GameResult.CHECKMATE, game.gameResult)
    assertEquals(PieceColor.WHITE, game.winner)
  }

  @Test
  fun testCastlingKingside() {
    // Setup position where White f1 & g1 are empty
    val game = ChessGame()
    // 1. e4 e5
    game.makeMove(game.generateLegalMoves().find { it.from == Square(4, 1) && it.to == Square(4, 3) }!!)
    game.makeMove(game.generateLegalMoves().find { it.from == Square(4, 6) && it.to == Square(4, 4) }!!)
    // 2. Nf3 Nc6
    game.makeMove(game.generateLegalMoves().find { it.from == Square(6, 0) && it.to == Square(5, 2) }!!)
    game.makeMove(game.generateLegalMoves().find { it.from == Square(1, 7) && it.to == Square(2, 5) }!!)
    // 3. Be2 d6
    game.makeMove(game.generateLegalMoves().find { it.from == Square(5, 0) && it.to == Square(4, 1) }!!)
    game.makeMove(game.generateLegalMoves().find { it.from == Square(3, 6) && it.to == Square(3, 5) }!!)

    // White should now have castling move (e1 to g1)
    val castlingMove = game.generateLegalMoves().find { it.isCastling && it.to == Square(6, 0) }
    assertNotNull(castlingMove)

    game.makeMove(castlingMove!!)
    // King is at g1 (6, 0) and Rook is at f1 (5, 0)
    val king = game.getPiece(6, 0)
    val rook = game.getPiece(5, 0)
    assertNotNull(king)
    assertEquals(PieceType.KING, king?.type)
    assertNotNull(rook)
    assertEquals(PieceType.ROOK, rook?.type)
  }

  @Test
  fun testFenRoundtrip() {
    val initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    val game = ChessGame.fromFen(initialFen)
    val exported = game.toFen()
    assertEquals(initialFen, exported)
  }

  @Test
  fun testAiFindsLegalMove() = runBlocking {
    val game = ChessGame()
    val bestMove = ChessAI.findBestMove(game, Difficulty.EASY)
    assertNotNull(bestMove)
    val legalMoves = game.generateLegalMoves()
    assertTrue(legalMoves.any { it.from == bestMove?.from && it.to == bestMove?.to })
  }
}
