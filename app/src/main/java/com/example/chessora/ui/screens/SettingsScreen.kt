package com.example.chessora.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessora.data.UserProfileEntity
import com.example.chessora.ui.board.BoardThemeType
import com.example.chessora.ui.board.PieceThemeType
import com.example.chessora.ui.components.LuxuryCard
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.example.chessora.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  profile: UserProfileEntity?,
  onSoundToggled: (Boolean) -> Unit,
  onMusicToggled: (Boolean) -> Unit = {},
  onVolumeChanged: (Float) -> Unit = {},
  onHapticsToggled: (Boolean) -> Unit,
  onBoardThemeSelected: (BoardThemeType) -> Unit,
  onPieceThemeSelected: (PieceThemeType) -> Unit,
  onCoordinatesToggled: (Boolean) -> Unit,
  onAutoQueenToggled: (Boolean) -> Unit,
  onResetProgress: () -> Unit,
  onBackClicked: () -> Unit
) {
  val context = LocalContext.current
  var showResetDialog by remember { mutableStateOf(false) }

  val soundEnabled = profile?.soundEnabled ?: true
  val musicEnabled = profile?.musicEnabled ?: true
  var soundVolume by remember(profile?.soundVolume) { mutableStateOf(profile?.soundVolume ?: 0.85f) }
  val hapticsEnabled = profile?.hapticsEnabled ?: true
  val showCoordinates = profile?.showCoordinates ?: true
  val autoQueen = profile?.autoQueen ?: false
  val currentBoardTheme = BoardThemeType.fromId(profile?.selectedBoardTheme ?: "OBSIDIAN_GOLD")
  val currentPieceTheme = PieceThemeType.fromId(profile?.selectedPieceTheme ?: "ROYAL_GOLD")

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "SETTINGS",
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
        .testTag("settings_screen"),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Audio & Feedback
      item {
        SectionTitle(title = "AUDIO & HAPTICS", icon = Icons.Default.VolumeUp)
        LuxuryCard(modifier = Modifier.fillMaxWidth()) {
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Sound Effects Toggle
            SettingToggleRow(
              title = "Sound Effects",
              subtitle = "Move clicks, piece strikes, and check alerts",
              checked = soundEnabled,
              onCheckedChange = onSoundToggled,
              testTag = "sound_toggle"
            )

            // Background Music Toggle
            SettingToggleRow(
              title = "Background Ambient Music",
              subtitle = "Calming acoustic chess lounge chords",
              checked = musicEnabled,
              onCheckedChange = onMusicToggled,
              testTag = "music_toggle"
            )

            // Master Volume Slider
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Sound & Music Volume",
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Text(
                  text = "${(soundVolume * 100).toInt()}%",
                  color = RoyalGoldLight,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
              Slider(
                value = soundVolume,
                onValueChange = {
                  soundVolume = it
                  onVolumeChanged(it)
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                  thumbColor = RoyalGold,
                  activeTrackColor = RoyalGold,
                  inactiveTrackColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth().testTag("volume_slider")
              )
            }

            // Haptic Vibration Toggle
            SettingToggleRow(
              title = "Haptic Vibration",
              subtitle = "Tactile phone feedback on moves and checks",
              checked = hapticsEnabled,
              onCheckedChange = onHapticsToggled,
              testTag = "haptics_toggle"
            )
          }
        }
      }

      // 2. Board Themes
      item {
        SectionTitle(title = "BOARD THEMES", icon = Icons.Default.Palette)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          for (theme in BoardThemeType.entries) {
            val isSelected = currentBoardTheme == theme
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) SurfaceDark else GraphiteDark,
              border = BorderStroke(1.dp, if (isSelected) RoyalGold else SurfaceBorder),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onBoardThemeSelected(theme) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Color swatch preview
                  Row(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(RoundedCornerShape(6.dp))
                  ) {
                    Box(modifier = Modifier.weight(1f).fillMaxSize().background(theme.lightSquare))
                    Box(modifier = Modifier.weight(1f).fillMaxSize().background(theme.darkSquare))
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Text(
                    text = theme.displayName,
                    color = if (isSelected) RoyalGoldLight else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }

                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = RoyalGold,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 3. Piece Themes
      item {
        SectionTitle(title = "PIECE MATERIAL STYLES", icon = Icons.Default.Tune)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          for (pieceTheme in PieceThemeType.entries) {
            val isSelected = currentPieceTheme == pieceTheme
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) SurfaceDark else GraphiteDark,
              border = BorderStroke(1.dp, if (isSelected) RoyalGold else SurfaceBorder),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onPieceThemeSelected(pieceTheme) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Row(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                  ) {
                    Box(modifier = Modifier.weight(1f).fillMaxSize().background(pieceTheme.whiteBaseColor))
                    Box(modifier = Modifier.weight(1f).fillMaxSize().background(pieceTheme.blackBaseColor))
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Text(
                    text = pieceTheme.displayName,
                    color = if (isSelected) RoyalGoldLight else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }

                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = RoyalGold,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 4. Gameplay Preferences
      item {
        SectionTitle(title = "GAMEPLAY PREFERENCES", icon = Icons.Default.Settings)
        LuxuryCard(modifier = Modifier.fillMaxWidth()) {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SettingToggleRow(
              title = "Board Coordinates",
              subtitle = "Show rank (1-8) and file (a-h) notations",
              checked = showCoordinates,
              onCheckedChange = onCoordinatesToggled,
              testTag = "coords_toggle"
            )

            SettingToggleRow(
              title = "Auto-Queen Promotion",
              subtitle = "Automatically promote pawns to Queen",
              checked = autoQueen,
              onCheckedChange = onAutoQueenToggled,
              testTag = "auto_queen_toggle"
            )
          }
        }
      }

      // 5. Data Management
      item {
        SectionTitle(title = "DATA & RESET", icon = Icons.Default.RestartAlt)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SurfaceDark,
          border = BorderStroke(1.dp, SoftRubyRed.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { showResetDialog = true }
            .testTag("reset_progress_button")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.RestartAlt,
              contentDescription = null,
              tint = SoftRubyRed,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Reset Career & Statistics",
                color = SoftRubyRed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = "Clears match history and resets XP and level",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }
        }
      }

      // App info
      item {
        Spacer(modifier = Modifier.height(10.dp))
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(text = "CHESSORA 3D v1.0", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Text(text = "100% Offline Chess Engine & Peer Multiplayer", color = TextMuted, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
      }
    }

    if (showResetDialog) {
      AlertDialog(
        onDismissRequest = { showResetDialog = false },
        title = { Text("Reset All Progress?", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = { Text("This will erase all match history and reset your rating and XP back to default.", color = TextSecondary) },
        confirmButton = {
          TextButton(onClick = {
            showResetDialog = false
            onResetProgress()
            Toast.makeText(context, "Progress has been reset.", Toast.LENGTH_SHORT).show()
          }) {
            Text("Reset", color = SoftRubyRed, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showResetDialog = false }) {
            Text("Cancel", color = TextSecondary)
          }
        },
        containerColor = GraphiteDark
      )
    }
  }
}

@Composable
fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(bottom = 6.dp)
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = title,
      color = RoyalGold,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )
  }
}

@Composable
fun SettingToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = ObsidianBlack,
        checkedTrackColor = RoyalGold
      ),
      modifier = Modifier.testTag(testTag)
    )
  }
}
