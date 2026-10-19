package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.audio.BounceAudioEngine
import com.example.game.audio.SfxType
import com.example.game.model.*
import com.example.game.multiplayer.MultiplayerManager
import com.example.game.multiplayer.MultiplayerMode
import com.example.game.multiplayer.MultiplayerSession
import com.example.game.physics.GamePhysicsEngine
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun BounceGameScreen(
    level: LevelDefinition,
    multiplayerSession: MultiplayerSession,
    isRetroKeypadMode: Boolean,
    onToggleKeypadMode: () -> Unit,
    onExitToMenu: () -> Unit,
    onSaveLevelStats: (chapter: Int, score: Int, eggs: Int, time: String) -> Unit,
    unlockedRock: Boolean,
    unlockedBeach: Boolean
) {
    // Game State
    val player1 = remember(level) {
        PlayerBallState(
            id = 1,
            x = level.startX,
            y = level.startY,
            checkpointX = level.startX,
            checkpointY = level.startY
        )
    }

    val player2 = remember(level, multiplayerSession) {
        if (multiplayerSession.mode != MultiplayerMode.SOLO) {
            MultiplayerManager.initPlayer2(player1, multiplayerSession.player2Form)
        } else null
    }

    var moveInputP1 by remember { mutableFloatStateOf(0f) }
    var jumpInputP1 by remember { mutableStateOf(false) }

    var moveInputP2 by remember { mutableFloatStateOf(0f) }
    var jumpInputP2 by remember { mutableStateOf(false) }

    var isPaused by remember { mutableStateOf(false) }
    var isVictory by remember { mutableStateOf(false) }
    var gameTimeSeconds by remember { mutableIntStateOf(0) }
    var currentScore by remember { mutableIntStateOf(0) }
    var activeMoleDialog by remember { mutableStateOf(level.introDialogMole) }

    // Color restoration status: true if all machines destroyed
    var isColorRestored by remember {
        mutableStateOf(level.colorMachines.isEmpty())
    }

    val physics = remember(level) {
        GamePhysicsEngine(
            level = level,
            onColorMachineDestroyed = {
                if (level.colorMachines.all { it.isDestroyed }) {
                    isColorRestored = true
                }
                currentScore += 500
            },
            onEggCollected = {
                currentScore += 100
            },
            onLevelCompleted = {
                isVictory = true
                val mins = gameTimeSeconds / 60
                val secs = gameTimeSeconds % 60
                val timeStr = String.format("%d:%02d", mins, secs)
                onSaveLevelStats(level.chapterNumber, currentScore + 1000, player1.eggsCollected, timeStr)
            },
            onPlayerDied = {
                // Respawn after short delay
            }
        )
    }

    // Music playback
    LaunchedEffect(level) {
        BounceAudioEngine.playMusic(level.musicTrack)
    }

    // Game Loop (60 FPS)
    LaunchedEffect(isPaused, isVictory) {
        var frameCount = 0
        while (!isPaused && !isVictory) {
            delay(16) // ~60fps
            frameCount++
            if (frameCount % 60 == 0) {
                gameTimeSeconds++
            }

            // P1 Physics
            if (player1.isDead) {
                physics.respawnPlayer(player1)
            } else {
                physics.update(player1, moveInputP1, jumpInputP1)
            }

            // P2 Physics (if Multiplayer)
            player2?.let { p2 ->
                if (p2.isDead) {
                    physics.respawnPlayer(p2)
                } else {
                    if (multiplayerSession.mode == MultiplayerMode.CO_OP_AI) {
                        // AI companion logic
                        val nearestRockX = level.rocks.firstOrNull { !it.isBroken }?.let { it.x + it.width / 2f }
                        val (aiMove, aiJump) = MultiplayerManager.updateCompanionAI(
                            ai = p2,
                            leader = player1,
                            nearestBreakableRockX = nearestRockX,
                            jumpObstacleAhead = false
                        )
                        physics.update(p2, aiMove, aiJump)
                    } else {
                        // Human P2 input
                        physics.update(p2, moveInputP2, jumpInputP2)
                    }
                }
            }

            // Update debris/particles
            physics.updateParticles()
        }
    }

    // Dismiss dialog after 5 seconds
    LaunchedEffect(activeMoleDialog) {
        if (activeMoleDialog != null) {
            delay(5000)
            activeMoleDialog = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Canvas Game Renderer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Camera target: track player 1 (or midpoint with P2)
            val targetX = if (player2 != null) (player1.x + player2.x) / 2f else player1.x
            val targetY = if (player2 != null) (player1.y + player2.y) / 2f else player1.y

            val camX = (targetX - canvasW / 2f).coerceIn(0f, max(0f, level.levelWidth - canvasW))
            val camY = (targetY - canvasH / 2f).coerceIn(0f, max(0f, level.levelHeight - canvasH))

            // 1. Sky & Parallax Background
            val skyTop = if (isColorRestored) Color(level.world.skyColorTop) else Color(0xFF37474F)
            val skyBottom = if (isColorRestored) Color(level.world.skyColorBottom) else Color(0xFF78909C)

            drawRect(
                brush = Brush.verticalGradient(listOf(skyTop, skyBottom)),
                size = size
            )

            // Parallax Clouds / Beanstalks / Circus tents
            drawParallaxScenery(camX, canvasW, canvasH, level.world, isColorRestored)

            // 2. Terrain segments & Underground Earth
            drawTerrain(level.segments, camX, camY, level.world, isColorRestored)

            // 3. Water zones
            for (water in level.waterZones) {
                val wx = water.left - camX
                val wy = water.top - camY
                val ww = water.right - water.left
                val wh = water.bottom - water.top
                drawRect(
                    color = Color(0x9929B6F6),
                    topLeft = Offset(wx, wy),
                    size = Size(ww, wh)
                )
                // Surface wave line
                drawLine(
                    color = Color(0xEEB3E5FC),
                    start = Offset(wx, wy),
                    end = Offset(wx + ww, wy),
                    strokeWidth = 3f
                )
            }

            // 4. Bouncy Flowers
            for (flower in level.flowers) {
                drawBouncyFlower(flower, camX, camY, isColorRestored)
            }

            // 5. Bouncy Mushrooms
            for (shroom in level.mushrooms) {
                drawBouncyMushroom(shroom, camX, camY, isColorRestored)
            }

            // 6. Breakable Rocks
            for (rock in level.rocks) {
                if (!rock.isBroken) {
                    drawBreakableRock(rock, camX, camY)
                }
            }

            // 7. Spikes
            for (spike in level.spikes) {
                drawSpikes(spike, camX, camY)
            }

            // 8. Moving Platforms
            for (plat in level.movingPlatforms) {
                val px = plat.currentX - camX
                val py = plat.currentY - camY
                drawRoundRect(
                    color = Color(0xFF795548),
                    topLeft = Offset(px, py),
                    size = Size(plat.width, plat.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = Color(0xFF8D6E63),
                    topLeft = Offset(px + 2f, py + 2f),
                    size = Size(plat.width - 4f, 5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }

            // 9. Cannons
            for (cannon in level.cannons) {
                drawCannon(cannon, camX, camY)
            }

            // 10. Color Machines
            for (machine in level.colorMachines) {
                drawColorMachine(machine, camX, camY)
            }

            // 11. Checkpoint Flags
            for (cp in level.checkpoints) {
                drawCheckpoint(cp, camX, camY)
            }

            // 12. Golden Eggs
            for (egg in level.eggs) {
                if (!egg.isCollected) {
                    drawGoldenEgg(egg, camX, camY)
                }
            }

            // 13. Goal: Pumpkin House
            drawPumpkinHouse(level.goal, camX, camY)

            // 14. Particles (Sparkles, Confetti, Smoke)
            for (p in physics.particles) {
                drawCircle(
                    color = Color(p.color).copy(alpha = (p.life / p.maxLife).coerceIn(0f, 1f)),
                    radius = p.size,
                    center = Offset(p.x - camX, p.y - camY)
                )
            }

            // 15. Player 2 (if in multiplayer)
            player2?.let { p2 ->
                drawBallCharacter(p2, camX, camY, isPlayer2 = true)
            }

            // 16. Player 1 (Bounce)
            drawBallCharacter(player1, camX, camY, isPlayer2 = false)
        }

        // Top In-Game HUD: Score, Eggs, Timer, Level Name
        TopGameHud(
            chapterTitle = level.title,
            worldName = level.world.worldName,
            eggsCollected = player1.eggsCollected + (player2?.eggsCollected ?: 0),
            totalEggs = 30,
            score = currentScore,
            seconds = gameTimeSeconds,
            multiplayerMode = multiplayerSession.mode
        )

        // Mole Story Dialog Bubble (if active)
        activeMoleDialog?.let { dialogText ->
            MoleDialogBubble(
                text = dialogText,
                onDismiss = { activeMoleDialog = null }
            )
        }

        // Virtual Controls Overlay (Modern Virtual Touch or Retro Nokia Keypad)
        ControlsOverlay(
            isRetroKeypadMode = isRetroKeypadMode,
            onToggleKeypadMode = onToggleKeypadMode,
            onMoveP1 = { moveInputP1 = it },
            onJumpP1 = { jumpInputP1 = it },
            onSwitchFormP1 = {
                player1.form = it
                BounceAudioEngine.playSfx(SfxType.TRANSFORM)
            },
            currentFormP1 = player1.form,
            unlockedRock = unlockedRock || level.unlockRock,
            unlockedBeach = unlockedBeach || level.unlockBeach,
            isCoOpLocal = multiplayerSession.mode == MultiplayerMode.CO_OP_LOCAL,
            onMoveP2 = { moveInputP2 = it },
            onJumpP2 = { jumpInputP2 = it },
            onPause = { isPaused = true }
        )

        // Pause Menu Dialog
        if (isPaused) {
            PauseMenuDialog(
                onResume = { isPaused = false },
                onRestart = {
                    physics.respawnPlayer(player1)
                    player2?.let { physics.respawnPlayer(it) }
                    isPaused = false
                },
                onExit = onExitToMenu
            )
        }

        // Victory Dialog
        if (isVictory) {
            VictoryDialog(
                chapterTitle = level.title,
                score = currentScore + 1000,
                eggsCollected = player1.eggsCollected,
                seconds = gameTimeSeconds,
                unlockRock = level.unlockRock,
                unlockBeach = level.unlockBeach,
                onContinue = onExitToMenu
            )
        }
    }
}

// ---------------- Canvas Drawing Helpers ----------------

private fun DrawScope.drawParallaxScenery(camX: Float, width: Float, height: Float, world: WorldTheme, isColorRestored: Boolean) {
    // Parallax background clouds / hills
    val cloudColor = if (isColorRestored) Color.White.copy(alpha = 0.55f) else Color(0x66B0BEC5)
    val hillColor = if (isColorRestored) Color(world.bgElementsColor).copy(alpha = 0.35f) else Color(0x3378909C)

    // Floating Clouds
    for (i in 0 until 6) {
        val cx = ((i * 320f - camX * 0.25f) % (width + 300f)) - 100f
        val cy = 60f + (i % 3) * 45f
        drawCircle(color = cloudColor, radius = 35f, center = Offset(cx, cy))
        drawCircle(color = cloudColor, radius = 45f, center = Offset(cx + 35f, cy - 8f))
        drawCircle(color = cloudColor, radius = 32f, center = Offset(cx + 70f, cy))
    }

    // Distant Hills / Beanstalks
    for (i in 0 until 5) {
        val hx = ((i * 420f - camX * 0.45f) % (width + 400f)) - 150f
        drawCircle(color = hillColor, radius = 160f, center = Offset(hx, height + 60f))
    }
}

private fun DrawScope.drawTerrain(
    segments: List<TerrainSegment>,
    camX: Float,
    camY: Float,
    world: WorldTheme,
    isColorRestored: Boolean
) {
    val groundColor = if (isColorRestored) Color(world.groundColor) else Color(0xFF455A64)
    val grassTopColor = if (isColorRestored) Color(world.groundAccent) else Color(0xFF37474F)

    for (seg in segments) {
        val p1 = Offset(seg.start.x - camX, seg.start.y - camY)
        val p2 = Offset(seg.end.x - camX, seg.end.y - camY)

        // Earth strata beneath
        val path = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p2.x, p2.y + 120f)
            lineTo(p1.x, p1.y + 120f)
            close()
        }
        drawPath(path, groundColor)

        // Grassy / rocky top rim
        drawLine(
            color = grassTopColor,
            start = p1,
            end = p2,
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawBouncyFlower(flower: BouncyFlower, camX: Float, camY: Float, isColorRestored: Boolean) {
    val fx = flower.x - camX
    val fy = flower.y - camY
    val squish = flower.springSquish

    // Stem
    drawLine(
        color = if (isColorRestored) Color(0xFF388E3C) else Color(0xFF546E7A),
        start = Offset(fx, fy),
        end = Offset(fx, fy - 28f * squish),
        strokeWidth = 6f
    )

    // Petals (spring flower)
    val petalColor = if (isColorRestored) Color(0xFFFF4081) else Color(0xFF90A4AE)
    val centerColor = if (isColorRestored) Color(0xFFFFEB3B) else Color(0xFFB0BEC5)

    for (ang in 0 until 360 step 60) {
        val rad = ang * PI / 180.0
        val px = fx + (cos(rad) * 14f).toFloat()
        val py = fy - 28f * squish + (sin(rad) * 10f * squish).toFloat()
        drawCircle(color = petalColor, radius = 7f, center = Offset(px, py))
    }
    // Flower center
    drawCircle(color = centerColor, radius = 8f, center = Offset(fx, fy - 28f * squish))
}

private fun DrawScope.drawBouncyMushroom(shroom: BouncyMushroom, camX: Float, camY: Float, isColorRestored: Boolean) {
    val sx = shroom.x - camX
    val sy = shroom.y - camY
    val capRad = shroom.capRadius

    // Stem
    drawRoundRect(
        color = Color(0xFFECEFF1),
        topLeft = Offset(sx - 10f, sy - 28f),
        size = Size(20f, 28f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Cap
    val capColor = if (shroom.isPurple) {
        if (isColorRestored) Color(0xFF8E24AA) else Color(0xFF546E7A)
    } else {
        if (isColorRestored) Color(0xFFFBC02D) else Color(0xFF78909C)
    }

    drawArc(
        color = capColor,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(sx - capRad, sy - 28f - capRad),
        size = Size(capRad * 2f, capRad * 2f)
    )

    // White polka dots on cap
    drawCircle(Color.White, radius = 4f, center = Offset(sx - 10f, sy - 34f))
    drawCircle(Color.White, radius = 5f, center = Offset(sx + 8f, sy - 38f))
}

private fun DrawScope.drawBreakableRock(rock: BreakableRock, camX: Float, camY: Float) {
    val rx = rock.x - camX
    val ry = rock.y - camY

    drawRoundRect(
        color = Color(0xFF546E7A),
        topLeft = Offset(rx, ry),
        size = Size(rock.width, rock.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
    )
    // Cracks
    drawLine(
        color = Color(0xFF263238),
        start = Offset(rx + 8f, ry + 10f),
        end = Offset(rx + rock.width / 2f, ry + rock.height / 2f),
        strokeWidth = 2.5f
    )
    drawLine(
        color = Color(0xFF263238),
        start = Offset(rx + rock.width / 2f, ry + rock.height / 2f),
        end = Offset(rx + rock.width - 6f, ry + rock.height - 12f),
        strokeWidth = 2.5f
    )
}

private fun DrawScope.drawSpikes(spike: SpikeHazard, camX: Float, camY: Float) {
    val sx = spike.x - camX
    val sy = spike.y - camY
    val numSpikes = 3
    val spikeW = spike.width / numSpikes

    for (i in 0 until numSpikes) {
        val path = Path().apply {
            moveTo(sx + i * spikeW, sy + spike.height)
            lineTo(sx + i * spikeW + spikeW / 2f, sy)
            lineTo(sx + (i + 1) * spikeW, sy + spike.height)
            close()
        }
        drawPath(path, Color(0xFFB0BEC5))
        drawPath(path, Color(0xFF455A64), style = Stroke(width = 1.5f))
    }
}

private fun DrawScope.drawCannon(cannon: CannonLauncher, camX: Float, camY: Float) {
    val cx = cannon.x - camX
    val cy = cannon.y - camY
    val rad = cannon.angleDegrees * PI / 180.0

    // Base mount
    drawCircle(Color(0xFF37474F), radius = 16f, center = Offset(cx, cy))

    // Barrel
    val barrelLen = 32f
    val endX = cx + (cos(rad) * barrelLen).toFloat()
    val endY = cy + (sin(rad) * barrelLen).toFloat()

    drawLine(
        color = Color(0xFF78909C),
        start = Offset(cx, cy),
        end = Offset(endX, endY),
        strokeWidth = 14f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawColorMachine(machine: ColorMachine, camX: Float, camY: Float) {
    val mx = machine.x - camX
    val my = machine.y - camY

    if (machine.isDestroyed) {
        // Destroyed broken wreckage
        drawRoundRect(
            color = Color(0xFF424242),
            topLeft = Offset(mx, my + 25f),
            size = Size(machine.width, machine.height - 25f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
    } else {
        // Evil Color Drain Machine
        // Chimney
        drawRect(
            color = Color(0xFF212121),
            topLeft = Offset(mx + machine.width * 0.7f, my - 20f),
            size = Size(14f, 20f)
        )
        // Machine body
        drawRoundRect(
            color = Color(0xFF37474F),
            topLeft = Offset(mx, my),
            size = Size(machine.width, machine.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        // Stomp button / dome on top
        drawArc(
            color = Color(0xFFFF5252),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(mx + 8f, my - 12f),
            size = Size(24f, 24f)
        )
        // Hypnotic spiral icon on side
        drawCircle(
            color = Color(0xFFCE93D8),
            radius = 12f,
            center = Offset(mx + machine.width / 2f, my + machine.height / 2f)
        )
        drawCircle(
            color = Color(0xFF4A148C),
            radius = 6f,
            center = Offset(mx + machine.width / 2f, my + machine.height / 2f)
        )
    }
}

private fun DrawScope.drawCheckpoint(cp: CheckpointFlag, camX: Float, camY: Float) {
    val cx = cp.x - camX
    val cy = cp.y - camY

    // Flag pole
    drawLine(
        color = Color(0xFF8D6E63),
        start = Offset(cx, cy),
        end = Offset(cx, cy - 40f),
        strokeWidth = 3f
    )
    // Flag cloth
    val flagColor = if (cp.isReached) Color(0xFF4CAF50) else Color(0xFFE53935)
    val path = Path().apply {
        moveTo(cx, cy - 40f)
        lineTo(cx + 20f, cy - 30f)
        lineTo(cx, cy - 20f)
        close()
    }
    drawPath(path, flagColor)
}

private fun DrawScope.drawGoldenEgg(egg: GoldenEgg, camX: Float, camY: Float) {
    val ex = egg.x - camX
    val ey = egg.y - camY

    // Glow
    drawCircle(
        color = Color(0x55FFE082),
        radius = 16f,
        center = Offset(ex, ey)
    )
    // Egg body (oval)
    drawOval(
        color = Color(0xFFFFB300),
        topLeft = Offset(ex - 8f, ey - 11f),
        size = Size(16f, 22f)
    )
    // Specular highlight
    drawOval(
        color = Color(0xFFFFF9C4),
        topLeft = Offset(ex - 5f, ey - 8f),
        size = Size(6f, 8f)
    )
}

private fun DrawScope.drawPumpkinHouse(goal: PumpkinGoal, camX: Float, camY: Float) {
    val gx = goal.x - camX
    val gy = goal.y - camY
    val gw = goal.width
    val gh = goal.height

    // Pumpkin body
    drawOval(
        color = Color(0xFFFF6F00),
        topLeft = Offset(gx, gy),
        size = Size(gw, gh)
    )
    // Pumpkin green stem
    drawRoundRect(
        color = Color(0xFF2E7D32),
        topLeft = Offset(gx + gw / 2f - 6f, gy - 14f),
        size = Size(12f, 16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
    )
    // Wooden door
    drawArc(
        color = Color(0xFF4E342E),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(gx + gw / 2f - 16f, gy + gh - 40f),
        size = Size(32f, 40f)
    )
    // Round door handle
    drawCircle(
        color = Color(0xFFFFD54F),
        radius = 3.5f,
        center = Offset(gx + gw / 2f + 8f, gy + gh - 18f)
    )
}

private fun DrawScope.drawBallCharacter(
    player: PlayerBallState,
    camX: Float,
    camY: Float,
    isPlayer2: Boolean
) {
    val bx = player.x - camX
    val by = player.y - camY
    val form = player.form
    val baseRad = form.radius
    val radX = baseRad * player.squashX
    val radY = baseRad * player.squashY

    val bodyColor = Color(form.colorHex)
    val outlineColor = Color(form.outlineHex)

    // Ball Body
    drawOval(
        color = bodyColor,
        topLeft = Offset(bx - radX, by - radY),
        size = Size(radX * 2f, radY * 2f)
    )
    drawOval(
        color = outlineColor,
        topLeft = Offset(bx - radX, by - radY),
        size = Size(radX * 2f, radY * 2f),
        style = Stroke(width = 2.5f)
    )

    // Form specific patterns
    when (form) {
        BallForm.RED -> {
            // Cute animated eyes
            val eyeLookX = (player.vx / form.maxSpeed).coerceIn(-1f, 1f) * 4f
            val eyeOffsetY = -radY * 0.25f

            // Left Eye
            drawOval(Color.White, topLeft = Offset(bx - 9f + eyeLookX, by + eyeOffsetY - 6f), size = Size(8f, 12f))
            drawCircle(Color.Black, radius = 3.5f, center = Offset(bx - 5f + eyeLookX, by + eyeOffsetY))
            drawCircle(Color.White, radius = 1.2f, center = Offset(bx - 6f + eyeLookX, by + eyeOffsetY - 1.5f))

            // Right Eye
            drawOval(Color.White, topLeft = Offset(bx + 1f + eyeLookX, by + eyeOffsetY - 6f), size = Size(8f, 12f))
            drawCircle(Color.Black, radius = 3.5f, center = Offset(bx + 5f + eyeLookX, by + eyeOffsetY))
            drawCircle(Color.White, radius = 1.2f, center = Offset(bx + 4f + eyeLookX, by + eyeOffsetY - 1.5f))

            // Smiling Mouth
            drawArc(
                color = Color.Black,
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(bx - 5f, by + 2f),
                size = Size(10f, 6f),
                style = Stroke(width = 1.8f)
            )
        }
        BallForm.ROCK -> {
            // Bumpy rocky facets and cracks
            drawLine(Color(0xFF37474F), Offset(bx - radX * 0.5f, by - radY * 0.3f), Offset(bx + radX * 0.2f, by + radY * 0.5f), strokeWidth = 2f)
            drawLine(Color(0xFF37474F), Offset(bx + radX * 0.2f, by - radY * 0.6f), Offset(bx + radX * 0.6f, by - radY * 0.1f), strokeWidth = 2f)

            // Determined eyes
            drawCircle(Color.White, radius = 4f, center = Offset(bx - 6f, by - 4f))
            drawCircle(Color.Black, radius = 2f, center = Offset(bx - 5f, by - 4f))
            drawCircle(Color.White, radius = 4f, center = Offset(bx + 6f, by - 4f))
            drawCircle(Color.Black, radius = 2f, center = Offset(bx + 7f, by - 4f))
        }
        BallForm.BEACH -> {
            // Beach ball stripes: Red, Yellow, White sections
            drawArc(Color(0xFFE53935), 0f, 60f, true, topLeft = Offset(bx - radX, by - radY), size = Size(radX * 2f, radY * 2f))
            drawArc(Color(0xFFFFFFFF), 120f, 60f, true, topLeft = Offset(bx - radX, by - radY), size = Size(radX * 2f, radY * 2f))
            drawArc(Color(0xFF1E88E5), 240f, 60f, true, topLeft = Offset(bx - radX, by - radY), size = Size(radX * 2f, radY * 2f))

            // Big joyful eyes
            drawCircle(Color.Black, radius = 3f, center = Offset(bx - 5f, by - 3f))
            drawCircle(Color.Black, radius = 3f, center = Offset(bx + 5f, by - 3f))
        }
    }

    // P2 badge over head if multiplayer
    if (isPlayer2) {
        drawCircle(Color(0xFF29B6F6), radius = 5f, center = Offset(bx, by - radY - 8f))
    }
}

// ---------------- HUD & Dialog Composables ----------------

@Composable
private fun TopGameHud(
    chapterTitle: String,
    worldName: String,
    eggsCollected: Int,
    totalEggs: Int,
    score: Int,
    seconds: Int,
    multiplayerMode: MultiplayerMode
) {
    val mins = seconds / 60
    val secs = seconds % 60
    val timeFormatted = String.format("%d:%02d", mins, secs)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left: Chapter name & score
        Column {
            Text(
                text = chapterTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Score: $score",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFFD54F)
            )
            if (multiplayerMode != MultiplayerMode.SOLO) {
                Text(
                    text = multiplayerMode.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF80D8FF)
                )
            }
        }

        // Center / Right: Egg Count & Timer
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Egg count badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x88000000))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Egg,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "$eggsCollected/$totalEggs",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Timer badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x88000000))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = timeFormatted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun MoleDialogBubble(text: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF9E6),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF8D6E63)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mole avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF795548)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Mole", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mole says:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                    Text(
                        text = text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3E2723)
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF8D6E63))
                }
            }
        }
    }
}

