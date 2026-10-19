package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.BallForm

@Composable
fun ControlsOverlay(
    isRetroKeypadMode: Boolean,
    onToggleKeypadMode: () -> Unit,
    onMoveP1: (Float) -> Unit,
    onJumpP1: (Boolean) -> Unit,
    onSwitchFormP1: (BallForm) -> Unit,
    currentFormP1: BallForm,
    unlockedRock: Boolean,
    unlockedBeach: Boolean,
    isCoOpLocal: Boolean = false,
    onMoveP2: (Float) -> Unit = {},
    onJumpP2: (Boolean) -> Unit = {},
    onPause: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Top right controls: Pause & Keypad toggle
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Retro Keypad Toggle
            FilledTonalButton(
                onClick = onToggleKeypadMode,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isRetroKeypadMode) Color(0x99212121) else Color(0x66FFFFFF)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Dialpad,
                    contentDescription = "Toggle Nokia Keypad",
                    modifier = Modifier.size(18.dp),
                    tint = if (isRetroKeypadMode) Color(0xFFFFD54F) else Color.White
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isRetroKeypadMode) "Nokia" else "Touch",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Pause Button
            IconButton(
                onClick = onPause,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White
                )
            }
        }

        if (isRetroKeypadMode) {
            // Authentic Nokia 3310 / N73 translucent keypad overlay
            RetroNokiaKeypad(
                onMove = onMoveP1,
                onJump = onJumpP1,
                onSwitchForm = onSwitchFormP1,
                unlockedRock = unlockedRock,
                unlockedBeach = unlockedBeach
            )
        } else if (isCoOpLocal) {
            // Local 2-Player Split Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Player 1 controls (Bounce - Red)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("P1 (Bounce)", color = Color(0xFFFFCDD2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HoldButton(Icons.AutoMirrored.Filled.ArrowBack, onHoldChange = { onMoveP1(if (it) -1f else 0f) })
                        HoldButton(Icons.AutoMirrored.Filled.ArrowForward, onHoldChange = { onMoveP1(if (it) 1f else 0f) })
                        HoldButton(Icons.Default.KeyboardArrowUp, color = Color(0xCCEF5350), onHoldChange = onJumpP1)
                    }
                }

                // Player 2 controls (Bumpy / Wolly)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("P2 (Partner)", color = Color(0xFFB0BEC5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HoldButton(Icons.AutoMirrored.Filled.ArrowBack, onHoldChange = { onMoveP2(if (it) -1f else 0f) })
                        HoldButton(Icons.AutoMirrored.Filled.ArrowForward, onHoldChange = { onMoveP2(if (it) 1f else 0f) })
                        HoldButton(Icons.Default.KeyboardArrowUp, color = Color(0xCC78909C), onHoldChange = onJumpP2)
                    }
                }
            }
        } else {
            // Modern Virtual Gamepad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter)
            ) {
                // Bottom Left: D-Pad (Left / Right buttons)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 16.dp, start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HoldButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        size = 64,
                        onHoldChange = { onMoveP1(if (it) -1f else 0f) }
                    )
                    HoldButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        size = 64,
                        onHoldChange = { onMoveP1(if (it) 1f else 0f) }
                    )
                }

                // Bottom Right: Jump and Form Switcher
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Ball Form switcher button
                    if (unlockedRock || unlockedBeach) {
                        FormSwitchButton(
                            currentForm = currentFormP1,
                            unlockedRock = unlockedRock,
                            unlockedBeach = unlockedBeach,
                            onSelectForm = onSwitchFormP1
                        )
                    }

                    // Jump Button
                    HoldButton(
                        icon = Icons.Default.ArrowUpward,
                        size = 76,
                        color = Color(0xCCE53935),
                        onHoldChange = onJumpP1
                    )
                }
            }
        }
    }
}

@Composable
private fun HoldButton(
    icon: ImageVector,
    size: Int = 56,
    color: Color = Color(0x99263238),
    onHoldChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, Color(0x66FFFFFF), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHoldChange(true)
                        tryAwaitRelease()
                        onHoldChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((size * 0.55f).dp)
        )
    }
}

@Composable
private fun FormSwitchButton(
    currentForm: BallForm,
    unlockedRock: Boolean,
    unlockedBeach: Boolean,
    onSelectForm: (BallForm) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.BottomCenter) {
        // Current form badge
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Color(currentForm.colorHex).copy(alpha = 0.9f))
                .border(2.5.dp, Color.White, CircleShape)
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentForm.displayName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Text(
                    text = "FORM",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Popup form options
        if (expanded) {
            Column(
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xEE212121))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Red form
                SmallFormIcon(BallForm.RED, isSelected = currentForm == BallForm.RED) {
                    onSelectForm(BallForm.RED)
                    expanded = false
                }
                // Rock form
                if (unlockedRock) {
                    SmallFormIcon(BallForm.ROCK, isSelected = currentForm == BallForm.ROCK) {
                        onSelectForm(BallForm.ROCK)
                        expanded = false
                    }
                }
                // Beach form
                if (unlockedBeach) {
                    SmallFormIcon(BallForm.BEACH, isSelected = currentForm == BallForm.BEACH) {
                        onSelectForm(BallForm.BEACH)
                        expanded = false
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallFormIcon(form: BallForm, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(form.colorHex))
            .border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = form.displayName.take(1),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun RetroNokiaKeypad(
    onMove: (Float) -> Unit,
    onJump: (Boolean) -> Unit,
    onSwitchForm: (BallForm) -> Unit,
    unlockedRock: Boolean,
    unlockedBeach: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Left Numpad (1-9, *, 0, #)
        Column(
            modifier = Modifier.width(150.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeypadKey("1", "Red") { onSwitchForm(BallForm.RED) }
                KeypadKey("2", "Jump", onHold = onJump)
                KeypadKey("3", "Beach") { if (unlockedBeach) onSwitchForm(BallForm.BEACH) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeypadKey("4", "Left", onHold = { onMove(if (it) -1f else 0f) })
                KeypadKey("5", "Jump", onHold = onJump)
                KeypadKey("6", "Right", onHold = { onMove(if (it) 1f else 0f) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeypadKey("7", "Rock") { if (unlockedRock) onSwitchForm(BallForm.ROCK) }
                KeypadKey("8", "Down") {}
                KeypadKey("9", "") {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeypadKey("*", "") {}
                KeypadKey("0", "") {}
                KeypadKey("#", "") {}
            }
        }

        // Right D-Pad and Softkeys (L, R, Arrows, F)
        Column(
            modifier = Modifier.width(150.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeypadKey("L", "Soft") {}
                Spacer(Modifier.width(42.dp))
                KeypadKey("R", "Soft") {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Spacer(Modifier.width(42.dp))
                KeypadKey("↑", "Jump", onHold = onJump)
                Spacer(Modifier.width(42.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeypadKey("←", "Left", onHold = { onMove(if (it) -1f else 0f) })
                KeypadKey("F", "Jump", onHold = onJump)
                KeypadKey("→", "Right", onHold = { onMove(if (it) 1f else 0f) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Spacer(Modifier.width(42.dp))
                KeypadKey("↓", "") {}
                Spacer(Modifier.width(42.dp))
            }
        }
    }
}

@Composable
private fun KeypadKey(
    symbol: String,
    subtext: String,
    onHold: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x33FFFFFF))
            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHold?.invoke(true)
                        tryAwaitRelease()
                        onHold?.invoke(false)
                    },
                    onTap = { onClick?.invoke() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = symbol,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f)
            )
            if (subtext.isNotEmpty()) {
                Text(
                    text = subtext,
                    fontSize = 7.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
