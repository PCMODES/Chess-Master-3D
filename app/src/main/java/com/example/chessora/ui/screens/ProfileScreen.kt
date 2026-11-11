package com.example.chessora.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.chessora.data.AchievementEntity
import com.example.chessora.data.MatchHistoryEntity
import com.example.chessora.data.UserProfileEntity
import com.example.chessora.ui.components.LevelProgressBar
import com.example.chessora.ui.components.LuxuryCard
import com.example.chessora.ui.theme.ElectricBlue
import com.example.chessora.ui.theme.EmeraldGreen
import com.example.chessora.ui.theme.GraphiteDark
import com.example.chessora.ui.theme.ObsidianBlack
import com.example.chessora.ui.theme.RoyalGold
import com.example.chessora.ui.theme.RoyalGoldLight
import com.example.chessora.ui.theme.SoftRubyRed
import com.example.chessora.ui.theme.SurfaceBorder
import com.example.chessora.ui.theme.SurfaceDark
import com.example.chessora.ui.theme.SurfaceElevated
import com.example.chessora.ui.theme.TextMuted
import com.example.chessora.ui.theme.TextPrimary
import com.example.chessora.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  profile: UserProfileEntity?,
  allMatches: List<MatchHistoryEntity>,
  achievements: List<AchievementEntity>,
  onUpdateUsername: (String) -> Unit,
  onBackClicked: () -> Unit
) {
  val context = LocalContext.current
  var showEditNameDialog by remember { mutableStateOf(false) }
  var nameInput by remember { mutableStateOf(profile?.username ?: "Grandmaster") }

  val level = profile?.level ?: 1
  val xp = profile?.xp ?: 0
  val rating = profile?.rating ?: 1200
  val wins = profile?.wins ?: 0
  val losses = profile?.losses ?: 0
  val draws = profile?.draws ?: 0
  val streak = profile?.streak ?: 0
  val bestStreak = profile?.bestStreak ?: 0
  val puzzlesSolved = profile?.puzzlesSolved ?: 0
  val totalGames = wins + losses + draws
  val winRate = if (totalGames == 0) 0 else ((wins.toFloat() / totalGames) * 100).toInt()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "STRATEGIST PROFILE",
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 20.dp)
        .testTag("profile_screen"),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Profile Header Card
      item {
        LuxuryCard(modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                  Brush.sweepGradient(listOf(RoyalGold, SurfaceElevated, RoyalGold))
                ),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.ic_chessora_logo),
                contentDescription = "Avatar",
                modifier = Modifier
                  .size(74.dp)
                  .clip(CircleShape)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable {
                nameInput = profile?.username ?: "Grandmaster"
                showEditNameDialog = true
              }
            ) {
              Text(
                text = profile?.username ?: "Grandmaster",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Name",
                tint = RoyalGold,
                modifier = Modifier.size(16.dp)
              )
            }

            Text(
              text = "Rating: $rating • Ranked Chess Master",
              color = RoyalGoldLight,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LevelProgressBar(level = level, xp = xp, modifier = Modifier.fillMaxWidth())
          }
        }
      }

      // 2. Performance Stats Grid
      item {
        Text(
          text = "CAREER PERFORMANCE",
          color = RoyalGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(title = "MATCHES", value = "$totalGames", modifier = Modifier.weight(1f))
          StatCard(title = "WINS", value = "$wins", color = EmeraldGreen, modifier = Modifier.weight(1f))
          StatCard(title = "LOSSES", value = "$losses", color = SoftRubyRed, modifier = Modifier.weight(1f))
          StatCard(title = "DRAWS", value = "$draws", color = ElectricBlue, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(title = "WIN RATE", value = "$winRate%", color = RoyalGold, modifier = Modifier.weight(1f))
          StatCard(title = "BEST STREAK", value = "$bestStreak", color = Color(0xFFFF7043), modifier = Modifier.weight(1f))
          StatCard(title = "PUZZLES", value = "$puzzlesSolved", color = ElectricBlue, modifier = Modifier.weight(1f))
        }
      }

      // 3. Achievements Section
      item {
        Text(
          text = "ACHIEVEMENTS",
          color = RoyalGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }

      items(achievements) { ach ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (ach.isUnlocked) SurfaceDark else GraphiteDark,
          border = BorderStroke(1.dp, if (ach.isUnlocked) RoyalGold.copy(alpha = 0.6f) else SurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = if (ach.isUnlocked) RoyalGold.copy(alpha = 0.2f) else SurfaceElevated,
              modifier = Modifier.size(40.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = if (ach.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                  contentDescription = null,
                  tint = if (ach.isUnlocked) RoyalGold else TextMuted,
                  modifier = Modifier.size(22.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = ach.title,
                color = if (ach.isUnlocked) RoyalGoldLight else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = ach.description,
                color = TextSecondary,
                fontSize = 11.sp
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (ach.isUnlocked) EmeraldGreen.copy(alpha = 0.15f) else SurfaceElevated
            ) {
              Text(
                text = if (ach.isUnlocked) "UNLOCKED" else "+${ach.xpReward} XP",
                color = if (ach.isUnlocked) EmeraldGreen else TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }
        }
      }

      // 4. Match History PGN Section
      if (allMatches.isNotEmpty()) {
        item {
          Text(
            text = "MATCH ARCHIVE",
            color = RoyalGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }

        items(allMatches.take(10)) { match ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GraphiteDark,
            border = BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "vs ${match.opponentName}",
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Text(
                  text = "${match.gameMode.replace("_", " ")} • ${match.movesCount} moves • ${match.result}",
                  color = TextSecondary,
                  fontSize = 11.sp
                )
              }

              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("Chess PGN", match.pgn)
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "PGN copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy PGN",
                  tint = RoyalGold,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
      AlertDialog(
        onDismissRequest = { showEditNameDialog = false },
        title = { Text("Edit Tactician Name", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Username") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = RoyalGold,
              unfocusedBorderColor = SurfaceBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
        },
        confirmButton = {
          TextButton(onClick = {
            if (nameInput.isNotBlank()) {
              onUpdateUsername(nameInput.trim())
            }
            showEditNameDialog = false
          }) {
            Text("Save", color = RoyalGold, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showEditNameDialog = false }) {
            Text("Cancel", color = TextSecondary)
          }
        },
        containerColor = GraphiteDark
      )
    }
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  modifier: Modifier = Modifier,
  color: Color = TextPrimary
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = SurfaceDark,
    border = BorderStroke(1.dp, SurfaceBorder),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = title, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
  }
}
