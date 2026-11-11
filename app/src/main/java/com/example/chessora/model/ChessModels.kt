package com.example.chessora.model

enum class PieceColor {
  WHITE,
  BLACK;

  fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}

enum class PieceType(val value: Int, val fenChar: Char, val displayName: String) {
  PAWN(100, 'p', "Pawn"),
  KNIGHT(320, 'n', "Knight"),
  BISHOP(330, 'b', "Bishop"),
  ROOK(500, 'r', "Rook"),
  QUEEN(900, 'q', "Queen"),
  KING(20000, 'k', "King")
}

data class Piece(
  val type: PieceType,
  val color: PieceColor,
  val hasMoved: Boolean = false
) {
  val fenChar: Char = if (color == PieceColor.WHITE) type.fenChar.uppercaseChar() else type.fenChar
}

data class Square(
  val col: Int, // 0 = 'a', 7 = 'h'
  val row: Int  // 0 = '1', 7 = '8' (White baseline = row 0, Black baseline = row 7)
) {
  val isValid: Boolean get() = col in 0..7 && row in 0..7

  val algebraic: String get() = "${('a' + col)}${row + 1}"

  val isLight: Boolean get() = (col + row) % 2 != 0

  companion object {
    fun fromAlgebraic(alg: String): Square? {
      if (alg.length != 2) return null
      val c = alg[0] - 'a'
      val r = alg[1] - '1'
      return if (c in 0..7 && r in 0..7) Square(c, r) else null
    }
  }
}

data class Move(
  val from: Square,
  val to: Square,
  val piece: Piece,
  val capturedPiece: Piece? = null,
  val promotion: PieceType? = null,
  val isCastling: Boolean = false,
  val isEnPassant: Boolean = false,
  val isDoublePawn: Boolean = false,
  val san: String = "" // Standard Algebraic Notation (e.g. e4, Nf3, O-O, exd5#)
)
