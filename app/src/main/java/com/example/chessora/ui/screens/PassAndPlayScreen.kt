package com.example.chessora.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessora.ui.components.GoldButton
import com.example.chessora.ui.components.LuxuryCard
import com.example.chessora.ui.theme.EmeraldGreen
import com.example.chessora.ui.theme.GraphiteDark
import com.example.chessora.ui.theme.ObsidianBlack
import com.example.chessora.ui.theme.RoyalGold
import com.example.chessora.ui.theme.RoyalGoldLight
import com.example.chessora.ui.theme.SurfaceBorder
import com.example.chessora.ui.theme.SurfaceDark
import com.example.chessora.ui.theme.TextMuted
import com.example.chessora.ui.theme.TextPrimary
import com.example.chessora.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassAndPlayScreen(
  onBackClicked: () -> Unit,
  onStartMatch: (player1: String, player2: String, timeMinutes: Int, autoRotate: Boolean) -> Unit
) {
  var player1Name by remember { mutableStateOf("Player 1") }
  var player2Name by remember { mutableStateOf("Player 2") }
  var selectedTimeMinutes by remember { mutableStateOf(10) }
  var autoRotateBoard by remember { mutableStateOf(false) }

  val timeOptions = listOf(
    Pair("Untimed", 0),
    Pair("Blitz 5m", 5),
    Pair("Rapid 10m", 10),
    Pair("Classical 15m", 15)
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "PASS & PLAY",
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
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      LuxuryCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "TWO PLAYERS, ONE DEVICE",
            color = RoyalGold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Text(
            text = "Play face-to-face completely offline",
            color = TextSecondary,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Player 1 Name
          OutlinedTextField(
            value = player1Name,
            onValueChange = { player1Name = it },
            label = { Text("White Player Name") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("p1_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = RoyalGold,
              unfocusedBorderColor = SurfaceBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Player 2 Name
          OutlinedTextField(
            value = player2Name,
            onValueChange = { player2Name = it },
            label = { Text("Black Player Name") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("p2_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = RoyalGold,
              unfocusedBorderColor = SurfaceBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      // Time Control Card
      LuxuryCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "CHESS CLOCK",
            color = RoyalGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            for ((label, mins) in timeOptions) {
              val isSelected = selectedTimeMinutes == mins
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) RoyalGold.copy(alpha = 0.2f) else GraphiteDark,
                border = BorderStroke(1.dp, if (isSelected) RoyalGold else SurfaceBorder),
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { selectedTimeMinutes = mins }
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = label,
                    color = if (isSelected) RoyalGoldLight else TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }

      // Auto-Rotate Toggle Card
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Face-to-Face Mode (Fixed Board)",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = if (!autoRotateBoard)
                "Board stays fixed (Ideal for 2 players sitting opposite each other)"
              else
                "Flips board 180° after each move (For passing 1 phone back and forth)",
              color = if (!autoRotateBoard) EmeraldGreen else RoyalGoldLight,
              fontSize = 12.sp
            )
          }

          Switch(
            checked = autoRotateBoard,
            onCheckedChange = { autoRotateBoard = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = ObsidianBlack,
              checkedTrackColor = RoyalGold
            ),
            modifier = Modifier.testTag("auto_rotate_switch")
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      GoldButton(
        text = "START PASS & PLAY",
        icon = Icons.Default.PlayArrow,
        onClick = {
          onStartMatch(
            player1Name.ifBlank { "White" },
            player2Name.ifBlank { "Black" },
            selectedTimeMinutes,
            autoRotateBoard
          )
        },
        modifier = Modifier.fillMaxWidth(),
        testTag = "start_pass_play_button"
      )
    }
  }
}
