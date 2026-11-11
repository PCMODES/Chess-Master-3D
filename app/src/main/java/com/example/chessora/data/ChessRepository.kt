package com.example.chessora.data

import com.example.chessora.model.GameResult
import com.example.chessora.model.PieceColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.max

class ChessRepository(private val dao: ChessDao) {

  val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
  val recentMatches: Flow<List<MatchHistoryEntity>> = dao.getRecentMatches()
  val allMatches: Flow<List<MatchHistoryEntity>> = dao.getAllMatches()
  val allPuzzles: Flow<List<PuzzleEntity>> = dao.getAllPuzzles()
  val allAchievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()
  val allCampaignProgress: Flow<List<CampaignLevelProgressEntity>> = dao.getAllCampaignProgress()

  suspend fun initializeStarterDataIfEmpty() = withContext(Dispatchers.IO) {
    var profile = dao.getUserProfileOnce()
    if (profile == null) {
      profile = UserProfileEntity(
        id = 1,
        username = "Grandmaster",
        avatarId = 1,
        level = 1,
        xp = 50,
        rating = 1200,
        wins = 0,
        losses = 0,
        draws = 0,
        streak = 0,
        bestStreak = 0,
        puzzlesSolved = 0
      )
      dao.insertOrUpdateProfile(profile)
    }

    // Master Puzzle Library for 365 Days Rotation
    val masterPuzzles = listOf(
      PuzzleEntity(1, "Scholar's Nemesis", "Mate in 1", "Easy", "r1bqkb1r/pppp1ppp/2n5/4p3/2B1n3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 4", "Qxf7#", "White has queen and bishop lined up against f7. Deliver checkmate in one strike!", 800, 80),
      PuzzleEntity(2, "Back-Rank Bolt", "Mate in 1", "Easy", "6k1/5ppp/8/8/8/8/8/R3K3 w - - 0 1", "Ra8#", "Black's king is trapped behind a wall of pawns. Exploit the vulnerable back rank!", 850, 85),
      PuzzleEntity(3, "Corridor Checkmate", "Mate in 1", "Easy", "6k1/5Npp/8/8/8/8/8/4Q1K1 w - - 0 1", "Qe8#", "Invade the enemy back rank and finish the game immediately.", 900, 90),
      PuzzleEntity(4, "Smothered Knight Strike", "Mate in 1", "Medium", "6rk/5Npp/8/8/8/8/8/6K1 w - - 0 1", "Nh6#", "The classic smothered mate: use the knight's jumping power to trap the cornered king.", 1100, 110),
      PuzzleEntity(5, "Hook Mate", "Mate in 1", "Medium", "6k1/5p1p/5Np1/8/8/8/8/3R2K1 w - - 0 1", "Rd8#", "Rook and Knight coordinate on the 8th rank to shut down all escape squares.", 1150, 115),
      PuzzleEntity(6, "Arabian Mate", "Mate in 1", "Medium", "7k/R7/5N2/8/8/8/8/4K3 w - - 0 1", "Rh7#", "Ancient Arabian checkmate pattern with rook supported by a knight on the 7th rank.", 1200, 120),
      PuzzleEntity(7, "Epaulette Mate", "Mate in 1", "Medium", "3rkr2/6Q1/8/8/8/8/8/4K3 w - - 0 1", "Qe7#", "The defender's own rooks block sideways flight squares. Queen delivers the fatal blow.", 1250, 125),
      PuzzleEntity(8, "Queen Infiltration", "Mate in 1", "Easy", "r4rk1/ppp2ppp/2n5/3qp3/3P4/2P2Q2/PP3PPP/R1B1R1K1 w - - 0 13", "Qxd5", "Capitalize on an undefended queen in the center of the board.", 950, 95),
      PuzzleEntity(9, "Boden's Bishop Cross", "Mate in 1", "Hard", "2kr3r/ppp2ppp/8/8/1B6/8/8/2K4B w - - 0 1", "Be7#", "Intersecting bishop diagonals slice through the castled king's defenses.", 1350, 135),
      PuzzleEntity(10, "Damiano's Pawn Squeeze", "Mate in 1", "Medium", "5rk1/6pp/7N/8/8/8/8/4Q1K1 w - - 0 1", "Qe6#", "Take advantage of the pinned pawn to deliver back-rank checkmate.", 1200, 120),
      PuzzleEntity(11, "Morphy's Opera Stunner", "Mate in 1", "Hard", "4kb1r/p2n1ppp/4q3/4p1B1/4P3/1Q6/PPP2PPP/2KR4 w k - 0 1", "Qb8+", "Paul Morphy's famous Opera House deflection and pin maneuver.", 1450, 145),
      PuzzleEntity(12, "Blackburne's Sunburst", "Mate in 1", "Medium", "r1b2rk1/ppp2ppp/8/4N3/2B5/8/PPP2PPP/R2K3R w - - 0 1", "Bxf7#", "Sacrificial bishop thrust breaking open the uncastled king.", 1100, 110),
      PuzzleEntity(13, "Royal Knight Fork", "Forks & Pins", "Medium", "r1b1k2r/pppp1ppp/8/2b1N3/2B1n2q/8/PPPP1PPP/RNBQK2R w KQkq - 0 6", "Bxf7+", "Disrupt Black's king and establish tactical dominance with a bishop check fork.", 1200, 120),
      PuzzleEntity(14, "Skewer the King", "Forks & Pins", "Medium", "4k3/8/8/8/8/8/r7/3R2K1 w - - 0 1", "Re1+", "Force the king away from defending the undefended rook.", 1250, 125),
      PuzzleEntity(15, "Absolute Pin Strike", "Forks & Pins", "Medium", "r1b1k2r/ppppqppp/2n5/4p3/2B1P3/3P1N2/PPP2PPP/R1BQK2R w KQkq - 0 1", "Bxf7+", "Exploit the absolute pin along the diagonal to seize tempo and material.", 1300, 130),
      PuzzleEntity(16, "Double Attack Fork", "Forks & Pins", "Easy", "r1bqkb1r/pppp1ppp/2n5/4p3/3Pn3/2N2N2/PPP2PPP/R1BQKB1R w KQkq - 0 5", "Nxe4", "Eliminate the central outpost knight and stabilize your pawn center.", 950, 95),
      PuzzleEntity(17, "Decoy and Pin", "Forks & Pins", "Hard", "r1b2rk1/pppp1ppp/8/8/1B1q4/8/PPP2PPP/R3K2R w KQ - 0 1", "Bxf8", "Lure the rook into an inescapable trap and simplify to a winning endgame.", 1400, 140),
      PuzzleEntity(18, "Anastasia's Trap", "Mate in 2", "Hard", "5rk1/1p3ppp/8/2N5/8/8/1Q3PPP/5RK1 w - - 0 1", "Qxb7", "Clear the file and infiltrate enemy territory with decisive coordination.", 1400, 140),
      PuzzleEntity(19, "Greek Gift Sacrifice", "Mate in 2", "Hard", "r1bq1rk1/ppp2ppp/2n1p3/3p4/3P4/2PB1N2/P1P2PPP/R2Q1RK1 w - - 0 1", "Bxh7+", "The classical bishop sacrifice on h7 opening up the king's fortress.", 1500, 150),
      PuzzleEntity(20, "Queen Sac Clearance", "Mate in 2", "Hard", "2r3k1/5ppp/8/8/8/8/1Q3PPP/4R1K1 w - - 0 1", "Qe5", "Threaten mate along the e-file and back rank simultaneously.", 1450, 145),
      PuzzleEntity(21, "Deflection of Defender", "Mate in 2", "Hard", "3r2k1/p4ppp/1p6/8/8/8/1Q3PPP/3R2K1 w - - 0 1", "Rxd8#", "Deflect the back rank guardian and deliver unavoidable mate.", 1350, 135),
      PuzzleEntity(22, "Overloaded Defender", "Mate in 2", "Expert", "r1b2rk1/2q1bppp/p1n1p3/1p1pP3/5P2/P1NBQN2/1PP3PP/2KR3R w - - 0 1", "Bxh7+", "Shatter the castled shell when the opponent's pieces are poorly coordinated.", 1600, 160),
      PuzzleEntity(23, "Windmill Squeeze", "Mate in 2", "Expert", "r4rk1/pb3ppp/1p6/8/8/8/1B3PPP/3R2K1 w - - 0 1", "Rd7", "Apply relentless linear pressure along open files and ranks.", 1650, 165),
      PuzzleEntity(24, "Underpromotion Ambush", "Endgame", "Hard", "7k/4P3/8/8/8/8/8/4K3 w - - 0 1", "e8=Q+", "Push the passed pawn to the eighth rank to create an immediate queen check.", 1350, 135),
      PuzzleEntity(25, "Endgame Breakthrough", "Endgame", "Hard", "8/5k2/8/4P3/8/8/8/4K3 w - - 0 1", "e6+", "Advance the passed pawn to restrict the king's active mobility.", 1500, 150),
      PuzzleEntity(26, "King Opposition", "Endgame", "Medium", "8/8/4k3/8/4K3/8/4P3/8 w - - 0 1", "Kd4", "Seize the direct vertical opposition to escort your passed pawn home.", 1200, 120),
      PuzzleEntity(27, "Lucena Bridge", "Endgame", "Expert", "1K6/1P1k4/8/8/8/8/2r5/1R6 w - - 0 1", "Rd1+", "Build the classic Lucena shelter bridge to promote the pawn safely.", 1700, 170),
      PuzzleEntity(28, "Philidor Draw Fortress", "Endgame", "Hard", "8/8/4k3/R7/8/8/4K1r1/8 w - - 0 1", "Kf3", "Evade rook harassment and build a reliable drawing barrier.", 1450, 145),
      PuzzleEntity(29, "Queen vs Advanced Pawn", "Endgame", "Hard", "8/8/8/8/8/5k2/4p3/4K1Q1 w - - 0 1", "Qf2+", "Blockade the dangerous passed pawn before it can queen.", 1400, 140),
      PuzzleEntity(30, "Rook vs Bishop Squeeze", "Endgame", "Medium", "8/8/8/4k3/8/8/3b4/3R1K2 w - - 0 1", "Ke2", "Corner the bishop with coordinated king and rook movements.", 1250, 125),
      PuzzleEntity(31, "Passed Pawn Highway", "Endgame", "Medium", "8/8/2k5/8/3P4/8/8/2K5 w - - 0 1", "Kd2", "Centralize the king behind the passed pawn for optimal defense.", 1150, 115),
      PuzzleEntity(32, "Triangulation King Walk", "Endgame", "Hard", "8/8/8/3k4/8/2K5/8/8 w - - 0 1", "Kd3", "Gain the opposition tempo and push the defender backward.", 1350, 135),
      PuzzleEntity(33, "Perpetual Check Escape", "Tactics", "Medium", "6k1/5p1p/6p1/8/8/8/5qPP/7K w - - 0 1", "h3", "Open an escape square (Luft) to avoid back-rank and queen tricks.", 1100, 110),
      PuzzleEntity(34, "Stalemate Refusal", "Tactics", "Medium", "7k/8/8/8/8/8/1r6/K7 w - - 0 1", "Kxb2", "Eliminate the sacrificial piece without falling into a stalemate trap.", 1050, 105),
      PuzzleEntity(35, "Zugzwang Vice", "Endgame", "Hard", "8/8/8/4k3/4p3/4K3/8/8 w - - 0 1", "Ke2", "Place the opponent in zugzwang where any move gives up ground.", 1400, 140),
      PuzzleEntity(36, "Grandmaster Finale", "Mate in 1", "Grandmaster", "r1bqkb1r/pppp1ppp/2n5/4p3/2B1n3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 4", "Qxf7#", "Deliver the final tactical execution with grandmaster authority.", 2000, 200)
    )
    dao.insertPuzzles(masterPuzzles)

    // Insert Default Achievements
    val starterAchievements = listOf(
      AchievementEntity("FIRST_BLOOD", "First Blood", "Win your first offline chess match", "Trophy", 100),
      AchievementEntity("MATE_MASTER", "Checkmate Artist", "Win a game by delivering checkmate", "Crown", 150),
      AchievementEntity("AI_SLAYER", "Silicon Master", "Defeat AI on Hard difficulty or higher", "Cpu", 250),
      AchievementEntity("PUZZLE_PRODIGY", "Puzzle Prodigy", "Solve 5 tactical chess puzzles", "Puzzle", 200),
      AchievementEntity("STREAK_TITAN", "Streak Titan", "Achieve a 3-game win streak", "Flame", 300),
      AchievementEntity("SPEED_DEMON", "Speed Demon", "Win a game in under 20 moves", "Zap", 200),
      AchievementEntity("TACTICIAN", "Grand Tactician", "Win 5 matches total", "Medal", 250),
      AchievementEntity("LEVEL_UP", "Ascendance", "Reach player Level 5", "Star", 300)
    )
    dao.insertAchievements(starterAchievements)
  }

