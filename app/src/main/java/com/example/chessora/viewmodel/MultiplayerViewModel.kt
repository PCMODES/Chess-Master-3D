package com.example.chessora.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessora.model.ChessGame
import com.example.chessora.model.Move
import com.example.chessora.model.PieceColor
import com.example.chessora.model.PieceType
import com.example.chessora.model.Square
import com.example.chessora.multiplayer.ConnectionStatus
import com.example.chessora.multiplayer.LocalMultiplayerManager
import com.example.chessora.multiplayer.MultiplayerEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MultiplayerUiState(
  val connectionStatus: ConnectionStatus = ConnectionStatus.IDLE,
  val isHost: Boolean = false,
  val hostIp: String = "",
  val roomCode: String = "",
  val qrPayload: String = "",
  val playerColor: PieceColor = PieceColor.WHITE,
  val opponentName: String = "Opponent",
  val game: ChessGame = ChessGame(),
  val selectedSquare: Square? = null,
  val legalMoves: List<Move> = emptyList(),
  val pendingPromotionMove: Move? = null,
  val isOpponentTurn: Boolean = false,
  val infoMessage: String = "",
  val drawOfferedToMe: Boolean = false,
  val rematchRequested: Boolean = false
)

class MultiplayerViewModel(application: Application) : AndroidViewModel(application) {

  val manager = LocalMultiplayerManager(application)

