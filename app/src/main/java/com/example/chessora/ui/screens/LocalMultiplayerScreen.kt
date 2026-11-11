package com.example.chessora.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.chessora.sound.ChessSoundManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessora.model.PieceColor
import com.example.chessora.multiplayer.ConnectionStatus
import com.example.chessora.ui.components.DarkOutlinedButton
import com.example.chessora.ui.components.GoldButton
import com.example.chessora.ui.components.LuxuryCard
import com.example.chessora.ui.components.QrCodeCanvas
import com.example.chessora.ui.theme.ElectricBlue
import com.example.chessora.ui.theme.EmeraldGreen
import com.example.chessora.ui.theme.GraphiteDark
import com.example.chessora.ui.theme.ObsidianBlack
import com.example.chessora.ui.theme.RoyalGold
import com.example.chessora.ui.theme.RoyalGoldLight
import com.example.chessora.ui.theme.RoyalSilver
import com.example.chessora.ui.theme.SoftRubyRed
import com.example.chessora.ui.theme.SurfaceBorder
import com.example.chessora.ui.theme.SurfaceDark
import com.example.chessora.ui.theme.SurfaceElevated
import com.example.chessora.ui.theme.TextMuted
import com.example.chessora.ui.theme.TextPrimary
import com.example.chessora.ui.theme.TextSecondary
import com.example.chessora.viewmodel.MultiplayerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalMultiplayerScreen(
  uiState: MultiplayerUiState,
  onCreateRoom: (preferredColor: PieceColor, playerName: String) -> Unit,
  onJoinRoom: (connectionString: String, playerName: String) -> Unit,
  onStartGame: () -> Unit,
  onBackClicked: () -> Unit
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) } // 0 = Host, 1 = Join
  var joinInput by remember { mutableStateOf("") }
  var hostColor by remember { mutableStateOf(PieceColor.WHITE) }
  var showQrScannerDialog by remember { mutableStateOf(false) }

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "OFFLINE MULTIPLAYER",
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .testTag("multiplayer_screen")
    ) {
      // Tab Row
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = GraphiteDark,
        contentColor = RoyalGold,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = RoyalGold
          )
        }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Text(
              text = "CREATE ROOM (HOST)",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (selectedTab == 0) RoyalGold else TextSecondary
            )
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Text(
              text = "JOIN ROOM (CLIENT)",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (selectedTab == 1) RoyalGold else TextSecondary
            )
          }
        )
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Status Card
        item {
          ConnectionStatusCard(uiState.connectionStatus, uiState.infoMessage)
        }

        if (uiState.connectionStatus == ConnectionStatus.CONNECTED) {
          // Connected state! Show launch button
          item {
            LuxuryCard(
              modifier = Modifier.fillMaxWidth(),
              borderColor = EmeraldGreen
            ) {
              Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = EmeraldGreen,
                  modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "OPPONENT CONNECTED!",
                  color = EmeraldGreen,
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                )
                Text(
                  text = "Playing as ${uiState.playerColor.name}",
                  color = TextSecondary,
                  fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                GoldButton(
                  text = "ENTER GAME BOARD",
                  icon = Icons.Default.PlayArrow,
                  onClick = onStartGame,
                  modifier = Modifier.fillMaxWidth(),
                  testTag = "enter_multiplayer_game_button"
                )
              }
            }
          }
        } else if (selectedTab == 0) {
          // HOST TAB
          item {
            if (uiState.connectionStatus == ConnectionStatus.HOSTING) {
              // Display QR and connection info
              LuxuryCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = RoyalGold
              ) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "ROOM CODE",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = uiState.roomCode,
                    color = RoyalGold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                  )

                  Spacer(modifier = Modifier.height(12.dp))

                  // QR Code
                  Box(
                    modifier = Modifier
                      .size(200.dp)
                      .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    QrCodeCanvas(
                      payload = uiState.qrPayload,
                      modifier = Modifier.fillMaxSize()
                    )
                  }

                  Spacer(modifier = Modifier.height(14.dp))

                  Text(
                    text = "Host IP: ${uiState.hostIp}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                  )

                  Spacer(modifier = Modifier.height(8.dp))

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(16.dp),
                      color = RoyalGold,
                      strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "Waiting for other phone to connect...",
                      color = RoyalGoldLight,
                      fontSize = 12.sp
                    )
                  }
                }
              }
            } else {
              // Host Setup Form
              LuxuryCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                  Text(
                    text = "HOST A LOCAL MATCH",
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                  Text(
                    text = "Create a direct offline session for nearby players",
                    color = TextSecondary,
                    fontSize = 12.sp
                  )

                  Spacer(modifier = Modifier.height(16.dp))

                  Text(
                    text = "YOUR COLOR",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )

                  Spacer(modifier = Modifier.height(6.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    ColorOptionButton(
                      title = "White",
                      isSelected = hostColor == PieceColor.WHITE,
                      onClick = { hostColor = PieceColor.WHITE },
                      modifier = Modifier.weight(1f)
                    )
                    ColorOptionButton(
                      title = "Black",
                      isSelected = hostColor == PieceColor.BLACK,
                      onClick = { hostColor = PieceColor.BLACK },
                      modifier = Modifier.weight(1f)
                    )
                  }

                  Spacer(modifier = Modifier.height(20.dp))

                  GoldButton(
                    text = "CREATE & SHOW QR",
                    icon = Icons.Default.QrCode,
                    onClick = { onCreateRoom(hostColor, "Host Player") },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "create_room_button"
                  )
                }
              }
            }
          }
        } else {
          // JOIN TAB
          item {
            LuxuryCard(modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                  text = "JOIN HOST DEVICE",
                  color = RoyalGold,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
                Text(
                  text = "Scan the host QR code or enter their connection details",
                  color = TextSecondary,
                  fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action: Open Live Camera QR Scanner
                GoldButton(
                  text = "SCAN HOST QR CODE (CAMERA)",
                  icon = Icons.Default.CameraAlt,
                  onClick = {
                    if (!hasCameraPermission) {
                      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    showQrScannerDialog = true
                  },
                  modifier = Modifier.fillMaxWidth(),
                  testTag = "scan_qr_button"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hotspot One-Tap Direct Connect
                DarkOutlinedButton(
                  text = "QUICK CONNECT HOTSPOT (192.168.43.1)",
                  icon = Icons.Default.FlashOn,
                  onClick = {
                    joinInput = "192.168.43.1"
                    onJoinRoom("192.168.43.1", "Challenger")
                  },
                  modifier = Modifier.fillMaxWidth(),
                  testTag = "quick_hotspot_connect_button"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    modifier = Modifier.weight(1f).height(1.dp),
                    color = SurfaceBorder
                  ) {}
                  Text(
                    text = "  OR ENTER MANUALLY  ",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                  )
                  Surface(
                    modifier = Modifier.weight(1f).height(1.dp),
                    color = SurfaceBorder
                  ) {}
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                  value = joinInput,
                  onValueChange = { joinInput = it },
                  label = { Text("Host IP or Room Code") },
                  placeholder = { Text("192.168.43.1") },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_ip_input"),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalGold,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                  ),
                  shape = RoundedCornerShape(12.dp),
                  singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                GoldButton(
                  text = "CONNECT TO HOST",
                  icon = Icons.Default.Wifi,
                  enabled = joinInput.isNotBlank(),
                  onClick = { onJoinRoom(joinInput, "Challenger") },
                  modifier = Modifier.fillMaxWidth(),
                  testTag = "connect_to_host_button"
                )
              }
            }
          }
        }

        // Offline Network Instructions
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.Top
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "HOW 100% OFFLINE MULTIPLAYER WORKS",
                  color = ElectricBlue,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "1. Connect both devices to the same Wi-Fi router, or turn on Portable Hotspot on one phone and connect the other.\n2. No internet access or mobile data is required.\n3. Moves stream instantly peer-to-peer between devices.",
                  color = TextSecondary,
                  fontSize = 12.sp,
                  lineHeight = 17.sp
                )
              }
            }
          }
        }
      }
    }

    if (showQrScannerDialog) {
      QrScannerDialog(
        hasCameraPermission = hasCameraPermission,
        onRequestPermission = {
          cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onDismiss = { showQrScannerDialog = false },
        onCodeScanned = { scannedTarget ->
          joinInput = if (scannedTarget.startsWith("CHESSORA|")) {
            val parts = scannedTarget.split("|")
            if (parts.size >= 2) parts[1] else scannedTarget
          } else scannedTarget
          showQrScannerDialog = false
          onJoinRoom(scannedTarget, "Challenger")
        }
      )
    }
  }
}

