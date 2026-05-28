package com.autonovations.railytics_up_mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autonovations.railytics_up_mobile.data.models.Stream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isOfflineMode: Boolean,
    onToggleOfflineMode: (Boolean) -> Unit,
    apiUrl: String,
    onUpdateApiUrl: (String) -> Unit,
    streams: List<Stream>,
    onAddStream: (name: String, url: String, desc: String, active: Boolean) -> Unit,
    onToggleStreamActive: (id: String, active: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember { mutableStateOf(apiUrl) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A05))
            .verticalScroll(scrollState)
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

        // 3. Manage Video Streams Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎥 Manage Video Streams",
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    var showAddDialog by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB000),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+ Add Stream", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    if (showAddDialog) {
                        AddStreamDialog(
                            onDismiss = { showAddDialog = false },
                            onConfirm = { name, url, desc, active ->
                                onAddStream(name, url, desc, active)
                                showAddDialog = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List streams inside Settings
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    streams.forEach { stream ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stream.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stream.url,
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Switch(
                                checked = stream.active,
                                onCheckedChange = { onToggleStreamActive(stream.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Color(0xFFFFC107),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. About Section
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStreamDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String, desc: String, active: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }
    
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add New Video Stream", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Provide a YouTube live camera link (e.g. https://www.youtube.com/watch?v=...) to track real-time trains.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Stream Name / Location") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFFFC107)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("YouTube URL") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFFFC107)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFFFC107)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set Active by Default", color = Color.White, fontSize = 13.sp)
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFFFFC107),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                errorText?.let {
                    Text(it, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorText = "Please enter a stream name."
                    } else if (url.isBlank()) {
                        errorText = "Please enter a YouTube video URL."
                    } else {
                        val pattern = "^.*(youtu.be/|v/|u/\\w/|embed/|watch\\?v=|\\&v=)([^#\\&\\?]*).*".toRegex()
                        val matchResult = pattern.find(url)
                        val id = matchResult?.groupValues?.get(2)
                        if (id?.length != 11) {
                            errorText = "Please enter a valid YouTube video URL containing an 11-char video ID."
                        } else {
                            onConfirm(name, url, desc, active)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB000), contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color(0xFF1E1E1E)
    )
}
