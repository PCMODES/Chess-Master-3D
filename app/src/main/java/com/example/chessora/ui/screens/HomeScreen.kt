package com.example.chessora.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.chessora.data.MatchHistoryEntity
import com.example.chessora.data.UserProfileEntity
import com.example.chessora.ui.components.getLevelTitle
import com.example.chessora.ui.theme.AmberGlow
import com.example.chessora.ui.theme.CoralRose
import com.example.chessora.ui.theme.DeepNavy
import com.example.chessora.ui.theme.ElectricBlue
import com.example.chessora.ui.theme.ElectricCyan
import com.example.chessora.ui.theme.EmeraldGreen
import com.example.chessora.ui.theme.EmeraldLight
import com.example.chessora.ui.theme.GoldBorder
import com.example.chessora.ui.theme.GraphiteDark
import com.example.chessora.ui.theme.ObsidianBlack
import com.example.chessora.ui.theme.RoyalGold
import com.example.chessora.ui.theme.RoyalGoldDark
import com.example.chessora.ui.theme.RoyalGoldLight
import com.example.chessora.ui.theme.RoyalSilver
import com.example.chessora.ui.theme.SoftRubyRed
import com.example.chessora.ui.theme.SunsetOrange
import com.example.chessora.ui.theme.SurfaceBorder
import com.example.chessora.ui.theme.SurfaceBorderGlow
import com.example.chessora.ui.theme.SurfaceDark
import com.example.chessora.ui.theme.SurfaceElevated
import com.example.chessora.ui.theme.TextMuted
import com.example.chessora.ui.theme.TextPrimary
import com.example.chessora.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
  profile: UserProfileEntity?,
  recentMatches: List<MatchHistoryEntity>,
  onPlayVsAiClicked: () -> Unit,
  onCampaignClicked: () -> Unit,
  onQuickPlayClicked: () -> Unit,
  onLocalMultiplayerClicked: () -> Unit,
  onPassAndPlayClicked: () -> Unit,
  onDailyPuzzlesClicked: () -> Unit,
  onProfileClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val level = profile?.level ?: 1
  val xp = profile?.xp ?: 0
  val requiredXp = level * 250
  val progress = (xp.toFloat() / requiredXp.toFloat()).coerceIn(0f, 1f)
  val rating = profile?.rating ?: 1200
  val streak = profile?.streak ?: 0
  val winRate = (profile?.winRate ?: 0f).toInt()
  val username = profile?.username ?: "Grandmaster"

  val todayDateStr = run {
    val cal = Calendar.getInstance()
    SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(cal.time)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            ObsidianBlack,
            DeepNavy,
            ObsidianBlack
          )
        )
      )
      .testTag("home_screen"),
    contentPadding = PaddingValues(bottom = 36.dp)
  ) {
    // 1. Sleek Top Bar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Brand & User Profile
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onProfileClicked() }
            .padding(4.dp)
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(
                Brush.sweepGradient(listOf(RoyalGold, AmberGlow, RoyalGoldDark, RoyalGold))
              )
              .padding(2.dp),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_chessora_logo),
              contentDescription = "Profile Avatar",
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "CHESSORA",
                color = RoyalGold,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                letterSpacing = 1.2.sp
              )
              Text(
                text = " 3D",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
              )
            }
            Text(
              text = "$username • ${getLevelTitle(level)}",
              color = TextSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Action Buttons: Rating Pill + Settings
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, GoldBorder),
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable { onProfileClicked() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = RoyalGold,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$rating",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }

          Surface(
            shape = CircleShape,
            color = SurfaceDark,
            border = BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.size(38.dp)
          ) {
            IconButton(
              onClick = onSettingsClicked,
              modifier = Modifier
                .fillMaxSize()
                .testTag("settings_button")
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = RoyalSilver,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }

    // 2. Cinematic Hero Card with 1-Tap Quick Play
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(22.dp))
          .testTag("hero_banner_card"),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.5.dp, GoldBorder),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
        ) {
          // Hero Background Image
          Image(
            painter = painterResource(id = R.drawable.img_chess_home_hero),
            contentDescription = "Chess 3D Master Hero",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          // Dark Royal Gradient Vignette
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Transparent,
                    ObsidianBlack.copy(alpha = 0.5f),
                    ObsidianBlack.copy(alpha = 0.95f)
                  ),
                  startY = 0f,
                  endY = 600f
                )
              )
          )

          // Content overlay
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            // Tagline chip
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = ObsidianBlack.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, GoldBorder)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = RoyalGold,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "THE ART OF STRATEGY",
                    color = RoyalGoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(20.dp),
                color = ObsidianBlack.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, SurfaceBorder)
              ) {
                Text(
                  text = "3D & 2D Offline",
                  color = TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            // Bottom Section: Title, Level Progress & Instant Quick Play Button
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Welcome Back",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                  )
                  Text(
                    text = "Master the Board",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                  )
                }

                // 1-Tap Quick Match CTA Button
                Button(
                  onClick = onQuickPlayClicked,
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGold,
                    contentColor = ObsidianBlack
                  ),
                  border = BorderStroke(1.dp, RoyalGoldLight),
                  contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                  modifier = Modifier.testTag("quick_play_hero_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = ObsidianBlack,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "QUICK PLAY",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // XP Progress Bar
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Level $level Progress",
                  color = TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "$xp / $requiredXp XP",
                  color = RoyalGoldLight,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = RoyalGold,
                trackColor = SurfaceDark.copy(alpha = 0.8f)
              )
            }
          }
        }
      }
    }

    // 3. Quick Stats Ribbon (Bright, High-Contrast, Glassmorphic)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Streak Card
        StatBox(
          modifier = Modifier.weight(1f),
          title = "STREAK",
          value = "$streak Days",
          icon = Icons.Default.Whatshot,
          iconTint = if (streak > 0) SunsetOrange else TextMuted,
          cardBackground = SurfaceDark
        )

        // Win Rate Card
        StatBox(
          modifier = Modifier.weight(1f),
          title = "WIN RATE",
          value = "$winRate%",
          icon = Icons.Default.AutoAwesome,
          iconTint = EmeraldLight,
          cardBackground = SurfaceDark
        )

        // Elo Rating Card
        StatBox(
          modifier = Modifier.weight(1f),
          title = "RATING",
          value = "$rating",
          icon = Icons.Default.Bolt,
          iconTint = RoyalGold,
          cardBackground = SurfaceDark
        )
      }
    }

    // 4. Section Header: Game Modes
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 22.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "CHOOSE GAME MODE",
          color = RoyalGold,
          fontWeight = FontWeight.Black,
          fontSize = 13.sp,
          letterSpacing = 1.2.sp
        )
        Text(
          text = "100% Offline",
          color = EmeraldGreen,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        )
      }
    }

    // 5. Rich Game Mode Cards (Vibrant, Intuitive, Simple)
    // Mode 1: Levels Campaign (Beat Level 1 -> Unlock Level 2 -> Unlimited!)
    item {
      val maxUnlocked = profile?.maxUnlockedLevel ?: 1
      VibrantGameModeCard(
        title = "LEVELS CAMPAIGN",
        badge = "LEVEL $maxUnlocked UNLOCKED • UNLIMITED",
        subtitle = "Win Level 1 to unlock Level 2! 100+ progressive stages with 3-star mastery",
        icon = Icons.Default.MilitaryTech,
        gradientColors = listOf(Color(0xFF2C163D), SurfaceDark),
        accentColor = RoyalGold,
        badgeBg = Color(0x33F5C542),
        buttonText = "PLAY STAGE $maxUnlocked",
        onClick = onCampaignClicked,
        testTag = "mode_campaign"
      )
    }

    // Mode 2: Play vs AI
    item {
      VibrantGameModeCard(
        title = "PLAY VS AI",
        badge = "SOLO • 6 LEVELS",
        subtitle = "Challenge offline AI engine from Novice to Grandmaster",
        icon = Icons.Default.Psychology,
        gradientColors = listOf(Color(0xFF2C220E), SurfaceDark),
        accentColor = RoyalGold,
        badgeBg = Color(0x33F5C542),
        buttonText = "CHALLENGE",
        onClick = onPlayVsAiClicked,
        testTag = "mode_vs_ai"
      )
    }

    // Mode 2: Pass & Play
    item {
      VibrantGameModeCard(
        title = "PASS & PLAY",
        badge = "2 PLAYERS • 1 DEVICE",
        subtitle = "Tabletop chess with friends with optional board auto-flip",
        icon = Icons.Default.Smartphone,
        gradientColors = listOf(Color(0xFF0F2B20), SurfaceDark),
        accentColor = EmeraldGreen,
        badgeBg = Color(0x3310B981),
        buttonText = "PASS & PLAY",
        onClick = onPassAndPlayClicked,
        testTag = "mode_pass_play"
      )
    }

    // Mode 3: Local Multiplayer
    item {
      VibrantGameModeCard(
        title = "LOCAL MULTIPLAYER",
        badge = "CAMERA QR & HOTSPOT",
        subtitle = "Play on two separate devices via local Wi-Fi or camera scan",
        icon = Icons.Default.QrCodeScanner,
        gradientColors = listOf(Color(0xFF0D253A), SurfaceDark),
        accentColor = ElectricCyan,
        badgeBg = Color(0x3306B6D4),
        buttonText = "CONNECT",
        onClick = onLocalMultiplayerClicked,
        testTag = "mode_multiplayer"
      )
    }

    // Mode 4: Daily Puzzles
    item {
      VibrantGameModeCard(
        title = "DAILY PUZZLES",
        badge = "365 DAYS • +100 XP",
        subtitle = "Mate in 1, 2, Forks & Pins with 36+ tactical positions",
        icon = Icons.Default.Extension,
        gradientColors = listOf(Color(0xFF2E171B), SurfaceDark),
        accentColor = CoralRose,
        badgeBg = Color(0x33F43F5E),
        buttonText = "SOLVE",
        onClick = onDailyPuzzlesClicked,
        testTag = "mode_puzzles"
      )
    }

    // 6. Today's Daily Puzzle Spotlight Card
    item {
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(18.dp))
          .clickable { onDailyPuzzlesClicked() }
          .testTag("daily_puzzle_spotlight"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, GoldBorder),
        colors = CardDefaults.cardColors(containerColor = GraphiteDark)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  Brush.linearGradient(listOf(SunsetOrange, CoralRose))
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "TODAY'S DAILY PUZZLE",
                  color = RoyalGold,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = EmeraldGreen.copy(alpha = 0.2f),
                  border = BorderStroke(0.8.dp, EmeraldGreen)
                ) {
                  Text(
                    text = "+100 XP",
                    color = EmeraldLight,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                  )
                }
              }
              Text(
                text = todayDateStr,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
              Text(
                text = "Tap to test your calculation today",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = RoyalGold,
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }

    // 7. Grandmaster Chess Tip of the Day
    item {
      Spacer(modifier = Modifier.height(6.dp))
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, SurfaceBorder)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = RoyalGold,
            modifier = Modifier
              .size(24.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "GRANDMASTER TIP OF THE DAY",
              color = RoyalGold,
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Control the center early with pawns (e4, d4). A centralized knight controls eight squares, while a knight on the rim controls only four.",
              color = TextSecondary,
              fontSize = 12.sp,
              lineHeight = 17.sp
            )
          }
        }
      }
    }

    // 8. Recent Battles Section (if available)
    if (recentMatches.isNotEmpty()) {
      item {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "RECENT MATCHES",
          color = RoyalGold,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          letterSpacing = 1.2.sp,
          modifier = Modifier.padding(start = 22.dp, top = 8.dp, bottom = 8.dp)
        )
      }

      items(recentMatches.take(3)) { match ->
        RecentMatchRow(match = match)
      }
    }
  }
}

