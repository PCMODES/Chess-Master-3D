package com.example.chessora.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PgnUtil {

  fun exportToPgn(
    game: ChessGame,
    whiteName: String = "White",
    blackName: String = "Black",
    event: String = "Chessora 3D Match",
    round: String = "1"
  ): String {
    val dateStr = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
    val resultStr = when (game.gameResult) {
      GameResult.CHECKMATE -> if (game.winner == PieceColor.WHITE) "1-0" else "0-1"
      GameResult.RESIGNATION -> if (game.winner == PieceColor.WHITE) "1-0" else "0-1"
      GameResult.TIMEOUT -> if (game.winner == PieceColor.WHITE) "1-0" else "0-1"
      GameResult.STALEMATE,
      GameResult.DRAW_FIFTY_MOVES,
      GameResult.DRAW_REPETITION,
      GameResult.DRAW_INSUFFICIENT_MATERIAL,
      GameResult.DRAW_AGREEMENT -> "1/2-1/2"
      GameResult.IN_PROGRESS -> "*"
    }

    val sb = StringBuilder()
    sb.appendLine("[Event \"$event\"]")
    sb.appendLine("[Site \"Chessora 3D Offline\"]")
    sb.appendLine("[Date \"$dateStr\"]")
    sb.appendLine("[Round \"$round\"]")
    sb.appendLine("[White \"$whiteName\"]")
    sb.appendLine("[Black \"$blackName\"]")
    sb.appendLine("[Result \"$resultStr\"]")
    sb.appendLine()

    var moveNum = 1
    for (i in game.moveHistory.indices) {
      if (i % 2 == 0) {
        sb.append("$moveNum. ")
        moveNum++
      }
      val san = game.moveHistory[i].san.ifEmpty {
        "${game.moveHistory[i].from.algebraic}${game.moveHistory[i].to.algebraic}"
      }
      sb.append(san).append(" ")
    }
    sb.append(resultStr)

    return sb.toString()
  }
}
