package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.model.BallForm
import com.example.game.multiplayer.MultiplayerMode
import com.example.game.multiplayer.MultiplayerSession

@Composable
fun MultiplayerDialog(
    currentSession: MultiplayerSession,
    onDismiss: () -> Unit,
    onConfirm: (MultiplayerSession) -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentSession.mode) }
    var selectedP2Form by remember { mutableStateOf(currentSession.player2Form) }
    var roomCodeInput by remember { mutableStateOf(currentSession.roomCode) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF263238),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.96f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Multiplayer Mode",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Text(
                    text = "Play Bounce Tales together with friends or an AI partner!",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )

                // Mode Selector options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MultiplayerMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF37474F) else Color(0xFF1E272C))
                                .border(
                                    1.5.dp,
                                    if (isSelected) Color(0xFFFF5252) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMode = mode }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedMode = mode },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF5252))
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = mode.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = mode.description,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB0BEC5)
                                )
                            }
                        }
                    }
                }

                // Partner Form Selector (For Co-op or Race)
                if (selectedMode != MultiplayerMode.SOLO) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Player 2 Character:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BallForm.values().forEach { form ->
                                val isSelected = selectedP2Form == form
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(form.colorHex).copy(alpha = 0.35f) else Color(0xFF1E272C))
                                        .border(
                                            2.dp,
                                            if (isSelected) Color(form.colorHex) else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedP2Form = form }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(form.colorHex))
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = form.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4-Digit Room Code Box (if Online / Room selected)
                if (selectedMode == MultiplayerMode.ROOM_ONLINE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = roomCodeInput,
                            onValueChange = { if (it.length <= 4) roomCodeInput = it },
                            label = { Text("Room Code", color = Color.LightGray) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFF5252)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                roomCodeInput = (1000..9999).random().toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
                        ) {
                            Text("Random", fontSize = 12.sp)
                        }
                    }
                }

                // Confirm / Apply button
                Button(
                    onClick = {
                        val session = currentSession.copy(
                            mode = selectedMode,
                            player2Form = selectedP2Form,
                            roomCode = roomCodeInput
                        )
                        onConfirm(session)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "APPLY SETTINGS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