  suspend fun recordMatchFinished(
    opponentName: String,
    gameMode: String,
    playerColor: PieceColor,
    difficulty: String,
    result: GameResult,
    winner: PieceColor?,
    movesCount: Int,
    durationSeconds: Int,
    pgn: String,
    fen: String
  ): Pair<Int, Boolean> = withContext(Dispatchers.IO) {
    val isWin = winner == playerColor
    val isDraw = result == GameResult.STALEMATE ||
        result == GameResult.DRAW_FIFTY_MOVES ||
        result == GameResult.DRAW_REPETITION ||
        result == GameResult.DRAW_INSUFFICIENT_MATERIAL ||
        result == GameResult.DRAW_AGREEMENT

    val resultStr = if (isWin) "WIN" else if (isDraw) "DRAW" else "LOSS"

    // Save match
    dao.insertMatch(
      MatchHistoryEntity(
        opponentName = opponentName,
        gameMode = gameMode,
        playerColor = playerColor.name,
        difficulty = difficulty,
        result = resultStr,
        movesCount = movesCount,
        durationSeconds = durationSeconds,
        pgn = pgn,
        fen = fen
      )
    )

    // Calculate XP and Rating changes
    val baseXP = when {
      isWin -> 150
      isDraw -> 60
      else -> 30
    }
    val difficultyBonus = when (difficulty) {
      "Grandmaster" -> 100
      "Master" -> 75
      "Expert" -> 50
      "Hard" -> 30
      "Medium" -> 15
      else -> 0
    }
    val totalXpGained = baseXP + difficultyBonus

    val ratingDelta = when {
      isWin -> when (difficulty) {
        "Grandmaster" -> 25
        "Master" -> 22
        "Expert" -> 18
        "Hard" -> 15
        else -> 12
      }
      isDraw -> 2
      else -> -8
    }

    // Update Profile
    val current = dao.getUserProfileOnce() ?: UserProfileEntity()
    val newWins = if (isWin) current.wins + 1 else current.wins
    val newLosses = if (!isWin && !isDraw) current.losses + 1 else current.losses
    val newDraws = if (isDraw) current.draws + 1 else current.draws

    val newStreak = if (isWin) current.streak + 1 else 0
    val newBestStreak = max(current.bestStreak, newStreak)
    val newRating = max(400, current.rating + ratingDelta)

    var newXp = current.xp + totalXpGained
    var newLevel = current.level
    var leveledUp = false

    val xpForNextLevel = newLevel * 250
    if (newXp >= xpForNextLevel) {
      newLevel++
      newXp -= xpForNextLevel
      leveledUp = true
    }

    val updatedProfile = current.copy(
      level = newLevel,
      xp = newXp,
      rating = newRating,
      wins = newWins,
      losses = newLosses,
      draws = newDraws,
      streak = newStreak,
      bestStreak = newBestStreak
    )
    dao.insertOrUpdateProfile(updatedProfile)

    // Check Achievements
    if (isWin) {
      dao.unlockAchievement("FIRST_BLOOD")
      if (result == GameResult.CHECKMATE) {
        dao.unlockAchievement("MATE_MASTER")
      }
      if (difficulty in listOf("Hard", "Expert", "Master", "Grandmaster")) {
        dao.unlockAchievement("AI_SLAYER")
      }
      if (newStreak >= 3) {
        dao.unlockAchievement("STREAK_TITAN")
      }
      if (movesCount <= 20) {
        dao.unlockAchievement("SPEED_DEMON")
      }
      if (newWins >= 5) {
        dao.unlockAchievement("TACTICIAN")
      }
    }
    if (newLevel >= 5) {
      dao.unlockAchievement("LEVEL_UP")
    }

    Pair(totalXpGained, leveledUp)
  }

