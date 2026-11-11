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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessora.ai.Difficulty
import com.example.chessora.model.PieceColor
import com.example.chessora.ui.components.GoldButton
import com.example.chessora.ui.theme.GraphiteDark
import com.example.chessora.ui.theme.ObsidianBlack
import com.example.chessora.ui.theme.RoyalGold
import com.example.chessora.ui.theme.RoyalGoldLight
import com.example.chessora.ui.theme.RoyalSilver
import com.example.chessora.ui.theme.SurfaceBorder
import com.example.chessora.ui.theme.SurfaceDark
import com.example.chessora.ui.theme.TextMuted
import com.example.chessora.ui.theme.TextPrimary
import com.example.chessora.ui.theme.TextSecondary

data class TimeControlOption(val label: String, val minutes: Int, val incrementSec: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayVsAiScreen(
  onBackClicked: () -> Unit,
  onStartMatch: (difficulty: Difficulty, color: PieceColor, timeMinutes: Int, incrementSec: Int) -> Unit
) {
  var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
  var selectedColorOption by remember { mutableStateOf("WHITE") } // WHITE, BLACK, RANDOM
  var selectedTimeControl by remember { mutableStateOf(TimeControlOption("Rapid 10m", 10, 0)) }

  val timeControlOptions = listOf(
    TimeControlOption("Untimed", 0, 0),
    TimeControlOption("Blitz 3m+2s", 3, 2),
    TimeControlOption("Rapid 10m", 10, 0),
    TimeControlOption("Classical 15m", 15, 10)
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "PLAY VS AI",
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
    containerColor = ObsidianBlack,
    bottomBar = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(GraphiteDark)
          .padding(20.dp)
      ) {
        GoldButton(
          text = "START BATTLE",
          icon = Icons.Default.PlayArrow,
          onClick = {
            val color = when (selectedColorOption) {
              "WHITE" -> PieceColor.WHITE
              "BLACK" -> PieceColor.BLACK
              else -> if (kotlin.random.Random.nextBoolean()) PieceColor.WHITE else PieceColor.BLACK
            }
            onStartMatch(
              selectedDifficulty,
              color,
              selectedTimeControl.minutes,
              selectedTimeControl.incrementSec
            )
          },
          modifier = Modifier.fillMaxWidth(),
          testTag = "start_ai_match_button"
        )
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Color Selection
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "CHOOSE YOUR SIDE",
          color = RoyalGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          ColorSelectionCard(
            title = "White",
            subtitle = "Move First",
            isSelected = selectedColorOption == "WHITE",
            onClick = { selectedColorOption = "WHITE" },
            circleColor = RoyalGold,
            modifier = Modifier.weight(1f)
          )
          ColorSelectionCard(
            title = "Black",
            subtitle = "Defend",
            isSelected = selectedColorOption == "BLACK",
            onClick = { selectedColorOption = "BLACK" },
            circleColor = Color(0xFF202634),
            modifier = Modifier.weight(1f)
          )
          ColorSelectionCard(
            title = "Random",
            subtitle = "Surprise",
            isSelected = selectedColorOption == "RANDOM",
            onClick = { selectedColorOption = "RANDOM" },
            icon = Icons.Default.Casino,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // 2. Time Control
      item {
        Text(
          text = "TIME CONTROL",
          color = RoyalGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          for (tc in timeControlOptions) {
            val isSelected = selectedTimeControl == tc
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) RoyalGold.copy(alpha = 0.18f) else SurfaceDark,
              border = BorderStroke(1.dp, if (isSelected) RoyalGold else SurfaceBorder),
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable { selectedTimeControl = tc }
            ) {
              Column(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = tc.label,
                  color = if (isSelected) RoyalGoldLight else TextPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      // 3. AI Difficulty Selector
      item {
        Text(
          text = "SELECT AI OPPONENT",
          color = RoyalGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }

      items(Difficulty.entries.size) { index ->
        val diff = Difficulty.entries[index]
        val isSelected = selectedDifficulty == diff

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceDark else GraphiteDark
          ),
          border = BorderStroke(1.dp, if (isSelected) RoyalGold else SurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { selectedDifficulty = diff }
            .testTag("ai_diff_${diff.name.lowercase()}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = if (isSelected) RoyalGold else SurfaceBorder,
              modifier = Modifier.size(24.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ObsidianBlack,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = diff.displayName,
                  color = if (isSelected) RoyalGoldLight else TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = SurfaceDark
                ) {
                  Text(
                    text = "Elo ${diff.estimatedElo}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = diff.description,
                color = TextMuted,
                fontSize = 12.sp
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

@Composable
fun ColorSelectionCard(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  circleColor: Color? = null,
  icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = if (isSelected) SurfaceDark else GraphiteDark,
    border = BorderStroke(1.dp, if (isSelected) RoyalGold else SurfaceBorder),
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (circleColor != null) {
        Surface(
          shape = CircleShape,
          color = circleColor,
          border = BorderStroke(1.dp, SurfaceBorder),
          modifier = Modifier.size(28.dp)
        ) {}
      } else if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(28.dp))
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(text = title, color = if (isSelected) RoyalGoldLight else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      Text(text = subtitle, color = TextMuted, fontSize = 10.sp)
    }
  }
}
