package com.example.chessora.multiplayer

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.chessora.model.Move
import com.example.chessora.model.PieceColor
import com.example.chessora.model.PieceType
import com.example.chessora.model.Square
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import kotlin.random.Random

sealed interface MultiplayerEvent {
  data class OpponentConnected(val opponentName: String, val assignedColor: PieceColor) : MultiplayerEvent
  data class MoveReceived(val from: Square, val to: Square, val promotion: PieceType?) : MultiplayerEvent
  data object DrawOffered : MultiplayerEvent
  data object DrawAccepted : MultiplayerEvent
  data object DrawDeclined : MultiplayerEvent
  data object OpponentResigned : MultiplayerEvent
  data object RematchRequested : MultiplayerEvent
  data object RematchAccepted : MultiplayerEvent
  data class Disconnected(val reason: String) : MultiplayerEvent
  data class Error(val message: String) : MultiplayerEvent
}

enum class ConnectionStatus {
  IDLE,
  HOSTING,
  CONNECTING,
  CONNECTED,
  DISCONNECTED,
  ERROR
}

class LocalMultiplayerManager(private val context: Context) {

  private val scope = CoroutineScope(Dispatchers.IO)
  private var serverSocket: ServerSocket? = null
  private var activeSocket: Socket? = null
  private var socketReader: BufferedReader? = null
  private var socketWriter: PrintWriter? = null
  private var networkJob: Job? = null

  private val _connectionStatus = MutableStateFlow(ConnectionStatus.IDLE)
  val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

  private val _events = MutableSharedFlow<MultiplayerEvent>(extraBufferCapacity = 64)
  val events: SharedFlow<MultiplayerEvent> = _events.asSharedFlow()

  var isHost: Boolean = false
    private set
  var localPlayerColor: PieceColor = PieceColor.WHITE
    private set
  var hostIpAddress: String = ""
    private set
  var hostPort: Int = 8887
    private set
  var roomCode: String = ""
    private set