@Composable
private fun PauseMenuDialog(onResume: () -> Unit, onRestart: () -> Unit, onExit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onResume,
        title = { Text("Game Paused", fontWeight = FontWeight.Bold) },
        text = { Text("Take a breath, Bounce! What would you like to do?") },
        confirmButton = {
            Button(onClick = onResume, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                Text("Resume")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRestart) { Text("Restart") }
                TextButton(onClick = onExit) { Text("Menu") }
            }
        }
    )
}

@Composable
private fun VictoryDialog(
    chapterTitle: String,
    score: Int,
    eggsCollected: Int,
    seconds: Int,
    unlockRock: Boolean,
    unlockBeach: Boolean,
    onContinue: () -> Unit
) {
    val mins = seconds / 60
    val secs = seconds % 60
    val timeStr = String.format("%d:%02d", mins, secs)

    AlertDialog(
        onDismissRequest = onContinue,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Chapter Completed!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD84315),
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = chapterTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5D4037)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Trophy icon
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(54.dp)
                )

                Text("Score: $score", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Text("Eggs Collected: $eggsCollected / 30", fontSize = 14.sp, color = Color(0xFF37474F))
                Text("Time: $timeStr", fontSize = 14.sp, color = Color(0xFF37474F))

                if (unlockRock) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1)),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "🎉 Unlocked: Bumpy (Rock Ball Form)!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (unlockBeach) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "🎉 Unlocked: Wolly (Beach Ball Form)!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57F17),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CONTINUE TO STORYBOOK", fontWeight = FontWeight.Bold)
            }
        }
    )
}
