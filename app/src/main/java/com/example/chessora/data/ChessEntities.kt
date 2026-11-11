package com.example.chessora.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
  @PrimaryKey val id: Int = 1,
  val username: String = "Grandmaster",
  val avatarId: Int = 1,
  val level: Int = 1,
  val xp: Int = 0,
  val rating: Int = 1200,
  val wins: Int = 0,
  val losses: Int = 0,
  val draws: Int = 0,
  val streak: Int = 0,
  val bestStreak: Int = 0,
  val puzzlesSolved: Int = 0,
  val maxUnlockedLevel: Int = 1, // Campaign progression level: Win 1 -> 2 unlocks, etc.
  val selectedBoardTheme: String = "OBSIDIAN_GOLD",
  val selectedPieceTheme: String = "ROYAL_GOLD",
  val is3DMode: Boolean = true,
  val soundEnabled: Boolean = true,
  val musicEnabled: Boolean = true,
  val soundVolume: Float = 0.85f,
  val hapticsEnabled: Boolean = true,
  val showCoordinates: Boolean = true,
  val showLegalMoves: Boolean = true,
  val autoQueen: Boolean = false
) {
  val winRate: Float get() {
    val total = wins + losses + draws
    return if (total == 0) 0f else (wins.toFloat() / total) * 100f
  }
}

@Entity(tableName = "campaign_progress")
data class CampaignLevelProgressEntity(
  @PrimaryKey val levelNumber: Int,
  val stars: Int = 0, // 0 to 3 stars earned
  val isCompleted: Boolean = false,
  val bestMoves: Int = 0,
  val completedAt: Long? = null
)

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val opponentName: String,
  val gameMode: String, // VS_AI, CAMPAIGN, PASS_AND_PLAY, LOCAL_MULTIPLAYER
  val playerColor: String,
  val difficulty: String,
  val result: String, // WIN, LOSS, DRAW
  val movesCount: Int,
  val durationSeconds: Int,
  val timestamp: Long = System.currentTimeMillis(),
  val pgn: String,
  val fen: String
)

@Entity(tableName = "puzzles")
data class PuzzleEntity(
  @PrimaryKey val id: Int,
  val title: String,
  val theme: String, // Mate in 1, Mate in 2, Fork, Pin, Endgame
  val difficulty: String,
  val fen: String,
  val solutionMoves: String, // e.g. "Qh7#" or "Nf7+,Kg8,Nh6#"
  val description: String,
  val rating: Int,
  val xpReward: Int,
  val isCompleted: Boolean = false
)

@Entity(tableName = "achievements")
data class AchievementEntity(
  @PrimaryKey val id: String,
  val title: String,
  val description: String,
  val icon: String,
  val xpReward: Int,
  val isUnlocked: Boolean = false,
  val unlockedAt: Long? = null
)