  private val _uiState = MutableStateFlow(MultiplayerUiState())
  val uiState: StateFlow<MultiplayerUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      manager.connectionStatus.collect { status ->
        _uiState.value = _uiState.value.copy(
          connectionStatus = status,
          isHost = manager.isHost,
          hostIp = manager.hostIpAddress,
          roomCode = manager.roomCode,
          playerColor = manager.localPlayerColor
        )
      }
    }

    viewModelScope.launch {
      manager.events.collect { event ->
        handleMultiplayerEvent(event)
      }
    }
  }

  fun createRoom(preferredColor: PieceColor, playerName: String) {
    val qr = manager.createRoom(preferredColor, playerName)
    _uiState.value = _uiState.value.copy(
      qrPayload = qr,
      playerColor = preferredColor,
      game = ChessGame(),
      infoMessage = "Room created! Waiting for challenger to connect..."
    )
  }

  fun joinRoom(connectionString: String, playerName: String) {
    manager.joinRoom(connectionString, playerName)
    _uiState.value = _uiState.value.copy(
      game = ChessGame(),
      infoMessage = "Connecting to host..."
    )
  }

  private fun handleMultiplayerEvent(event: MultiplayerEvent) {
    val state = _uiState.value
    when (event) {
      is MultiplayerEvent.OpponentConnected -> {
        _uiState.value = state.copy(
          opponentName = event.opponentName,
          playerColor = event.assignedColor,
          infoMessage = "${event.opponentName} connected! Match started."
        )
      }
      is MultiplayerEvent.MoveReceived -> {
        val game = state.game.copy()
        val move = Move(
          from = event.from,
          to = event.to,
          piece = game.getPiece(event.from) ?: return,
          promotion = event.promotion
        )
        game.makeMove(move)
        _uiState.value = state.copy(
          game = game,
          selectedSquare = null,
          legalMoves = emptyList()
        )
      }
      is MultiplayerEvent.DrawOffered -> {
        _uiState.value = state.copy(drawOfferedToMe = true, infoMessage = "Opponent offered a draw.")
      }
      is MultiplayerEvent.DrawAccepted -> {
        val game = state.game.copy()
        game.agreeDraw()
        _uiState.value = state.copy(game = game, infoMessage = "Draw agreed.")
      }
      is MultiplayerEvent.DrawDeclined -> {
        _uiState.value = state.copy(infoMessage = "Opponent declined draw.")
      }
      is MultiplayerEvent.OpponentResigned -> {
        val game = state.game.copy()
        game.resign(state.playerColor.opposite())
        _uiState.value = state.copy(game = game, infoMessage = "Opponent resigned! You win!")
      }
      is MultiplayerEvent.RematchRequested -> {
        _uiState.value = state.copy(rematchRequested = true, infoMessage = "Opponent requested a rematch.")
      }
      is MultiplayerEvent.RematchAccepted -> {
        _uiState.value = state.copy(
          game = ChessGame(),
          infoMessage = "Rematch accepted! New game started."
        )
      }
      is MultiplayerEvent.Disconnected -> {
        _uiState.value = state.copy(infoMessage = "Opponent disconnected: ${event.reason}")
      }
      is MultiplayerEvent.Error -> {
        _uiState.value = state.copy(infoMessage = "Error: ${event.message}")
      }
    }
  }

  fun onSquareTapped(square: Square) {
    val state = _uiState.value
    val game = state.game
    // Only move if it is my turn
    if (game.activeColor != state.playerColor) return

    val selected = state.selectedSquare
    if (selected == null) {
      val piece = game.getPiece(square)
      if (piece != null && piece.color == state.playerColor) {
        val legal = game.generateLegalMoves(state.playerColor).filter { it.from == square }
        _uiState.value = state.copy(selectedSquare = square, legalMoves = legal)
      }
    } else {
      if (square == selected) {
        _uiState.value = state.copy(selectedSquare = null, legalMoves = emptyList())
        return
      }

      val pieceAtTarget = game.getPiece(square)
      if (pieceAtTarget != null && pieceAtTarget.color == state.playerColor) {
        val legal = game.generateLegalMoves(state.playerColor).filter { it.from == square }
        _uiState.value = state.copy(selectedSquare = square, legalMoves = legal)
        return
      }

      val matching = state.legalMoves.find { it.to == square }
      if (matching != null) {
        if (matching.piece.type == PieceType.PAWN && (square.row == 7 || square.row == 0)) {
          _uiState.value = state.copy(pendingPromotionMove = matching)
        } else {
          executeAndSendMove(matching)
        }
      } else {
        _uiState.value = state.copy(selectedSquare = null, legalMoves = emptyList())
      }
    }
  }

  fun selectPromotion(type: PieceType) {
    val pending = _uiState.value.pendingPromotionMove ?: return
    _uiState.value = _uiState.value.copy(pendingPromotionMove = null)
    executeAndSendMove(pending.copy(promotion = type))
  }

  fun dismissPromotion() {
    _uiState.value = _uiState.value.copy(pendingPromotionMove = null, selectedSquare = null, legalMoves = emptyList())
  }

  private fun executeAndSendMove(move: Move) {
    val state = _uiState.value
    val game = state.game.copy()
    game.makeMove(move)
    _uiState.value = state.copy(
      game = game,
      selectedSquare = null,
      legalMoves = emptyList()
    )
    manager.sendMove(move)
  }

  fun offerDraw() {
    manager.sendDrawOffer()
    _uiState.value = _uiState.value.copy(infoMessage = "Draw offered to opponent.")
  }

  fun acceptDraw() {
    manager.acceptDraw()
    val game = _uiState.value.game.copy()
    game.agreeDraw()
    _uiState.value = _uiState.value.copy(game = game, drawOfferedToMe = false)
  }

  fun declineDraw() {
    manager.declineDraw()
    _uiState.value = _uiState.value.copy(drawOfferedToMe = false)
  }

  fun resign() {
    manager.resign()
    val game = _uiState.value.game.copy()
    game.resign(_uiState.value.playerColor)
    _uiState.value = _uiState.value.copy(game = game)
  }

  fun requestRematch() {
    manager.requestRematch()
    _uiState.value = _uiState.value.copy(infoMessage = "Rematch requested.")
  }

  fun acceptRematch() {
    manager.acceptRematch()
    _uiState.value = _uiState.value.copy(
      game = ChessGame(),
      rematchRequested = false,
      infoMessage = "Rematch started!"
    )
  }

  fun disconnect() {
    manager.disconnect()
    _uiState.value = MultiplayerUiState()
  }

  override fun onCleared() {
    super.onCleared()
    manager.disconnect()
  }
}
