package com.autonovations.railytics_up_mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isOfflineMode: Boolean,
    onToggleOfflineMode: (Boolean) -> Unit,
    apiUrl: String,
    onUpdateApiUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember { mutableStateOf(apiUrl) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A05))
            .padding(16.dp)
    ) {
        Text(
            text = "App Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Switch Mode Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Data Operations Mode",
                    color = Color(0xFFFFC107),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose whether the app runs a simulated routine using local bundled datasets or fetches from a remote Catalyst server endpoint.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isOfflineMode) "Simulated (Offline)" else "Live Server Connection",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isOfflineMode) "Using assets/streams.json" else "Connecting to endpoint",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = onToggleOfflineMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFFFFC107),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. API Configuration Input
        if (!isOfflineMode) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Zoho Catalyst Endpoint",
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            cursorColor = Color(0xFFFFC107),
                            focusedIndicatorColor = Color(0xFFFFC107),
                            unfocusedIndicatorColor = Color.Gray
                        ),
                        placeholder = { Text("http://10.0.2.2:8080", color = Color.Gray) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onUpdateApiUrl(urlInput) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB000),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save URL", fontWeight = FontWeight.Bold)
                        }

                        // Presets Button
                        Button(
                            onClick = {
                                urlInput = "http://10.0.2.2:8080"
                                onUpdateApiUrl("http://10.0.2.2:8080")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22C55E),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Use Localhost", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 3. About Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Diagnostics Info",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("App Version: 1.0.0-mobile", color = Color.Gray, fontSize = 12.sp)
                Text("UI Engine: Jetpack Compose Material3", color = Color.Gray, fontSize = 12.sp)
                Text("Platform Backend: Zoho Catalyst Express Function (Mocked)", color = Color.Gray, fontSize = 12.sp)
                Text("Detection Engine: YOLO v8 Neural Model", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
