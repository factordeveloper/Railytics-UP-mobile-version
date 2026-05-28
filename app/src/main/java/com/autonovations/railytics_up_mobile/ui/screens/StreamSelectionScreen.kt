package com.autonovations.railytics_up_mobile.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.autonovations.railytics_up_mobile.data.models.Detection
import com.autonovations.railytics_up_mobile.data.models.Frame
import com.autonovations.railytics_up_mobile.data.models.Stream
import com.autonovations.railytics_up_mobile.ui.components.TrainFrameCanvas
import kotlin.math.abs

@Composable
fun StreamSelectionScreen(
    streams: List<String>, // We can take direct names or stream objects
    streamObjects: List<Stream>,
    activeStreamIds: List<String>,
    onStartAnalysis: (String) -> Unit,
    onStopAnalysis: (String) -> Unit,
    onStartAll: () -> Unit,
    onStopAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStreamForVideo by remember { mutableStateOf<Stream?>(null) }

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
            Text(
                text = "Stream Selection",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onStartAll,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB000), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Start All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = onStopAll,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Stop All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active sessions banner summary
        if (activeStreamIds.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2196F3).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFF2196F3).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "🔄 ${activeStreamIds.size} Analysis Session(s) in Progress",
                    color = Color(0xFF90CAF9),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stream list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(streamObjects) { stream ->
                StreamCard(
                    stream = stream,
                    isAnalyzing = activeStreamIds.contains(stream.id),
                    onStartAnalysis = { onStartAnalysis(stream.id) },
                    onStopAnalysis = { onStopAnalysis(stream.id) },
                    onWatchStream = { selectedStreamForVideo = stream }
                )
            }
        }
    }

    // Video Player Dialog overlay
    selectedStreamForVideo?.let { stream ->
        VideoPlayerDialog(
            stream = stream,
            onDismiss = { selectedStreamForVideo = null }
        )
    }
}

@Composable
fun StreamCard(
    stream: Stream,
    isAnalyzing: Boolean,
    onStartAnalysis: () -> Unit,
    onStopAnalysis: () -> Unit,
    onWatchStream: () -> Unit
) {
    val hash = abs(stream.filenameHashCode())
    // Let's create a simulated frame to render on Canvas as a thumbnail
    val mockFrame = remember(stream.id) {
        Frame(
            filename = "frame_${stream.name.lowercase().split(",")[0].replace(" ", "")}_001.jpg",
            filepath = "",
            timestamp = "",
            size = 0,
            hasTrains = true,
            detectionCount = 1,
            totalSerials = 1,
            streamId = stream.id,
            detections = listOf(
                Detection(
                    clazz = 0,
                    confidence = 0.92,
                    bbox = listOf(150, 120, 1100, 580),
                    className = if (hash % 2 == 0) "Locomotive" else "Railcar",
                    serials = emptyList(),
                    serialCount = 0
                )
            )
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Column {
            // Simulated live video frame thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clickable { onWatchStream() }
            ) {
                TrainFrameCanvas(frame = mockFrame, modifier = Modifier.fillMaxSize())

                // LIVE badge overlay
                if (stream.youtubeMetadata?.isLive == true) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .background(Color(0xFFEF4444), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🔴 LIVE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stream.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Channel: ${stream.youtubeMetadata?.uploader ?: "Virtual Railfan"}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF22C55E).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Active", color = Color(0xFF22C55E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        if (isAnalyzing) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFB000).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFFFB000).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Analyzing", color = Color(0xFFFFB000), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stream.youtubeMetadata?.title ?: "No title description",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onWatchStream,
                        border = BorderStroke(1.dp, Color(0xFFFFC107)),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFC107))
                    ) {
                        Text("Watch Stream", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    if (isAnalyzing) {
                        Button(
                            onClick = onStopAnalysis,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Stop Analysis", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = onStartAnalysis,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB000), contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Start Analysis", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerDialog(
    stream: Stream,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .padding(16.dp)
                .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                // Header of dialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stream.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, tint = Color.White, contentDescription = "Close")
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.3f))

                // YouTube WebView embed
                val videoId = getYouTubeVideoId(stream.url)
                if (videoId != null) {
                    val embedUrl = "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&mute=0&controls=1"

                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                webChromeClient = WebChromeClient()
                                webViewClient = WebViewClient()
                                loadUrl(embedUrl)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(48.dp),
                                contentDescription = TODO()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Invalid YouTube Video URL", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun Stream.filenameHashCode(): Int {
    return name.hashCode()
}

private fun getYouTubeVideoId(url: String): String? {
    val pattern = "^.*(youtu.be/|v/|u/\\w/|embed/|watch\\?v=|\\&v=)([^#\\&\\?]*).*".toRegex()
    val matchResult = pattern.find(url)
    val id = matchResult?.groupValues?.get(2)
    return if (id?.length == 11) id else null
}
