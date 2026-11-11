package com.example.chessora.model

data class CastlingRights(
  val whiteKingside: Boolean = true,
  val whiteQueenside: Boolean = true,
  val blackKingside: Boolean = true,
  val blackQueenside: Boolean = true
)

enum class GameResult {
  IN_PROGRESS,
  CHECKMATE,
  STALEMATE,
  DRAW_FIFTY_MOVES,
  DRAW_REPETITION,
  DRAW_INSUFFICIENT_MATERIAL,
  DRAW_AGREEMENT,
  RESIGNATION,
  TIMEOUT
}

class ChessGame(
  val board: Array<Array<Piece?>> = Array(8) { arrayOfNulls(8) },
  var activeColor: PieceColor = PieceColor.WHITE,
  var castlingRights: CastlingRights = CastlingRights(),
  var enPassantTarget: Square? = null,
  var halfmoveClock: Int = 0,
  var fullmoveNumber: Int = 1,
  val moveHistory: MutableList<Move> = mutableListOf(),
  val capturedWhitePieces: MutableList<Piece> = mutableListOf(),
  val capturedBlackPieces: MutableList<Piece> = mutableListOf(),
  val positionHistory: MutableList<String> = mutableListOf(),
  var gameResult: GameResult = GameResult.IN_PROGRESS,
  var winner: PieceColor? = null
) {

  init {
    if (board.all { row -> row.all { it == null } }) {
      setupStandardBoard()
    }
    if (positionHistory.isEmpty()) {
      positionHistory.add(toPositionHash())
    }
  }

  fun copy(): ChessGame {
    val newBoard = Array(8) { row ->
      Array(8) { col ->
        board[row][col]?.copy()
      }
    }
    val game = ChessGame(
      board = newBoard,
      activeColor = activeColor,
      castlingRights = castlingRights.copy(),
      enPassantTarget = enPassantTarget,
      halfmoveClock = halfmoveClock,
      fullmoveNumber = fullmoveNumber,
      moveHistory = moveHistory.toMutableList(),
      capturedWhitePieces = capturedWhitePieces.toMutableList(),
      capturedBlackPieces = capturedBlackPieces.toMutableList(),
      positionHistory = positionHistory.toMutableList(),
      gameResult = gameResult,
      winner = winner
    )
    return game
  }

  fun getPiece(col: Int, row: Int): Piece? {
    if (col !in 0..7 || row !in 0..7) return null
    return board[row][col]
  }

  fun getPiece(square: Square): Piece? = getPiece(square.col, square.row)

  fun setPiece(col: Int, row: Int, piece: Piece?) {
    if (col in 0..7 && row in 0..7) {
      board[row][col] = piece
    }
  }

  fun setupStandardBoard() {
    for (r in 0..7) {
      for (c in 0..7) {
        board[r][c] = null
      }
    }
    // White pieces (Row 0: back rank, Row 1: pawns)
    board[0][0] = Piece(PieceType.ROOK, PieceColor.WHITE)
    board[0][1] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
    board[0][2] = Piece(PieceType.BISHOP, PieceColor.WHITE)
    board[0][3] = Piece(PieceType.QUEEN, PieceColor.WHITE)
    board[0][4] = Piece(PieceType.KING, PieceColor.WHITE)
    board[0][5] = Piece(PieceType.BISHOP, PieceColor.WHITE)
    board[0][6] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
    board[0][7] = Piece(PieceType.ROOK, PieceColor.WHITE)
    for (c in 0..7) {
      board[1][c] = Piece(PieceType.PAWN, PieceColor.WHITE)
    }

    // Black pieces (Row 7: back rank, Row 6: pawns)
    board[7][0] = Piece(PieceType.ROOK, PieceColor.BLACK)
    board[7][1] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
    board[7][2] = Piece(PieceType.BISHOP, PieceColor.BLACK)
    board[7][3] = Piece(PieceType.QUEEN, PieceColor.BLACK)
    board[7][4] = Piece(PieceType.KING, PieceColor.BLACK)
    board[7][5] = Piece(PieceType.BISHOP, PieceColor.BLACK)
    board[7][6] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
    board[7][7] = Piece(PieceType.ROOK, PieceColor.BLACK)
    for (c in 0..7) {
      board[6][c] = Piece(PieceType.PAWN, PieceColor.BLACK)
    }

    activeColor = PieceColor.WHITE
    castlingRights = CastlingRights()
    enPassantTarget = null
    halfmoveClock = 0
    fullmoveNumber = 1
    gameResult = GameResult.IN_PROGRESS
    winner = null
  }

  fun findKing(color: PieceColor): Square? {
    for (r in 0..7) {
      for (c in 0..7) {
        val p = board[r][c]
        if (p != null && p.type == PieceType.KING && p.color == color) {
          return Square(c, r)
        }
      }
    }
    return null
  }

  fun isSquareAttacked(sq: Square, byColor: PieceColor): Boolean {
    // 1. Pawn attacks
    val pawnDirection = if (byColor == PieceColor.WHITE) 1 else -1
    val pawnRow = sq.row - pawnDirection
    if (pawnRow in 0..7) {
      for (dc in listOf(-1, 1)) {
        val pc = sq.col + dc
        if (pc in 0..7) {
          val p = board[pawnRow][pc]
          if (p != null && p.color == byColor && p.type == PieceType.PAWN) return true
        }
      }
    }

    // 2. Knight attacks
    val knightOffsets = listOf(
      Pair(1, 2), Pair(2, 1), Pair(-1, 2), Pair(-2, 1),
      Pair(1, -2), Pair(2, -1), Pair(-1, -2), Pair(-2, -1)
    )
    for ((dc, dr) in knightOffsets) {
      val c = sq.col + dc
      val r = sq.row + dr
      if (c in 0..7 && r in 0..7) {
        val p = board[r][c]
        if (p != null && p.color == byColor && p.type == PieceType.KNIGHT) return true
      }
    }

    // 3. King attacks (adjacent squares)
    for (dr in -1..1) {
      for (dc in -1..1) {
        if (dr == 0 && dc == 0) continue
        val c = sq.col + dc
        val r = sq.row + dr
        if (c in 0..7 && r in 0..7) {
          val p = board[r][c]
          if (p != null && p.color == byColor && p.type == PieceType.KING) return true
        }
      }
    }

    // 4. Straight rays (Rook or Queen)
    val straightDirs = listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))
    for ((dc, dr) in straightDirs) {
      var c = sq.col + dc
      var r = sq.row + dr
      while (c in 0..7 && r in 0..7) {
        val p = board[r][c]
        if (p != null) {
          if (p.color == byColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) return true
          break
        }
        c += dc
        r += dr
      }
    }

    // 5. Diagonal rays (Bishop or Queen)
    val diagDirs = listOf(Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1))
    for ((dc, dr) in diagDirs) {
      var c = sq.col + dc
      var r = sq.row + dr
      while (c in 0..7 && r in 0..7) {
        val p = board[r][c]
        if (p != null) {
          if (p.color == byColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) return true
          break
        }
        c += dc
        r += dr
      }
    }

    return false
  }

  fun isCheck(color: PieceColor): Boolean {
    val kingSquare = findKing(color) ?: return false
    return isSquareAttacked(kingSquare, color.opposite())
  }

  fun generateLegalMoves(color: PieceColor = activeColor): List<Move> {
    if (gameResult != GameResult.IN_PROGRESS) return emptyList()

    val pseudoMoves = mutableListOf<Move>()
    for (r in 0..7) {
      for (c in 0..7) {
        val p = board[r][c]
        if (p != null && p.color == color) {
          generatePseudoMovesForPiece(Square(c, r), p, pseudoMoves)
        }
      }
    }

    val legalMoves = mutableListOf<Move>()
    for (move in pseudoMoves) {
      if (isMoveLegal(move, color)) {
        legalMoves.add(move)
      }
    }
    return legalMoves
  }

  private fun generatePseudoMovesForPiece(sq: Square, piece: Piece, moves: MutableList<Move>) {
    when (piece.type) {
      PieceType.PAWN -> generatePawnMoves(sq, piece, moves)
      PieceType.KNIGHT -> generateKnightMoves(sq, piece, moves)
      PieceType.BISHOP -> generateRayMoves(sq, piece, listOf(Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)), moves)
      PieceType.ROOK -> generateRayMoves(sq, piece, listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0)), moves)
      PieceType.QUEEN -> generateRayMoves(sq, piece, listOf(
        Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0),
        Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)
      ), moves)
      PieceType.KING -> generateKingMoves(sq, piece, moves)
    }
  }

  private fun generatePawnMoves(sq: Square, piece: Piece, moves: MutableList<Move>) {
    val dir = if (piece.color == PieceColor.WHITE) 1 else -1
    val startRank = if (piece.color == PieceColor.WHITE) 1 else 6
    val promoRank = if (piece.color == PieceColor.WHITE) 7 else 0

    // Single step forward
    val nextRow = sq.row + dir
    if (nextRow in 0..7 && board[nextRow][sq.col] == null) {
      val target = Square(sq.col, nextRow)
      if (nextRow == promoRank) {
        for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
          moves.add(Move(from = sq, to = target, piece = piece, promotion = promo))
        }
      } else {
        moves.add(Move(from = sq, to = target, piece = piece))
        // Double step
        if (sq.row == startRank) {
          val doubleRow = sq.row + 2 * dir
          if (board[doubleRow][sq.col] == null) {
            moves.add(Move(from = sq, to = Square(sq.col, doubleRow), piece = piece, isDoublePawn = true))
          }
        }
      }
    }

    // Diagonal captures
    for (dc in listOf(-1, 1)) {
      val targetCol = sq.col + dc
      if (targetCol in 0..7 && nextRow in 0..7) {
        val targetSquare = Square(targetCol, nextRow)
        val targetPiece = board[nextRow][targetCol]
        if (targetPiece != null && targetPiece.color != piece.color) {
          if (nextRow == promoRank) {
            for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
              moves.add(Move(from = sq, to = targetSquare, piece = piece, capturedPiece = targetPiece, promotion = promo))
            }
          } else {
            moves.add(Move(from = sq, to = targetSquare, piece = piece, capturedPiece = targetPiece))
          }
        } else if (enPassantTarget != null && enPassantTarget == targetSquare) {
          // En passant capture
          val epCapturedRow = sq.row
          val epCapturedPiece = board[epCapturedRow][targetCol]
          moves.add(Move(from = sq, to = targetSquare, piece = piece, capturedPiece = epCapturedPiece, isEnPassant = true))
        }
      }
    }
  }

  private fun generateKnightMoves(sq: Square, piece: Piece, moves: MutableList<Move>) {
    val knightOffsets = listOf(
      Pair(1, 2), Pair(2, 1), Pair(-1, 2), Pair(-2, 1),
      Pair(1, -2), Pair(2, -1), Pair(-1, -2), Pair(-2, -1)
    )
    for ((dc, dr) in knightOffsets) {
      val c = sq.col + dc
      val r = sq.row + dr
      if (c in 0..7 && r in 0..7) {
        val targetPiece = board[r][c]
        if (targetPiece == null || targetPiece.color != piece.color) {
          moves.add(Move(from = sq, to = Square(c, r), piece = piece, capturedPiece = targetPiece))
        }
      }
    }
  }

  private fun generateRayMoves(sq: Square, piece: Piece, directions: List<Pair<Int, Int>>, moves: MutableList<Move>) {
    for ((dc, dr) in directions) {
      var c = sq.col + dc
      var r = sq.row + dr
      while (c in 0..7 && r in 0..7) {
        val targetPiece = board[r][c]
        if (targetPiece == null) {
          moves.add(Move(from = sq, to = Square(c, r), piece = piece))
        } else {
          if (targetPiece.color != piece.color) {
            moves.add(Move(from = sq, to = Square(c, r), piece = piece, capturedPiece = targetPiece))
          }
          break
        }
        c += dc
        r += dr
      }
    }
  }

  private fun generateKingMoves(sq: Square, piece: Piece, moves: MutableList<Move>) {
    for (dr in -1..1) {
      for (dc in -1..1) {
        if (dr == 0 && dc == 0) continue
        val c = sq.col + dc
        val r = sq.row + dr
        if (c in 0..7 && r in 0..7) {
          val targetPiece = board[r][c]
          if (targetPiece == null || targetPiece.color != piece.color) {
            moves.add(Move(from = sq, to = Square(c, r), piece = piece, capturedPiece = targetPiece))
          }
        }
      }
    }

    // Castling
    val row = if (piece.color == PieceColor.WHITE) 0 else 7
    if (sq == Square(4, row) && !isCheck(piece.color)) {
      // Kingside
      val canKingside = if (piece.color == PieceColor.WHITE) castlingRights.whiteKingside else castlingRights.blackKingside
      if (canKingside) {
        if (board[row][5] == null && board[row][6] == null &&
          !isSquareAttacked(Square(5, row), piece.color.opposite()) &&
          !isSquareAttacked(Square(6, row), piece.color.opposite())
        ) {
          val rook = board[row][7]
          if (rook != null && rook.type == PieceType.ROOK && rook.color == piece.color && !rook.hasMoved) {
            moves.add(Move(from = sq, to = Square(6, row), piece = piece, isCastling = true))
          }
        }
      }
      // Queenside
      val canQueenside = if (piece.color == PieceColor.WHITE) castlingRights.whiteQueenside else castlingRights.blackQueenside
      if (canQueenside) {
        if (board[row][1] == null && board[row][2] == null && board[row][3] == null &&
          !isSquareAttacked(Square(2, row), piece.color.opposite()) &&
          !isSquareAttacked(Square(3, row), piece.color.opposite())
        ) {
          val rook = board[row][0]
          if (rook != null && rook.type == PieceType.ROOK && rook.color == piece.color && !rook.hasMoved) {
            moves.add(Move(from = sq, to = Square(2, row), piece = piece, isCastling = true))
          }
        }
      }
    }
  }

  private fun isMoveLegal(move: Move, color: PieceColor): Boolean {
    // Make move on temp board
    val tempGame = this.copy()
    tempGame.applyMoveInternal(move)
    return !tempGame.isCheck(color)
  }

  private fun applyMoveInternal(move: Move) {
    val movingPiece = move.piece.copy(hasMoved = true)
    board[move.from.row][move.from.col] = null

    if (move.isCastling) {
      val row = move.from.row
      if (move.to.col == 6) { // Kingside
        board[row][6] = movingPiece
        val rook = board[row][7]?.copy(hasMoved = true)
        board[row][7] = null
        board[row][5] = rook
      } else if (move.to.col == 2) { // Queenside
        board[row][2] = movingPiece
        val rook = board[row][0]?.copy(hasMoved = true)
        board[row][0] = null
        board[row][3] = rook
      }
    } else if (move.isEnPassant) {
      board[move.to.row][move.to.col] = movingPiece
      val capturedRow = move.from.row
      board[capturedRow][move.to.col] = null
    } else if (move.promotion != null) {
      board[move.to.row][move.to.col] = Piece(type = move.promotion, color = movingPiece.color, hasMoved = true)
    } else {
      board[move.to.row][move.to.col] = movingPiece
    }
  }

  fun makeMove(move: Move): Boolean {
    val legalMoves = generateLegalMoves(activeColor)
    val matched = legalMoves.find {
      it.from == move.from && it.to == move.to && (it.promotion == move.promotion || (it.promotion == null && move.promotion == null))
    } ?: return false

    // Record captures
    if (matched.capturedPiece != null) {
      if (matched.capturedPiece.color == PieceColor.WHITE) {
        capturedWhitePieces.add(matched.capturedPiece)
      } else {
        capturedBlackPieces.add(matched.capturedPiece)
      }
    }

    // Apply move
    applyMoveInternal(matched)

    // Update castling rights
    var wk = castlingRights.whiteKingside
    var wq = castlingRights.whiteQueenside
    var bk = castlingRights.blackKingside
    var bq = castlingRights.blackQueenside

    if (matched.piece.type == PieceType.KING) {
      if (matched.piece.color == PieceColor.WHITE) { wk = false; wq = false }
      else { bk = false; bq = false }
    }
    if (matched.piece.type == PieceType.ROOK) {
      if (matched.from == Square(0, 0)) wq = false
      if (matched.from == Square(7, 0)) wk = false
      if (matched.from == Square(0, 7)) bq = false
      if (matched.from == Square(7, 7)) bk = false
    }
    // Also if rook is captured
    if (matched.to == Square(0, 0)) wq = false
    if (matched.to == Square(7, 0)) wk = false
    if (matched.to == Square(0, 7)) bq = false
    if (matched.to == Square(7, 7)) bk = false

    castlingRights = CastlingRights(wk, wq, bk, bq)

    // En passant target
    enPassantTarget = if (matched.isDoublePawn) {
      val midRow = (matched.from.row + matched.to.row) / 2
      Square(matched.from.col, midRow)
    } else {
      null
    }

    // Halfmove clock for 50-move rule
    if (matched.piece.type == PieceType.PAWN || matched.capturedPiece != null) {
      halfmoveClock = 0
    } else {
      halfmoveClock++
    }

    if (activeColor == PieceColor.BLACK) {
      fullmoveNumber++
    }

    // Generate SAN
    val sanMove = matched.copy(san = formatSan(matched))
    moveHistory.add(sanMove)

    // Switch active color
    activeColor = activeColor.opposite()

    // Add position hash for repetition
    val posHash = toPositionHash()
    positionHistory.add(posHash)

    // Check game termination conditions
    checkGameStatus()

    return true
  }

  private fun formatSan(m: Move): String {
    if (m.isCastling) {
      return if (m.to.col == 6) "O-O" else "O-O-O"
    }
    val piecePrefix = when (m.piece.type) {
      PieceType.PAWN -> ""
      PieceType.KNIGHT -> "N"
      PieceType.BISHOP -> "B"
      PieceType.ROOK -> "R"
      PieceType.QUEEN -> "Q"
      PieceType.KING -> "K"
    }
    val capture = if (m.capturedPiece != null) {
      if (m.piece.type == PieceType.PAWN) "${('a' + m.from.col)}x" else "x"
    } else ""
    val target = m.to.algebraic
    val promo = if (m.promotion != null) "=${m.promotion.displayName.first()}" else ""
    return "$piecePrefix$capture$target$promo"
  }

  fun checkGameStatus() {
    val nextMoves = generateLegalMoves(activeColor)
    val inCheck = isCheck(activeColor)

    if (nextMoves.isEmpty()) {
      if (inCheck) {
        gameResult = GameResult.CHECKMATE
        winner = activeColor.opposite()
      } else {
        gameResult = GameResult.STALEMATE
        winner = null
      }
      return
    }

    // Fifty-move rule
    if (halfmoveClock >= 100) {
      gameResult = GameResult.DRAW_FIFTY_MOVES
      winner = null
      return
    }

    // Threefold repetition
    val currentHash = toPositionHash()
    val count = positionHistory.count { it == currentHash }
    if (count >= 3) {
      gameResult = GameResult.DRAW_REPETITION
      winner = null
      return
    }

    // Insufficient material
    if (isInsufficientMaterial()) {
      gameResult = GameResult.DRAW_INSUFFICIENT_MATERIAL
      winner = null
      return
    }

    gameResult = GameResult.IN_PROGRESS
  }

  fun isInsufficientMaterial(): Boolean {
    val pieces = mutableListOf<Piece>()
    for (r in 0..7) {
      for (c in 0..7) {
        board[r][c]?.let { pieces.add(it) }
      }
    }
    if (pieces.size == 2) return true // K vs K
    if (pieces.size == 3) {
      // K + B vs K or K + N vs K
      val nonKings = pieces.filter { it.type != PieceType.KING }
      if (nonKings.size == 1 && (nonKings[0].type == PieceType.BISHOP || nonKings[0].type == PieceType.KNIGHT)) {
        return true
      }
    }
    return false
  }

  fun resign(color: PieceColor) {
    gameResult = GameResult.RESIGNATION
    winner = color.opposite()
  }

  fun agreeDraw() {
    gameResult = GameResult.DRAW_AGREEMENT
    winner = null
  }

  fun timeout(color: PieceColor) {
    gameResult = GameResult.TIMEOUT
    winner = color.opposite()
  }

  fun toPositionHash(): String {
    val sb = StringBuilder()
    for (r in 0..7) {
      for (c in 0..7) {
        val p = board[r][c]
        sb.append(p?.fenChar ?: '.')
      }
    }
    sb.append('_').append(activeColor.name)
    sb.append('_')
    if (castlingRights.whiteKingside) sb.append('K')
    if (castlingRights.whiteQueenside) sb.append('Q')
    if (castlingRights.blackKingside) sb.append('k')
    if (castlingRights.blackQueenside) sb.append('q')
    sb.append('_').append(enPassantTarget?.algebraic ?: "-")
    return sb.toString()
  }

  fun toFen(): String {
    val sb = StringBuilder()
    for (r in 7 downTo 0) {
      var emptyCount = 0
      for (c in 0..7) {
        val p = board[r][c]
        if (p == null) {
          emptyCount++
        } else {
          if (emptyCount > 0) {
            sb.append(emptyCount)
            emptyCount = 0
          }
          sb.append(p.fenChar)
        }
      }
      if (emptyCount > 0) sb.append(emptyCount)
      if (r > 0) sb.append('/')
    }

    sb.append(' ').append(if (activeColor == PieceColor.WHITE) 'w' else 'b')
    sb.append(' ')
    var castling = ""
    if (castlingRights.whiteKingside) castling += "K"
    if (castlingRights.whiteQueenside) castling += "Q"
    if (castlingRights.blackKingside) castling += "k"
    if (castlingRights.blackQueenside) castling += "q"
    sb.append(if (castling.isEmpty()) "-" else castling)
    sb.append(' ')
    sb.append(enPassantTarget?.algebraic ?: "-")
    sb.append(' ').append(halfmoveClock)
    sb.append(' ').append(fullmoveNumber)

    return sb.toString()
  }

  companion object {
    fun fromFen(fen: String): ChessGame {
      val game = ChessGame()
      val parts = fen.trim().split("\\s+".toRegex())
      if (parts.isEmpty()) return game

      // 1. Board
      for (r in 0..7) {
        for (c in 0..7) {
          game.board[r][c] = null
        }
      }

      val rows = parts[0].split('/')
      for (rIndex in rows.indices) {
        val boardRow = 7 - rIndex
        var boardCol = 0
        for (ch in rows[rIndex]) {
          if (ch.isDigit()) {
            boardCol += ch.digitToInt()
          } else {
            val color = if (ch.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
            val type = when (ch.lowercaseChar()) {
              'p' -> PieceType.PAWN
              'n' -> PieceType.KNIGHT
              'b' -> PieceType.BISHOP
              'r' -> PieceType.ROOK
              'q' -> PieceType.QUEEN
              'k' -> PieceType.KING
              else -> PieceType.PAWN
            }
            game.board[boardRow][boardCol] = Piece(type, color, hasMoved = true)
            boardCol++
          }
        }
      }

      // 2. Active color
      if (parts.size > 1) {
        game.activeColor = if (parts[1] == "w") PieceColor.WHITE else PieceColor.BLACK
      }

      // 3. Castling
      if (parts.size > 2) {
        val c = parts[2]
        game.castlingRights = CastlingRights(
          whiteKingside = c.contains('K'),
          whiteQueenside = c.contains('Q'),
          blackKingside = c.contains('k'),
          blackQueenside = c.contains('q')
        )
      }

      // 4. En passant
      if (parts.size > 3 && parts[3] != "-") {
        game.enPassantTarget = Square.fromAlgebraic(parts[3])
      }

      // 5. Halfmove
      if (parts.size > 4) {
        game.halfmoveClock = parts[4].toIntOrNull() ?: 0
      }

      // 6. Fullmove
      if (parts.size > 5) {
        game.fullmoveNumber = parts[5].toIntOrNull() ?: 1
      }

      game.checkGameStatus()
      return game
    }
  }
}