  suspend fun recordPuzzleSolved(puzzleId: Int) = withContext(Dispatchers.IO) {
    val puzzle = dao.getPuzzleById(puzzleId) ?: return@withContext
    if (!puzzle.isCompleted) {
      dao.updatePuzzle(puzzle.copy(isCompleted = true))

      val profile = dao.getUserProfileOnce() ?: return@withContext
      val newPuzzlesCount = profile.puzzlesSolved + 1
      var newXp = profile.xp + puzzle.xpReward
      var newLevel = profile.level
      val xpForNext = newLevel * 250
      if (newXp >= xpForNext) {
        newLevel++
        newXp -= xpForNext
      }
      val newRating = profile.rating + 10

      dao.insertOrUpdateProfile(
        profile.copy(
          puzzlesSolved = newPuzzlesCount,
          xp = newXp,
          level = newLevel,
          rating = newRating
        )
      )

      if (newPuzzlesCount >= 5) {
        dao.unlockAchievement("PUZZLE_PRODIGY")
      }
    }
  }

  suspend fun updateSettings(
    soundEnabled: Boolean? = null,
    musicEnabled: Boolean? = null,
    soundVolume: Float? = null,
    hapticsEnabled: Boolean? = null,
    is3DMode: Boolean? = null,
    boardTheme: String? = null,
    pieceTheme: String? = null,
    showCoordinates: Boolean? = null,
    autoQueen: Boolean? = null,
    username: String? = null,
    avatarId: Int? = null
  ) = withContext(Dispatchers.IO) {
    val current = dao.getUserProfileOnce() ?: return@withContext
    val updated = current.copy(
      soundEnabled = soundEnabled ?: current.soundEnabled,
      musicEnabled = musicEnabled ?: current.musicEnabled,
      soundVolume = soundVolume ?: current.soundVolume,
      hapticsEnabled = hapticsEnabled ?: current.hapticsEnabled,
      is3DMode = is3DMode ?: current.is3DMode,
      selectedBoardTheme = boardTheme ?: current.selectedBoardTheme,
      selectedPieceTheme = pieceTheme ?: current.selectedPieceTheme,
      showCoordinates = showCoordinates ?: current.showCoordinates,
      autoQueen = autoQueen ?: current.autoQueen,
      username = username ?: current.username,
      avatarId = avatarId ?: current.avatarId
    )
    dao.insertOrUpdateProfile(updated)
  }

