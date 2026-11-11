package com.example.chessora.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chessora.data.CampaignLevelProgressEntity
import com.example.chessora.data.UserProfileEntity
import com.example.chessora.model.CampaignLevel
import com.example.chessora.model.CampaignLevelsProvider
import com.example.chessora.ui.components.DarkOutlinedButton
import com.example.chessora.ui.components.GoldButton
import com.example.chessora.ui.theme.AmberGlow
import com.example.chessora.ui.theme.DeepNavy
import com.example.chessora.ui.theme.ElectricBlue
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
import com.example.chessora.ui.theme.SurfaceDark
import com.example.chessora.ui.theme.SurfaceElevated
import com.example.chessora.ui.theme.TextMuted
import com.example.chessora.ui.theme.TextPrimary
import com.example.chessora.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignScreen(
  profile: UserProfileEntity?,
  allProgress: List<CampaignLevelProgressEntity>,
  onStartLevel: (Int) -> Unit,
  onBackClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val maxUnlocked = profile?.maxUnlockedLevel ?: 1
  val progressMap = remember(allProgress) {
    allProgress.associate { it.levelNumber to Pair(it.stars, it.isCompleted) }
  }

  val totalStars = remember(allProgress) {
    allProgress.sumOf { it.stars }
  }

  // Current chapter based on max unlocked
  val defaultChapter = ((maxUnlocked - 1) / 10) + 1
  var selectedChapter by remember { mutableIntStateOf(defaultChapter) }
  var inspectingLevel by remember { mutableStateOf<CampaignLevel?>(null) }

  val chapterLevels = remember(selectedChapter, maxUnlocked, progressMap) {
    CampaignLevelsProvider.getChapterLevels(
      chapter = selectedChapter,
      maxUnlockedLevel = maxUnlocked,
      progressMap = progressMap
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "LEVELS CAMPAIGN",
              color = RoyalGold,
              fontWeight = FontWeight.Black,
              fontSize = 17.sp,
              letterSpacing = 1.sp
            )
          }
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
        actions = {
          // Total Stars Pill
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, GoldBorder),
            modifier = Modifier.padding(end = 16.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = RoyalGold,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$totalStars Stars",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBlack)
      )
    },
    containerColor = ObsidianBlack,
    modifier = modifier.fillMaxSize()
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(
          Brush.verticalGradient(
            colors = listOf(ObsidianBlack, DeepNavy, ObsidianBlack)
          )
        )
        .testTag("campaign_screen"),
      contentPadding = PaddingValues(bottom = 36.dp)
    ) {
      // 1. Chapters Selector Carousel
      item {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(10) { idx ->
            val chNum = idx + 1
            val isSelected = chNum == selectedChapter
            val isUnlocked = (chNum - 1) * 10 < maxUnlocked

            FilterChip(
              selected = isSelected,
              onClick = { selectedChapter = chNum },
              label = {
                Text(
                  text = "Chapter $chNum",
                  fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                  fontSize = 13.sp
                )
              },
              leadingIcon = if (!isUnlocked) {
                {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TextMuted
                  )
                }
              } else null,
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RoyalGold,
                selectedLabelColor = ObsidianBlack,
                containerColor = SurfaceDark,
                labelColor = if (isUnlocked) TextPrimary else TextMuted
              ),
              border = BorderStroke(
                1.dp,
                if (isSelected) RoyalGoldLight else SurfaceBorder
              )
            )
          }
        }
      }

      // 2. Chapter Overview Banner
      item {
        val chapterName = chapterLevels.firstOrNull()?.chapterName ?: "The Peasant's Road"
        val chStars = chapterLevels.sumOf { it.stars }
        val maxStars = chapterLevels.size * 3

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.2.dp, GoldBorder),
          colors = CardDefaults.cardColors(containerColor = GraphiteDark)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "CHAPTER $selectedChapter",
                  color = RoyalGold,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp
                )
                Text(
                  text = chapterName,
                  color = TextPrimary,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Black
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceDark,
                border = BorderStroke(1.dp, SurfaceBorder)
              ) {
                Text(
                  text = "$chStars / $maxStars ⭐",
                  color = RoyalGoldLight,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
              progress = { if (maxStars > 0) chStars.toFloat() / maxStars.toFloat() else 0f },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = RoyalGold,
              trackColor = SurfaceDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "Win Level 1 to unlock Level 2, and conquer every chapter!",
              color = TextSecondary,
              fontSize = 11.5.sp
            )
          }
        }
      }

      // 3. Section Title
      item {
        Text(
          text = "LEVELS PATH (1 to 10)",
          color = RoyalGold,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          letterSpacing = 1.2.sp,
          modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 8.dp)
        )
      }

      // 4. Level Cards List
      items(chapterLevels) { level ->
        CampaignLevelCard(
          level = level,
          onClick = {
            if (level.isUnlocked) {
              inspectingLevel = level
            }
          }
        )
      }
    }

    // Level Details Dialog
    inspectingLevel?.let { level ->
      LevelDetailsDialog(
        level = level,
        onStartBattle = {
          inspectingLevel = null
          onStartLevel(level.levelNumber)
        },
        onDismiss = { inspectingLevel = null }
      )
    }
  }
}