  fun getLocalIpAddress(): String {
    try {
      val interfaces = NetworkInterface.getNetworkInterfaces()
      while (interfaces.hasMoreElements()) {
        val nInterface = interfaces.nextElement()
        if (nInterface.isLoopback || !nInterface.isUp) continue
        val addresses = nInterface.inetAddresses
        while (addresses.hasMoreElements()) {
          val addr = addresses.nextElement()
          if (addr is Inet4Address && !addr.isLoopbackAddress) {
            val ip = addr.hostAddress ?: ""
            if (ip.isNotEmpty() && !ip.startsWith("127.")) {
              return ip
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.e("Multiplayer", "Error finding IP", e)
    }
    return "127.0.0.1"
  }

  fun createRoom(preferredColor: PieceColor = PieceColor.WHITE, playerName: String = "Host"): String {
    disconnect()
    isHost = true
    localPlayerColor = preferredColor
    hostIpAddress = getLocalIpAddress()
    hostPort = 8887
    val randomCodeNum = Random.nextInt(1000, 9999)
    roomCode = "CHESS-$randomCodeNum"

    val qrPayload = "CHESSORA|$hostIpAddress|$hostPort|${preferredColor.opposite().name}|$roomCode"

    _connectionStatus.value = ConnectionStatus.HOSTING

    networkJob = scope.launch {
      try {
        serverSocket = try {
          ServerSocket(8887)
        } catch (_: Exception) {
          val fallbackPort = 8887 + Random.nextInt(1, 100)
          hostPort = fallbackPort
          ServerSocket(fallbackPort)
        }
        Log.d("Multiplayer", "Server listening on $hostIpAddress:$hostPort")
        val socket = serverSocket?.accept() ?: return@launch
        activeSocket = socket
        setupSocketStreams(socket)

        // Handshake
        socketWriter?.println("WELCOME|$playerName|${preferredColor.opposite().name}|$roomCode")

        _connectionStatus.value = ConnectionStatus.CONNECTED
        _events.emit(MultiplayerEvent.OpponentConnected(
          opponentName = "Challenger",
          assignedColor = localPlayerColor
        ))

        listenForMessages()
      } catch (e: Exception) {
        if (_connectionStatus.value != ConnectionStatus.IDLE) {
          _connectionStatus.value = ConnectionStatus.ERROR
          _events.emit(MultiplayerEvent.Error("Server error: ${e.localizedMessage}"))
        }
      }
    }

    return qrPayload
  }

  fun joinRoom(connectionString: String, playerName: String = "Challenger") {
    disconnect()
    isHost = false
    _connectionStatus.value = ConnectionStatus.CONNECTING

    val parts = connectionString.split("|")
    val ip: String
    val port: Int
    var assignedColor = PieceColor.BLACK

    if (parts.size >= 4 && parts[0] == "CHESSORA") {
      ip = parts[1]
      port = parts[2].toIntOrNull() ?: 8887
      assignedColor = if (parts[3] == "WHITE") PieceColor.WHITE else PieceColor.BLACK
      roomCode = if (parts.size >= 5) parts[4] else "ROOM"
    } else if (connectionString.contains(":")) {
      val ipPort = connectionString.split(":")
      ip = ipPort[0].trim()
      port = ipPort[1].trim().toIntOrNull() ?: 8887
    } else {
      ip = connectionString.trim()
      port = 8887
    }

    localPlayerColor = assignedColor

    networkJob = scope.launch {
      try {
        val socket = withContext(Dispatchers.IO) {
          Socket(ip, port)
        }
        activeSocket = socket
        setupSocketStreams(socket)

        // Send Join handshake
        socketWriter?.println("JOIN|$playerName")

        _connectionStatus.value = ConnectionStatus.CONNECTED
        _events.emit(MultiplayerEvent.OpponentConnected(
          opponentName = "Host",
          assignedColor = localPlayerColor
        ))

        listenForMessages()
      } catch (e: Exception) {
        _connectionStatus.value = ConnectionStatus.ERROR
        _events.emit(MultiplayerEvent.Error("Connection failed: ${e.localizedMessage}. Ensure both devices are on the same Wi-Fi or Hotspot."))
      }
    }
  }

  private fun setupSocketStreams(socket: Socket) {
    socketReader = BufferedReader(InputStreamReader(socket.getInputStream()))
    socketWriter = PrintWriter(socket.getOutputStream(), true)
  }

  private suspend fun listenForMessages() {
    val reader = socketReader ?: return
    try {
      while (scope.isActive && _connectionStatus.value == ConnectionStatus.CONNECTED) {
        val line = withContext(Dispatchers.IO) {
          reader.readLine()
        } ?: break

        handleIncomingMessage(line)
      }
    } catch (e: Exception) {
      Log.d("Multiplayer", "Socket closed or read error: ${e.message}")
    } finally {
      if (_connectionStatus.value == ConnectionStatus.CONNECTED) {
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _events.emit(MultiplayerEvent.Disconnected("Opponent disconnected"))
      }
    }
  }

  private suspend fun handleIncomingMessage(msg: String) {
    val parts = msg.split("|")
    val type = parts.firstOrNull() ?: return

    when (type) {
      "WELCOME" -> {
        val hostName = parts.getOrNull(1) ?: "Host"
        val clientColorStr = parts.getOrNull(2) ?: "BLACK"
        localPlayerColor = if (clientColorStr == "WHITE") PieceColor.WHITE else PieceColor.BLACK
        _events.emit(MultiplayerEvent.OpponentConnected(hostName, localPlayerColor))
      }
      "MOVE" -> {
        // MOVE|e2|e4|QUEEN
        val fromAlg = parts.getOrNull(1) ?: return
        val toAlg = parts.getOrNull(2) ?: return
        val promoStr = parts.getOrNull(3)
        val from = Square.fromAlgebraic(fromAlg) ?: return
        val to = Square.fromAlgebraic(toAlg) ?: return
        val promo = promoStr?.let {
          when (it) {
            "QUEEN" -> PieceType.QUEEN
            "ROOK" -> PieceType.ROOK
            "BISHOP" -> PieceType.BISHOP
            "KNIGHT" -> PieceType.KNIGHT
            else -> null
          }
        }
        _events.emit(MultiplayerEvent.MoveReceived(from, to, promo))
      }
      "DRAW_OFFER" -> _events.emit(MultiplayerEvent.DrawOffered)
      "DRAW_ACCEPT" -> _events.emit(MultiplayerEvent.DrawAccepted)
      "DRAW_DECLINE" -> _events.emit(MultiplayerEvent.DrawDeclined)
      "RESIGN" -> _events.emit(MultiplayerEvent.OpponentResigned)
      "REMATCH_REQ" -> _events.emit(MultiplayerEvent.RematchRequested)
      "REMATCH_OK" -> _events.emit(MultiplayerEvent.RematchAccepted)
    }
  }

  fun sendMove(move: Move) {
    scope.launch {
      val promoStr = move.promotion?.name ?: "NONE"
      socketWriter?.println("MOVE|${move.from.algebraic}|${move.to.algebraic}|$promoStr")
    }
  }

  fun sendDrawOffer() {
    scope.launch { socketWriter?.println("DRAW_OFFER") }
  }

  fun acceptDraw() {
    scope.launch { socketWriter?.println("DRAW_ACCEPT") }
  }

  fun declineDraw() {
    scope.launch { socketWriter?.println("DRAW_DECLINE") }
  }

  fun resign() {
    scope.launch { socketWriter?.println("RESIGN") }
  }

  fun requestRematch() {
    scope.launch { socketWriter?.println("REMATCH_REQ") }
  }

  fun acceptRematch() {
    scope.launch { socketWriter?.println("REMATCH_OK") }
  }

  fun disconnect() {
    networkJob?.cancel()
    networkJob = null
    try {
      socketWriter?.close()
      socketReader?.close()
      activeSocket?.close()
      serverSocket?.close()
    } catch (_: Exception) {}
    activeSocket = null
    serverSocket = null
    socketWriter = null
    socketReader = null
    _connectionStatus.value = ConnectionStatus.IDLE
  }
}