@Composable
fun ConnectionStatusCard(status: ConnectionStatus, message: String) {
  val color: Color
  val text: String
  when (status) {
    ConnectionStatus.IDLE -> {
      color = TextMuted
      text = "Ready to Connect"
    }
    ConnectionStatus.HOSTING -> {
      color = RoyalGold
      text = "Hosting Room • Ready for Challenger"
    }
    ConnectionStatus.CONNECTING -> {
      color = ElectricBlue
      text = "Connecting to Host..."
    }
    ConnectionStatus.CONNECTED -> {
      color = EmeraldGreen
      text = "Connected Peer-to-Peer"
    }
    ConnectionStatus.DISCONNECTED -> {
      color = SoftRubyRed
      text = "Disconnected"
    }
    ConnectionStatus.ERROR -> {
      color = SoftRubyRed
      text = "Connection Failed"
    }
  }

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = color.copy(alpha = 0.12f),
    border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(shape = CircleShape, color = color, modifier = Modifier.size(10.dp)) {}
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = text,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    }
  }
}

@Composable
fun ColorOptionButton(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (isSelected) RoyalGold.copy(alpha = 0.2f) else SurfaceElevated,
    border = BorderStroke(1.dp, if (isSelected) RoyalGold else SurfaceBorder),
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() }
  ) {
    Box(
      modifier = Modifier.padding(vertical = 10.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = title,
        color = if (isSelected) RoyalGoldLight else TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    }
  }
}

