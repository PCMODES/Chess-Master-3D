package com.example.chessora.model

import com.example.chessora.ai.Difficulty
import kotlin.math.min

data class CampaignLevel(
  val levelNumber: Int,
  val chapterNumber: Int,
  val chapterName: String,
  val title: String,
  val opponentName: String,
  val opponentTitle: String,
  val opponentRating: Int,
  val aiDifficulty: Difficulty,
  val timeMinutes: Int,
  val xpReward: Int,
  val description: String,
  val isBossLevel: Boolean = false,
  val stars: Int = 0, // 0 to 3
  val isUnlocked: Boolean = false,
  val isCompleted: Boolean = false
)

object CampaignLevelsProvider {

  private val CHAPTER_NAMES = listOf(
    "The Peasant's Road",
    "The Knight's Tournament",
    "The Bishop's Sanctuary",
    "The Rook's Bastion",
    "The Queen's Citadel",
    "The King's Court",
    "Master's Crucible",
    "Grandmaster Legends",
    "World Championship",
    "Immortal Hall of Fame"
  )

  private val BOT_FIRST_NAMES = listOf(
    "Leo", "Gareth", "Robin", "Victor", "Mikhail", "Boris", "Elena", "Sophia",
    "Alexander", "Magnus", "Garry", "Anatoly", "Bobby", "Viswanathan", "Hikaru",
    "Fabiano", "Ding", "Alireza", "Ian", "Levon", "Maxime", "Wesley", "Sergey",
    "Vassily", "Tigran", "Paul", "Emanuel", "Jose", "Wilhelm", "Richard"
  )

  private val BOT_EPITHETS = listOf(
    "The Swift", "The Bold", "The Tactician", "The Iron Wall", "The Dragon",
    "The Bishop Whisperer", "The Endgame Virtuoso", "The Fortress", "The Maestro",
    "The Storm", "The Strategist", "The Unbreakable", "The Royal Shield"
  )

  /**
   * Generates or retrieves any level from 1 to Infinity.
   */
  fun getLevel(
    levelNumber: Int,
    maxUnlockedLevel: Int = 1,
    savedStars: Int = 0,
    savedCompleted: Boolean = false
  ): CampaignLevel {
    val chapterNumber = ((levelNumber - 1) / 10) + 1
    val chapterName = if (chapterNumber <= CHAPTER_NAMES.size) {
      CHAPTER_NAMES[chapterNumber - 1]
    } else {
      "Grandmaster Odyssey $chapterNumber"
    }

    val isBoss = levelNumber % 10 == 0 || levelNumber % 5 == 0
    val isChapterBoss = levelNumber % 10 == 0

    // Rating progression: Starts at 400 for Level 1, scales progressively up to 2800+
    val baseRating = 400 + ((levelNumber - 1) * 25)
    val opponentRating = min(3000, baseRating)

    // AI Difficulty mapping
    val difficulty = when {
      levelNumber <= 6 -> Difficulty.EASY
      levelNumber <= 20 -> Difficulty.MEDIUM
      levelNumber <= 40 -> Difficulty.HARD
      levelNumber <= 65 -> Difficulty.EXPERT
      levelNumber <= 85 -> Difficulty.MASTER
      else -> Difficulty.GRANDMASTER
    }

    // Time control
    val timeMinutes = when {
      isChapterBoss -> 15 // Deep battle
      levelNumber <= 10 -> 10
      levelNumber <= 30 -> 10
      levelNumber <= 60 -> 10
      else -> 10
    }

    // XP Reward
    val xp = 100 + (levelNumber * 20) + (if (isBoss) 150 else 0)

    val nameIndex = Math.floorMod(levelNumber * 7 + 3, BOT_FIRST_NAMES.size)
    val epithetIndex = Math.floorMod(levelNumber * 13 + 5, BOT_EPITHETS.size)
    val botName = if (isBoss) {
      "Boss ${BOT_FIRST_NAMES[nameIndex]} ${BOT_EPITHETS[epithetIndex]}"
    } else {
      "${BOT_FIRST_NAMES[nameIndex]} ${BOT_EPITHETS[epithetIndex]}"
    }

    val title = when {
      isChapterBoss -> "Chapter $chapterNumber Final Boss"
      isBoss -> "Gatekeeper Battle"
      levelNumber == 1 -> "First Step: The Novice"
      levelNumber == 2 -> "Peasant's Gambit"
      levelNumber == 3 -> "Knight's Outpost"
      levelNumber == 4 -> "Center Control Duel"
      else -> "Level $levelNumber: Clash of Minds"
    }

    val description = when {
      isChapterBoss -> "Defeat the Chapter Boss to conquer ${chapterName}!"
      isBoss -> "Defeat the guardian to unlock the path forward."
      levelNumber == 1 -> "Deliver checkmate to win Level 1 and unlock Level 2!"
      else -> "Outplay your opponent to claim stars and unlock Level ${levelNumber + 1}."
    }

    val unlocked = levelNumber <= maxUnlockedLevel
    val completed = savedCompleted || (levelNumber < maxUnlockedLevel)
    val effectiveStars = if (savedStars > 0) savedStars else if (completed) 3 else 0

    return CampaignLevel(
      levelNumber = levelNumber,
      chapterNumber = chapterNumber,
      chapterName = chapterName,
      title = title,
      opponentName = botName,
      opponentTitle = "Elo $opponentRating",
      opponentRating = opponentRating,
      aiDifficulty = difficulty,
      timeMinutes = timeMinutes,
      xpReward = xp,
      description = description,
      isBossLevel = isBoss,
      stars = effectiveStars,
      isUnlocked = unlocked,
      isCompleted = completed
    )
  }

  /**
   * Generates a batch of levels for a given chapter.
   */
  fun getChapterLevels(
    chapter: Int,
    maxUnlockedLevel: Int,
    progressMap: Map<Int, Pair<Int, Boolean>> = emptyMap()
  ): List<CampaignLevel> {
    val startLevel = (chapter - 1) * 10 + 1
    val endLevel = chapter * 10
    return (startLevel..endLevel).map { lvl ->
      val (stars, completed) = progressMap[lvl] ?: Pair(0, false)
      getLevel(
        levelNumber = lvl,
        maxUnlockedLevel = maxUnlockedLevel,
        savedStars = stars,
        savedCompleted = completed
      )
    }
  }
}