  suspend fun recordCampaignLevelVictory(
    levelNumber: Int,
    movesCount: Int
  ): Triple<Int, Boolean, Int> = withContext(Dispatchers.IO) {
    val stars = when {
      movesCount <= 30 -> 3
      movesCount <= 45 -> 2
      else -> 1
    }

    val progress = CampaignLevelProgressEntity(
      levelNumber = levelNumber,
      stars = stars,
      isCompleted = true,
      bestMoves = movesCount,
      completedAt = System.currentTimeMillis()
    )
    dao.insertOrUpdateCampaignProgress(progress)

    // Unlock next level: Level N + 1
    dao.updateMaxUnlockedLevel(levelNumber + 1)

    // Award XP and Rating
    val profile = dao.getUserProfileOnce()
    val xpEarned = 100 + (levelNumber * 25)
    var leveledUp = false
    if (profile != null) {
      val newWins = profile.wins + 1
      val newStreak = profile.streak + 1
      val bestStreak = max(profile.bestStreak, newStreak)
      var newXp = profile.xp + xpEarned
      var newLevel = profile.level
      val xpForNext = newLevel * 250
      if (newXp >= xpForNext) {
        newLevel++
        newXp -= xpForNext
        leveledUp = true
      }
      val ratingGain = 15
      val newRating = profile.rating + ratingGain
      val newMaxLevel = max(profile.maxUnlockedLevel, levelNumber + 1)

      dao.insertOrUpdateProfile(
        profile.copy(
          wins = newWins,
          streak = newStreak,
          bestStreak = bestStreak,
          xp = newXp,
          level = newLevel,
          rating = newRating,
          maxUnlockedLevel = newMaxLevel
        )
      )
      if (newMaxLevel >= 5) {
        dao.unlockAchievement("LEVEL_UP")
      }
    }

    Triple(xpEarned, leveledUp, stars)
  }

  suspend fun resetProgress() = withContext(Dispatchers.IO) {
    dao.clearMatchHistory()
    dao.insertOrUpdateProfile(
      UserProfileEntity(
        id = 1,
        username = "Grandmaster",
        avatarId = 1,
        level = 1,
        xp = 0,
        rating = 1200,
        maxUnlockedLevel = 1
      )
    )
  }
}