@Composable
fun StatBox(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  icon: ImageVector,
  iconTint: Color,
  cardBackground: Color
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = cardBackground,
    border = BorderStroke(1.dp, SurfaceBorder)
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        color = TextPrimary,
        fontWeight = FontWeight.Black,
        fontSize = 14.sp
      )
      Text(
        text = title,
        color = TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
    }
  }
}

@Composable
fun VibrantGameModeCard(
  title: String,
  badge: String,
  subtitle: String,
  icon: ImageVector,
  gradientColors: List<Color>,
  accentColor: Color,
  badgeBg: Color,
  buttonText: String,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 18.dp, vertical = 5.dp)
      .clip(RoundedCornerShape(18.dp))
      .clickable { onClick() }
      .testTag(testTag),
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.4f)),
    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(gradientColors)
        )
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Icon badge with vibrant accent
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.2f))
            .padding(1.dp),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Titles and description
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = title,
              color = TextPrimary,
              fontWeight = FontWeight.Black,
              fontSize = 15.sp,
              letterSpacing = 0.5.sp
            )
          }

          Spacer(modifier = Modifier.height(2.dp))

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = badgeBg
          ) {
            Text(
              text = badge,
              color = accentColor,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = subtitle,
            color = TextSecondary,
            fontSize = 11.5.sp,
            lineHeight = 15.sp
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Action Arrow / Button
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = accentColor.copy(alpha = 0.18f),
          border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = buttonText,
              color = accentColor,
              fontWeight = FontWeight.Black,
              fontSize = 11.sp,
              letterSpacing = 0.5.sp
            )
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun RecentMatchRow(match: MatchHistoryEntity) {
  val isWin = match.result == "WIN"
  val isDraw = match.result == "DRAW"
  val resultColor = if (isWin) EmeraldGreen else if (isDraw) ElectricBlue else SoftRubyRed

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 18.dp, vertical = 4.dp),
    shape = RoundedCornerShape(12.dp),
    color = GraphiteDark,
    border = BorderStroke(1.dp, SurfaceBorder)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "vs ${match.opponentName}",
          color = TextPrimary,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp
        )
        Text(
          text = "${match.gameMode.replace("_", " ")} • ${match.movesCount} moves",
          color = TextMuted,
          fontSize = 11.sp
        )
      }

      Surface(
        shape = RoundedCornerShape(6.dp),
        color = resultColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, resultColor.copy(alpha = 0.5f))
      ) {
        Text(
          text = match.result,
          color = resultColor,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
    }
  }
}