@Composable
fun CampaignLevelCard(
  level: CampaignLevel,
  onClick: () -> Unit
) {
  val isCompleted = level.isCompleted
  val isCurrent = level.isUnlocked && !isCompleted
  val isLocked = !level.isUnlocked

  val borderColor = when {
    isCompleted -> RoyalGold
    isCurrent -> EmeraldGreen
    else -> SurfaceBorder
  }

  val containerColor = when {
    isCurrent -> SurfaceElevated
    isCompleted -> SurfaceDark
    else -> GraphiteDark.copy(alpha = 0.6f)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 5.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable(enabled = !isLocked) { onClick() }
      .testTag("campaign_level_${level.levelNumber}"),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(
      width = if (isCurrent) 1.8.dp else 1.dp,
      color = borderColor
    ),
    colors = CardDefaults.cardColors(containerColor = containerColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left: Level Number Node & Title
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Level Circle Node
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
              when {
                isCompleted -> Brush.linearGradient(listOf(RoyalGold, AmberGlow))
                isCurrent -> Brush.linearGradient(listOf(EmeraldGreen, EmeraldLight))
                else -> Brush.linearGradient(listOf(SurfaceDark, GraphiteDark))
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          if (isLocked) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(20.dp)
            )
          } else {
            Text(
              text = "${level.levelNumber}",
              color = if (isCompleted || isCurrent) ObsidianBlack else TextMuted,
              fontWeight = FontWeight.Black,
              fontSize = 17.sp
            )
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Level Title & Opponent
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = level.title,
              color = if (isLocked) TextMuted else TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 14.5.sp
            )
            if (level.isBossLevel) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = SoftRubyRed.copy(alpha = 0.2f),
                border = BorderStroke(0.8.dp, SoftRubyRed)
              ) {
                Text(
                  text = "BOSS",
                  color = SoftRubyRed,
                  fontWeight = FontWeight.Black,
                  fontSize = 9.sp,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "vs ${level.opponentName} • ${level.opponentTitle}",
            color = if (isLocked) TextMuted.copy(alpha = 0.7f) else TextSecondary,
            fontSize = 12.sp
          )
        }
      }

      // Right: Stars or Status Action
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (isCompleted) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            val stars = if (level.stars > 0) level.stars else 3
            for (i in 1..3) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (i <= stars) RoyalGold else SurfaceBorder,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        } else if (isCurrent) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldGreen.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, EmeraldGreen)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = EmeraldLight,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = "PLAY",
                color = EmeraldLight,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
              )
            }
          }
        } else {
          Text(
            text = "LOCKED",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
fun LevelDetailsDialog(
  level: CampaignLevel,
  onStartBattle: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = ObsidianBlack,
      border = BorderStroke(1.8.dp, RoyalGold),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("level_details_dialog")
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Level Crest / Header Icon
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
              Brush.sweepGradient(listOf(RoyalGold, AmberGlow, RoyalGoldDark, RoyalGold))
            )
            .padding(2.dp),
          contentAlignment = Alignment.Center
        ) {
          Surface(
            shape = CircleShape,
            color = SurfaceDark,
            modifier = Modifier.fillMaxSize()
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (level.isBossLevel) Icons.Default.MilitaryTech else Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = RoyalGold,
                modifier = Modifier.size(32.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "LEVEL ${level.levelNumber}",
          color = RoyalGold,
          fontSize = 13.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 1.sp
        )

        Text(
          text = level.title,
          color = TextPrimary,
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          textAlign = TextAlign.Center
        )

        Text(
          text = "Chapter ${level.chapterNumber}: ${level.chapterName}",
          color = TextSecondary,
          fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Opponent Details Card
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = SurfaceDark,
          border = BorderStroke(1.dp, SurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "OPPONENT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = ElectricBlue.copy(alpha = 0.2f)
              ) {
                Text(
                  text = "Elo ${level.opponentRating}",
                  color = ElectricBlue,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = level.opponentName,
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = level.description,
              color = TextSecondary,
              fontSize = 12.sp,
              lineHeight = 16.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Rewards Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = SurfaceDark,
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "REWARD", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text(text = "+${level.xpReward} XP", color = RoyalGold, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = SurfaceDark,
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "UNLOCKS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text(text = "Level ${level.levelNumber + 1}", color = EmeraldLight, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Battle Button
        GoldButton(
          text = if (level.isCompleted) "REPLAY LEVEL" else "START BATTLE",
          onClick = onStartBattle,
          icon = Icons.Default.PlayArrow,
          modifier = Modifier.fillMaxWidth(),
          testTag = "start_campaign_battle_button"
        )

        Spacer(modifier = Modifier.height(8.dp))

        DarkOutlinedButton(
          text = "CLOSE",
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          testTag = "close_campaign_dialog"
        )
      }
    }
  }
}
