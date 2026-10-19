package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.audio.BounceAudioEngine
import com.example.game.model.LevelDefinition
import com.example.game.model.LevelRepository
import com.example.game.model.WorldTheme

@Composable
fun StoryBookSelectScreen(
    currentChapterIndex: Int,
    onChapterChange: (Int) -> Unit,
    onStartGame: (LevelDefinition) -> Unit,
    onOpenMultiplayer: () -> Unit,
    onToggleSound: () -> Unit,
    isSoundMuted: Boolean,
    bestScores: Map<Int, Int>,
    bestEggs: Map<Int, Int>,
    bestTimes: Map<Int, String>
) {
    val levels = LevelRepository.allLevels
    val level = levels.getOrElse(currentChapterIndex) { levels[0] }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3E2723), Color(0xFF1B0000))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative leather book edge
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .shadow(16.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF5D4037),
                            Color(0xFF8D6E63),
                            Color(0xFFD7CCC8),
                            Color(0xFF8D6E63),
                            Color(0xFF4E342E)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            // Inner book page (parchment style)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF9E6))
                    .border(2.dp, Color(0xFFD7CCC8), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header: World & Sound / Multiplayer buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Sound Toggle",
                            tint = Color(0xFF5D4037)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = level.world.worldName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color(0xFF8D6E63)
                        )
                        Text(
                            text = if (level.isBonus) "Bonus Chapter" else "Chapter ${level.chapterNumber}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF3E2723)
                        )
                    }

                    IconButton(
                        onClick = onOpenMultiplayer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Multiplayer",
                            tint = Color(0xFFE53935)
                        )
                    }
                }

                // Chapter Title
                Text(
                    text = level.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFFBF360C),
                    textAlign = TextAlign.Center
                )

                // Chapter Illustration / World Badge
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(level.world.skyColorBottom),
                                    Color(level.world.skyColorTop)
                                )
                            )
                        )
                        .border(3.dp, Color(0xFF8D6E63), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when (level.world) {
                        WorldTheme.SKY_BEAN_LAND -> {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(68.dp)
                            )
                        }
                        WorldTheme.MUSHROOM_MINES -> {
                            Icon(
                                imageVector = Icons.Default.Terrain,
                                contentDescription = null,
                                tint = Color(0xFF80CBC4),
                                modifier = Modifier.size(68.dp)
                            )
                        }
                        WorldTheme.HYPNOLAND -> {
                            Icon(
                                imageVector = Icons.Default.Attractions,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(68.dp)
                            )
                        }
                    }
                }

                // Statistics Card
                val score = bestScores[level.chapterNumber] ?: 4250
                val eggs = bestEggs[level.chapterNumber] ?: 30
                val time = bestTimes[level.chapterNumber] ?: "0:52"

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5EBE1))
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Score: $score",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Egg,
                                contentDescription = "Eggs",
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "$eggs/30",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF5D4037)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Time",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = time,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF5D4037)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Medal",
                                tint = if (eggs >= 30) Color(0xFFFFD700) else Color(0xFFB0BEC5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Page Navigation (Prev / Next Chapter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentChapterIndex > 0) {
                                onChapterChange(currentChapterIndex - 1)
                            }
                        },
                        enabled = currentChapterIndex > 0
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Chapter",
                            tint = if (currentChapterIndex > 0) Color(0xFF5D4037) else Color(0xFFD7CCC8)
                        )
                    }

                    Text(
                        text = "${currentChapterIndex + 1} / ${levels.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8D6E63)
                    )

                    IconButton(
                        onClick = {
                            if (currentChapterIndex < levels.size - 1) {
                                onChapterChange(currentChapterIndex + 1)
                            }
                        },
                        enabled = currentChapterIndex < levels.size - 1
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Chapter",
                            tint = if (currentChapterIndex < levels.size - 1) Color(0xFF5D4037) else Color(0xFFD7CCC8)
                        )
                    }
                }

                // Big Play / Select Button
                Button(
                    onClick = { onStartGame(level) },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "START ADVENTURE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
