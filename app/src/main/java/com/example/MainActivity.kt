package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chessora.ai.Difficulty
import com.example.chessora.model.PieceColor
import com.example.chessora.ui.screens.CampaignScreen
import com.example.chessora.ui.screens.GameScreen
import com.example.chessora.ui.screens.HomeScreen
import com.example.chessora.ui.screens.LocalMultiplayerScreen
import com.example.chessora.ui.screens.PassAndPlayScreen
import com.example.chessora.ui.screens.PlayVsAiScreen
import com.example.chessora.ui.screens.ProfileScreen
import com.example.chessora.ui.screens.PuzzlesScreen
import com.example.chessora.ui.screens.SettingsScreen
import com.example.chessora.ui.screens.SplashScreen
import com.example.chessora.ui.theme.ChessoraTheme
import com.example.chessora.ui.theme.ObsidianBlack
import com.example.chessora.viewmodel.ChessViewModel
import com.example.chessora.viewmodel.GameMode
import com.example.chessora.viewmodel.MultiplayerViewModel
import com.example.chessora.viewmodel.PuzzlesViewModel

const val ROUTE_SPLASH = "splash"
const val ROUTE_HOME = "home"
const val ROUTE_CAMPAIGN = "campaign"
const val ROUTE_PLAY_VS_AI = "play_vs_ai"
const val ROUTE_LOCAL_MULTIPLAYER = "local_multiplayer"
const val ROUTE_PASS_AND_PLAY = "pass_and_play"
const val ROUTE_GAME = "game"
const val ROUTE_PUZZLES = "puzzles"
const val ROUTE_PROFILE = "profile"
const val ROUTE_SETTINGS = "settings"

class MainActivity : ComponentActivity() {

  private val chessViewModel: ChessViewModel by viewModels()
  private val multiplayerViewModel: MultiplayerViewModel by viewModels()
  private val puzzlesViewModel: PuzzlesViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      ChessoraTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = ObsidianBlack
        ) {
          ChessoraApp(
            chessViewModel = chessViewModel,
            multiplayerViewModel = multiplayerViewModel,
            puzzlesViewModel = puzzlesViewModel
          )
        }
      }
    }
  }
}

