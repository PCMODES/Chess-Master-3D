package com.example.chessora.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChessDao {

  // User Profile
  @Query("SELECT * FROM user_profile WHERE id = 1")
  fun getUserProfile(): Flow<UserProfileEntity?>

  @Query("SELECT * FROM user_profile WHERE id = 1")
  suspend fun getUserProfileOnce(): UserProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

  // Campaign Progression
  @Query("SELECT * FROM campaign_progress ORDER BY levelNumber ASC")
  fun getAllCampaignProgress(): Flow<List<CampaignLevelProgressEntity>>

  @Query("SELECT * FROM campaign_progress WHERE levelNumber = :levelNumber")
  suspend fun getCampaignLevelProgress(levelNumber: Int): CampaignLevelProgressEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateCampaignProgress(progress: CampaignLevelProgressEntity)

  @Query("UPDATE user_profile SET maxUnlockedLevel = :newLevel WHERE id = 1 AND maxUnlockedLevel < :newLevel")
  suspend fun updateMaxUnlockedLevel(newLevel: Int)

  // Matches
  @Query("SELECT * FROM match_history ORDER BY timestamp DESC")
  fun getAllMatches(): Flow<List<MatchHistoryEntity>>

  @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 10")
  fun getRecentMatches(): Flow<List<MatchHistoryEntity>>

  @Insert
  suspend fun insertMatch(match: MatchHistoryEntity): Long

  @Query("DELETE FROM match_history")
  suspend fun clearMatchHistory()

  // Puzzles
  @Query("SELECT * FROM puzzles ORDER BY id ASC")
  fun getAllPuzzles(): Flow<List<PuzzleEntity>>

  @Query("SELECT * FROM puzzles WHERE id = :id")
  suspend fun getPuzzleById(id: Int): PuzzleEntity?

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertPuzzles(puzzles: List<PuzzleEntity>)

  @Update
  suspend fun updatePuzzle(puzzle: PuzzleEntity)

  // Achievements
  @Query("SELECT * FROM achievements ORDER BY id ASC")
  fun getAllAchievements(): Flow<List<AchievementEntity>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAchievements(achievements: List<AchievementEntity>)

  @Update
  suspend fun updateAchievement(achievement: AchievementEntity)

  @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id AND isUnlocked = 0")
  suspend fun unlockAchievement(id: String, timestamp: Long = System.currentTimeMillis())
}