@Composable
fun QrScannerDialog(
  hasCameraPermission: Boolean,
  onRequestPermission: () -> Unit,
  onDismiss: () -> Unit,
  onCodeScanned: (String) -> Unit
) {
  val context = LocalContext.current
  val soundManager = remember { ChessSoundManager(context) }
  val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
  val laserProgress by infiniteTransition.animateFloat(
    initialValue = 0.05f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser_progress"
  )

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = ObsidianBlack,
      border = BorderStroke(1.5.dp, RoyalGold),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("qr_scanner_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = null,
              tint = RoyalGold,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "LIVE QR SCANNER",
              color = RoyalGold,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              letterSpacing = 1.sp
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (hasCameraPermission)
            "Point camera at the Host QR Code on the second device"
          else
            "Camera permission is required to read the Host QR Code",
          color = if (hasCameraPermission) TextSecondary else RoyalGoldLight,
          fontSize = 12.sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Live Camera Viewfinder Box
        Box(
          modifier = Modifier
            .size(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GraphiteDark),
          contentAlignment = Alignment.Center
        ) {
          if (hasCameraPermission) {
            // Live Camera Preview
            CameraQrScannerView(
              onCodeScanned = { rawResult ->
                soundManager.playScanSuccessSound()
                onCodeScanned(rawResult)
              },
              modifier = Modifier.fillMaxSize()
            )

            // Reticle overlay and animated laser
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height
              val cornerLen = 28.dp.toPx()
              val strokeW = 3.5.dp.toPx()
              val cornerColor = RoyalGold

              // Top-Left corner
              drawLine(cornerColor, Offset(0f, 0f), Offset(cornerLen, 0f), strokeW)
              drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cornerLen), strokeW)

              // Top-Right corner
              drawLine(cornerColor, Offset(w, 0f), Offset(w - cornerLen, 0f), strokeW)
              drawLine(cornerColor, Offset(w, 0f), Offset(w, cornerLen), strokeW)

              // Bottom-Left corner
              drawLine(cornerColor, Offset(0f, h), Offset(cornerLen, h), strokeW)
              drawLine(cornerColor, Offset(0f, h), Offset(0f, h - cornerLen), strokeW)

              // Bottom-Right corner
              drawLine(cornerColor, Offset(w, h), Offset(w - cornerLen, h), strokeW)
              drawLine(cornerColor, Offset(w, h), Offset(w, h - cornerLen), strokeW)

              // Animated Laser Scan Bar
              val laserY = h * laserProgress
              drawLine(
                color = ElectricBlue,
                start = Offset(14.dp.toPx(), laserY),
                end = Offset(w - 14.dp.toPx(), laserY),
                strokeWidth = 3.dp.toPx()
              )
            }
          } else {
            // Permission Needed View
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = RoyalGold,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Camera Permission Required",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(12.dp))
              GoldButton(
                text = "ALLOW CAMERA",
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(0.9f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Connect Action fallback
        GoldButton(
          text = "AUTO-CONNECT HOTSPOT (192.168.43.1)",
          icon = Icons.Default.FlashOn,
          onClick = {
            soundManager.playClickSound()
            onCodeScanned("192.168.43.1")
            onDismiss()
          },
          modifier = Modifier.fillMaxWidth(),
          testTag = "scanner_auto_detect_button"
        )

        Spacer(modifier = Modifier.height(8.dp))

        DarkOutlinedButton(
          text = "CLOSE SCANNER",
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          testTag = "scanner_close_button"
        )
      }
    }
  }
}