@Composable
fun ChessoraApp(
  chessViewModel: ChessViewModel,
  multiplayerViewModel: MultiplayerViewModel,
  puzzlesViewModel: PuzzlesViewModel
) {
  val navController = rememberNavController()

  val userProfile by chessViewModel.userProfile.collectAsStateWithLifecycle()
  val recentMatches by chessViewModel.recentMatches.collectAsStateWithLifecycle()
  val campaignProgress by chessViewModel.allCampaignProgress.collectAsStateWithLifecycle()
  val chessUiState by chessViewModel.uiState.collectAsStateWithLifecycle()
  val multiplayerUiState by multiplayerViewModel.uiState.collectAsStateWithLifecycle()

  NavHost(
    navController = navController,
    startDestination = ROUTE_SPLASH,
    modifier = Modifier.fillMaxSize()
  ) {
    // 1. Splash Screen
    composable(ROUTE_SPLASH) {
      SplashScreen(
        onSplashFinished = {
          navController.navigate(ROUTE_HOME) {
            popUpTo(ROUTE_SPLASH) { inclusive = true }
          }
        }
      )
    }

    // 2. Home Dashboard
    composable(ROUTE_HOME) {
      HomeScreen(
        profile = userProfile,
        recentMatches = recentMatches,
        onPlayVsAiClicked = { navController.navigate(ROUTE_PLAY_VS_AI) },
        onCampaignClicked = { navController.navigate(ROUTE_CAMPAIGN) },
        onQuickPlayClicked = {
          chessViewModel.startNewGame(
            mode = GameMode.VS_AI,
            color = PieceColor.WHITE,
            difficulty = Difficulty.MEDIUM,
            timeMinutes = 10,
            incrementSec = 0,
            opponentName = "AI Medium"
          )
          navController.navigate(ROUTE_GAME)
        },
        onLocalMultiplayerClicked = { navController.navigate(ROUTE_LOCAL_MULTIPLAYER) },
        onPassAndPlayClicked = { navController.navigate(ROUTE_PASS_AND_PLAY) },
        onDailyPuzzlesClicked = { navController.navigate(ROUTE_PUZZLES) },
        onProfileClicked = { navController.navigate(ROUTE_PROFILE) },
        onSettingsClicked = { navController.navigate(ROUTE_SETTINGS) }
      )
    }

    // 2.5. Levels Campaign (Unlock progressive levels)
    composable(ROUTE_CAMPAIGN) {
      BackHandler { navController.popBackStack() }
      CampaignScreen(
        profile = userProfile,
        allProgress = campaignProgress,
        onStartLevel = { levelNum ->
          chessViewModel.startCampaignLevel(levelNum)
          navController.navigate(ROUTE_GAME)
        },
        onBackClicked = { navController.popBackStack() }
      )
    }

    // 3. Play vs AI Setup
    composable(ROUTE_PLAY_VS_AI) {
      BackHandler { navController.popBackStack() }
      PlayVsAiScreen(
        onBackClicked = { navController.popBackStack() },
        onStartMatch = { difficulty, color, timeMinutes, incrementSec ->
          chessViewModel.startNewGame(
            mode = GameMode.VS_AI,
            color = color,
            difficulty = difficulty,
            timeMinutes = timeMinutes,
            incrementSec = incrementSec,
            opponentName = "AI ${difficulty.displayName}"
          )
          navController.navigate(ROUTE_GAME)
        }
      )
    }

    // 4. Local Offline Multiplayer Setup
    composable(ROUTE_LOCAL_MULTIPLAYER) {
      BackHandler {
        multiplayerViewModel.disconnect()
        navController.popBackStack()
      }
      LocalMultiplayerScreen(
        uiState = multiplayerUiState,
        onCreateRoom = { color, name ->
          multiplayerViewModel.createRoom(color, name)
        },
        onJoinRoom = { connStr, name ->
          multiplayerViewModel.joinRoom(connStr, name)
        },
        onStartGame = {
          chessViewModel.startNewGame(
            mode = GameMode.LOCAL_MULTIPLAYER,
            color = multiplayerUiState.playerColor,
            timeMinutes = 10,
            incrementSec = 0,
            opponentName = multiplayerUiState.opponentName
          )
          navController.navigate(ROUTE_GAME)
        },
        onBackClicked = {
          multiplayerViewModel.disconnect()
          navController.popBackStack()
        }
      )
    }

    // 5. Pass & Play Setup
    composable(ROUTE_PASS_AND_PLAY) {
      BackHandler { navController.popBackStack() }
      PassAndPlayScreen(
        onBackClicked = { navController.popBackStack() },
        onStartMatch = { p1, p2, timeMins, autoRotate ->
          chessViewModel.startNewGame(
            mode = GameMode.PASS_AND_PLAY,
            color = PieceColor.WHITE,
            timeMinutes = timeMins,
            incrementSec = 0,
            opponentName = p2,
            autoRotate = autoRotate
          )
          navController.navigate(ROUTE_GAME)
        }
      )
    }

    // 6. Gameplay Screen
    composable(ROUTE_GAME) {
      BackHandler { navController.navigate(ROUTE_HOME) { popUpTo(ROUTE_HOME) { inclusive = true } } }
      GameScreen(
        viewModel = chessViewModel,
        uiState = chessUiState,
        onBackClicked = {
          navController.navigate(ROUTE_HOME) {
            popUpTo(ROUTE_HOME) { inclusive = true }
          }
        },
        onRematch = {
          chessViewModel.startNewGame(
            mode = chessUiState.gameMode,
            color = chessUiState.playerColor,
            difficulty = chessUiState.aiDifficulty,
            timeMinutes = if (chessUiState.isClockEnabled) (chessUiState.whiteTimeRemainingMs / 60000).toInt().coerceAtLeast(3) else 0,
            incrementSec = (chessUiState.timeIncrementMs / 1000).toInt(),
            opponentName = chessUiState.opponentName
          )
        },
        onNewGame = {
          navController.popBackStack()
        }
      )
    }

    // 7. Tactical Puzzles Screen
    composable(ROUTE_PUZZLES) {
      BackHandler { navController.popBackStack() }
      PuzzlesScreen(
        viewModel = puzzlesViewModel,
        onBackClicked = { navController.popBackStack() }
      )
    }

    // 8. Strategist Profile Screen
    composable(ROUTE_PROFILE) {
      BackHandler { navController.popBackStack() }
      ProfileScreen(
        profile = userProfile,
        allMatches = emptyList(),
        achievements = emptyList(),
        onUpdateUsername = { newName ->
          // Update profile username
        },
        onBackClicked = { navController.popBackStack() }
      )
    }

    // 9. Settings Screen
    composable(ROUTE_SETTINGS) {
      BackHandler { navController.popBackStack() }
      SettingsScreen(
        profile = userProfile,
        onSoundToggled = { enabled -> chessViewModel.toggleSound(enabled) },
        onMusicToggled = { enabled -> chessViewModel.toggleMusic(enabled) },
        onVolumeChanged = { volume -> chessViewModel.setVolume(volume) },
        onHapticsToggled = { enabled -> chessViewModel.toggleHaptics(enabled) },
        onBoardThemeSelected = { theme -> chessViewModel.setBoardTheme(theme) },
        onPieceThemeSelected = { pieceTheme -> chessViewModel.setPieceTheme(pieceTheme) },
        onCoordinatesToggled = { enabled -> chessViewModel.toggleCoordinates(enabled) },
        onAutoQueenToggled = { /* handled in repo */ },
        onResetProgress = { /* handled in repo */ },
        onBackClicked = { navController.popBackStack() }
      )
    }
  }
}
