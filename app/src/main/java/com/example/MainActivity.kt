package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.game.audio.BounceAudioEngine
import com.example.game.model.LevelDefinition
import com.example.game.model.LevelRepository
import com.example.game.multiplayer.MultiplayerSession
import com.example.ui.BounceGameScreen
import com.example.ui.MultiplayerDialog
import com.example.ui.StoryBookSelectScreen
import com.example.ui.theme.MyApplicationTheme

enum class GameScreen {
    STORYBOOK,
    IN_GAME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BounceTalesApp()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        BounceAudioEngine.stopMusic()
    }

    override fun onResume() {
        super.onResume()
        // Resume music if in menu or game
    }
}

@Composable
fun BounceTalesApp() {
    var currentScreen by remember { mutableStateOf(GameScreen.STORYBOOK) }
    var currentChapterIndex by remember { mutableIntStateOf(0) }
    var activeLevel by remember { mutableStateOf(LevelRepository.allLevels[0]) }
    var multiplayerSession by remember { mutableStateOf(MultiplayerSession()) }
    var showMultiplayerDialog by remember { mutableStateOf(false) }

    var isRetroKeypadMode by remember { mutableStateOf(false) }
    var isSoundMuted by remember { mutableStateOf(BounceAudioEngine.isMusicMuted()) }

    var unlockedRock by remember { mutableStateOf(false) }
    var unlockedBeach by remember { mutableStateOf(false) }

    val bestScores = remember {
        mutableStateMapOf(
            1 to 5736,
            2 to 4680,
            3 to 2440,
            4 to 3959,
            5 to 6120,
            6 to 7953,
            7 to 4232,
            8 to 4425
        )
    }

    val bestEggs = remember {
        mutableStateMapOf(
            1 to 30,
            2 to 30,
            3 to 30,
            4 to 30,
            5 to 30,
            6 to 30,
            7 to 30,
            8 to 30
        )
    }

    val bestTimes = remember {
        mutableStateMapOf(
            1 to "0:49",
            2 to "0:56",
            3 to "1:21",
            4 to "0:38",
            5 to "1:15",
            6 to "1:20",
            7 to "1:32",
            8 to "1:28"
        )
    }

    // Storybook background music "aw"
    LaunchedEffect(currentScreen) {
        if (currentScreen == GameScreen.STORYBOOK) {
            BounceAudioEngine.playMusic("aw")
        }
    }

    BackHandler(enabled = currentScreen == GameScreen.IN_GAME || showMultiplayerDialog) {
        if (showMultiplayerDialog) {
            showMultiplayerDialog = false
        } else if (currentScreen == GameScreen.IN_GAME) {
            currentScreen = GameScreen.STORYBOOK
            BounceAudioEngine.playMusic("aw")
        }
    }

    when (currentScreen) {
        GameScreen.STORYBOOK -> {
            StoryBookSelectScreen(
                currentChapterIndex = currentChapterIndex,
                onChapterChange = { currentChapterIndex = it },
                onStartGame = { selectedLevel ->
                    activeLevel = selectedLevel
                    currentScreen = GameScreen.IN_GAME
                },
                onOpenMultiplayer = { showMultiplayerDialog = true },
                onToggleSound = {
                    isSoundMuted = BounceAudioEngine.toggleMute()
                },
                isSoundMuted = isSoundMuted,
                bestScores = bestScores,
                bestEggs = bestEggs,
                bestTimes = bestTimes
            )

            if (showMultiplayerDialog) {
                MultiplayerDialog(
                    currentSession = multiplayerSession,
                    onDismiss = { showMultiplayerDialog = false },
                    onConfirm = { updatedSession ->
                        multiplayerSession = updatedSession
                        showMultiplayerDialog = false
                    }
                )
            }
        }

        GameScreen.IN_GAME -> {
            BounceGameScreen(
                level = activeLevel,
                multiplayerSession = multiplayerSession,
                isRetroKeypadMode = isRetroKeypadMode,
                onToggleKeypadMode = { isRetroKeypadMode = !isRetroKeypadMode },
                onExitToMenu = {
                    currentScreen = GameScreen.STORYBOOK
                    BounceAudioEngine.playMusic("aw")
                },
                onSaveLevelStats = { chapter, score, eggs, time ->
                    val prevScore = bestScores[chapter] ?: 0
                    if (score > prevScore) bestScores[chapter] = score
                    val prevEggs = bestEggs[chapter] ?: 0
                    if (eggs > prevEggs) bestEggs[chapter] = eggs
                    bestTimes[chapter] = time

                    if (activeLevel.unlockRock) unlockedRock = true
                    if (activeLevel.unlockBeach) unlockedBeach = true
                },
                unlockedRock = unlockedRock,
                unlockedBeach = unlockedBeach
            )
        }
    }
}
