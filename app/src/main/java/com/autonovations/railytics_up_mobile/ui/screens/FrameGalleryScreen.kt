package com.autonovations.railytics_up_mobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.autonovations.railytics_up_mobile.data.models.Frame
import com.autonovations.railytics_up_mobile.data.models.Serial
import com.autonovations.railytics_up_mobile.data.models.Stream
import com.autonovations.railytics_up_mobile.ui.components.TrainCropCanvas
import com.autonovations.railytics_up_mobile.ui.components.TrainFrameCanvas
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FrameGalleryScreen(
    frames: List<Frame>,
    totalFrames: Int,
    streams: List<Stream>,
    onRefresh: () -> Unit,
    onPageChanged: (streamId: String?, limit: Int, skip: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLocationFilter by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    val limit = 12

    var zoomedFrame by remember { mutableStateOf<Frame?>(null) }

    val totalPages = remember(totalFrames) {
        val pages = (totalFrames + limit - 1) / limit
        if (pages < 1) 1 else pages
    }

    // Trigger pagination when page or filter changes
    LaunchedEffect(currentPage, selectedLocationFilter) {
        val skip = (currentPage - 1) * limit
        onPageChanged(selectedLocationFilter, limit, skip)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A05))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Captured Frames",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$totalFrames saved frames",
                    fontSize = 12.sp,
                    color = Color(0xFFFFC107),
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = onRefresh,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Icon(imageVector = Icons.Default.Refresh, tint = Color(0xFFFFC107), contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filters bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location dropdown filter
            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = selectedLocationFilter?.let { id ->
                            streams.find { it.id == id }?.name?.substringBefore(",")
                        } ?: "All Locations",
                        color = Color(0xFFFFC107),
                        fontSize = 12.sp
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    DropdownMenuItem(
                        text = { Text("All Locations", color = Color.White) },
                        onClick = {
                            selectedLocationFilter = null
                            currentPage = 1
                            expanded = false
                        }
                    )
                    streams.forEach { stream ->
                        DropdownMenuItem(
                            text = { Text(stream.name.substringBefore(","), color = Color.White) },
                            onClick = {
                                selectedLocationFilter = stream.id
                                currentPage = 1
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of frames
        if (frames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No frames captured matching this filter.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(frames) { frame ->
                    FrameCardItem(
                        frame = frame,
                        streamName = streams.find { it.id == frame.streamId }?.name ?: "Location",
                        onClick = { zoomedFrame = frame }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pagination row controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { if (currentPage > 1) currentPage-- },
                enabled = currentPage > 1,
                border = BorderStroke(1.dp, if (currentPage > 1) Color(0xFFFFC107) else Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFC107))
            ) {
                Text("< Prev")
            }

            Text(
                text = "Page $currentPage of $totalPages",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            OutlinedButton(
                onClick = { if (currentPage < totalPages) currentPage++ },
                enabled = currentPage < totalPages,
                border = BorderStroke(1.dp, if (currentPage < totalPages) Color(0xFFFFC107) else Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFC107))
            ) {
                Text("Next >")
            }
        }
    }

    // Zoom Dialog box
    zoomedFrame?.let { frame ->
        FrameDetailsZoomDialog(
            frame = frame,
            streamName = streams.find { it.id == frame.streamId }?.name ?: "Location",
            onDismiss = { zoomedFrame = null }
        )
    }
}

@Composable
fun FrameCardItem(
    frame: Frame,
    streamName: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Column {
            // Frame canvas thumbnail representation
            TrainFrameCanvas(
                frame = frame,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = streamName.substringBefore(","),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTimestampShort(frame.timestamp),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun FrameDetailsZoomDialog(
    frame: Frame,
    streamName: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(16.dp)
                .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Frame Details",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, tint = Color.White, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High resolution Canvas drawing
                TrainFrameCanvas(
                    frame = frame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Details Text metadata
                Text("Metadata Properties", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Location: $streamName", color = Color.White, fontSize = 13.sp)
                Text("Filename: ${frame.filename}", color = Color.LightGray, fontSize = 13.sp)
                Text("File size: ${(frame.size / 1024)} KB", color = Color.LightGray, fontSize = 13.sp)
                Text("Timestamp: ${frame.timestamp}", color = Color.LightGray, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // OCR/Crops detections list
                Text("Crop Serials Detections", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val serials: List<Serial> = frame.detections.flatMap { it.serials }
                if (serials.isEmpty()) {
                    Text("No serial crops found in this frame.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(serials) { serial ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Draw actual cropped marking mark
                                TrainCropCanvas(
                                    serialText = serial.text,
                                    modifier = Modifier
                                        .size(width = 120.dp, height = 50.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text("Mark: ${serial.text}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Cleaned: ${serial.cleanedText}", color = Color.LightGray, fontSize = 12.sp)
                                    Text("Confidence: ${(serial.confidence * 100).toInt()}%", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestampShort(timestamp: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val date = parser.parse(timestamp) ?: return timestamp
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        timestamp
    }
}