@Composable
fun CameraQrScannerView(
  onCodeScanned: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val hasScanned = remember { mutableStateOf(false) }

  AndroidView(
    factory = { ctx ->
      val previewView = PreviewView(ctx).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
      }

      val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
      cameraProviderFuture.addListener({
        try {
          val cameraProvider = cameraProviderFuture.get()
          val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
          }

          val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

          val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to java.lang.Boolean.TRUE,
            DecodeHintType.CHARACTER_SET to "UTF-8"
          )
          val reader = MultiFormatReader().apply { setHints(hints) }

          imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
            if (hasScanned.value) {
              imageProxy.close()
              return@setAnalyzer
            }

            try {
              val rawText = decodeQrFromImageProxy(reader, imageProxy)
              if (!rawText.isNullOrBlank()) {
                val validTarget = extractHostIp(rawText)
                if (validTarget.isNotBlank()) {
                  hasScanned.value = true
                  onCodeScanned(validTarget)
                }
              }
            } catch (_: Exception) {
              // Frame without readable QR code, continue scanning
            } finally {
              reader.reset()
              imageProxy.close()
            }
          }

          val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
          cameraProvider.unbindAll()
          cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
          )
        } catch (_: Exception) {
          // Camera hardware not available or exception in preview
        }
      }, ContextCompat.getMainExecutor(ctx))

      previewView
    },
    modifier = modifier
  )
}

/**
 * Extracts raw luminance data from ImageProxy handling rowStride padding,
 * adjusts for physical camera sensor rotation, and decodes QR codes using ZXing.
 */
private fun decodeQrFromImageProxy(reader: MultiFormatReader, imageProxy: ImageProxy): String? {
  val planes = imageProxy.planes
  if (planes.isEmpty()) return null

  val yPlane = planes[0]
  val yBuffer = yPlane.buffer
  val width = imageProxy.width
  val height = imageProxy.height
  val rowStride = yPlane.rowStride
  val pixelStride = yPlane.pixelStride

  // Extract raw luminance bytes, properly handling rowStride padding
  val yData = ByteArray(width * height)
  yBuffer.rewind()

  if (rowStride == width && pixelStride == 1) {
    val bytesToRead = minOf(width * height, yBuffer.remaining())
    yBuffer.get(yData, 0, bytesToRead)
  } else {
    var outOffset = 0
    val rowBuffer = ByteArray(rowStride)
    for (row in 0 until height) {
      yBuffer.position(row * rowStride)
      if (pixelStride == 1) {
        val bytesToRead = minOf(width, yBuffer.remaining())
        yBuffer.get(yData, outOffset, bytesToRead)
        outOffset += width
      } else {
        val bytesToRead = minOf(rowStride, yBuffer.remaining())
        yBuffer.get(rowBuffer, 0, bytesToRead)
        for (col in 0 until width) {
          yData[outOffset++] = rowBuffer[col * pixelStride]
        }
      }
    }
  }

  // Handle camera sensor rotation relative to UI orientation
  val rotationDegrees = imageProxy.imageInfo.rotationDegrees
  val (finalData, finalWidth, finalHeight) = when (rotationDegrees) {
    90 -> {
      val rotated = ByteArray(width * height)
      for (y in 0 until height) {
        for (x in 0 until width) {
          rotated[x * height + (height - 1 - y)] = yData[y * width + x]
        }
      }
      Triple(rotated, height, width)
    }
    180 -> {
      val rotated = ByteArray(width * height)
      for (i in 0 until width * height) {
        rotated[width * height - 1 - i] = yData[i]
      }
      Triple(rotated, width, height)
    }
    270 -> {
      val rotated = ByteArray(width * height)
      for (y in 0 until height) {
        for (x in 0 until width) {
          rotated[(width - 1 - x) * height + y] = yData[y * width + x]
        }
      }
      Triple(rotated, height, width)
    }
    else -> Triple(yData, width, height)
  }

  val source = PlanarYUVLuminanceSource(
    finalData,
    finalWidth,
    finalHeight,
    0,
    0,
    finalWidth,
    finalHeight,
    false
  )

  // 1. Try standard binarization
  var text: String? = try {
    val bitmap = BinaryBitmap(HybridBinarizer(source))
    reader.decodeWithState(bitmap)?.text
  } catch (_: Exception) {
    null
  }

  // 2. Fallback to inverted luminance (handles dark mode, glare, or inverted screens)
  if (text.isNullOrBlank()) {
    text = try {
      val invertedSource = source.invert()
      val bitmap = BinaryBitmap(HybridBinarizer(invertedSource))
      reader.decodeWithState(bitmap)?.text
    } catch (_: Exception) {
      null
    }
  }

  return text?.trim()
}

fun extractHostIp(raw: String): String {
  val clean = raw.trim()
  if (clean.startsWith("CHESSORA|")) {
    return clean
  }
  val ipPortRegex = Regex("""^([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}(:[0-9]+)?)$""")
  if (ipPortRegex.matches(clean)) {
    return clean
  }
  val ipRegex = Regex("""([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}(:[0-9]+)?)""")
  val match = ipRegex.find(clean)
  return match?.value ?: clean
}

