package com.autonovations.railytics_up_mobile.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    var isWatchingInline by remember { mutableStateOf(false) }

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
            // Simulated live video frame thumbnail OR YoutubePlayer inline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                if (isWatchingInline) {
                    YoutubePlayer(videoUrl = stream.url, modifier = Modifier.fillMaxSize())
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { isWatchingInline = true }
                    ) {
                        TrainFrameCanvas(frame = mockFrame, modifier = Modifier.fillMaxSize())
                    }
                }

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
                        onClick = { isWatchingInline = !isWatchingInline },
                        border = BorderStroke(1.dp, Color(0xFFFFC107)),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFC107))
                    ) {
                        Text(
                            text = if (isWatchingInline) "Stop Watching" else "Watch Stream",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
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
fun YoutubePlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    // Build the canonical watch URL so the WebView loads the full YouTube page
    // (not an embed) — this bypasses Error 150 entirely.
    val watchUrl = remember(videoUrl) {
        val id = extractYouTubeVideoId(videoUrl)
        if (id != null) {
            // Prefer the live URL format for live streams; fallback to watch
            if (videoUrl.contains("/live") || videoUrl.contains("live/")) {
                "https://m.youtube.com/watch?v=$id&autoplay=1"
            } else {
                "https://m.youtube.com/watch?v=$id&autoplay=1"
            }
        } else {
            // Fallback: use the URL as-is if we can't extract an ID
            videoUrl
        }
    }

    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        allowContentAccess = true
                        setSupportMultipleWindows(false)
                        // Mobile Chrome UA — loads the mobile YouTube site which is
                        // lighter and focuses on the video player
                        userAgentString =
                            "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/124.0.6367.82 Mobile Safari/537.36"
                    }
                    webChromeClient = object : WebChromeClient() {}
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Inject CSS to hide YouTube mobile UI chrome:
                            // header, bottom nav, sidebar, ads — showing only the player
                            val js = """
                                (function() {
                                    var style = document.createElement('style');
                                    style.textContent = `
                                        /* Hide top header / nav bar */
                                        #masthead-container,
                                        ytm-mobile-topbar-renderer,
                                        .mobile-topbar-renderer,
                                        header.mobile-topbar-renderer { display: none !important; }

                                        /* Hide bottom navigation bar */
                                        .pivot-bar-renderer,
                                        ytm-pivot-bar-renderer,
                                        [data-is-persistent='true'] { display: none !important; }

                                        /* Remove padding that accounts for hidden bars */
                                        ytm-app { padding-top: 0 !important; }

                                        /* Hide ads / overlays */
                                        .video-ads, .ytp-ad-module,
                                        ytm-promoted-sparkles-web-renderer { display: none !important; }

                                        /* Make body flush black */
                                        body { background: #000 !important; margin: 0; }
                                    `;
                                    document.head.appendChild(style);

                                    /* Scroll page so player is at top */
                                    window.scrollTo(0, 0);
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(js, null)
                            isLoading = false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: ""
                            // Keep all YouTube navigation inside the WebView
                            return !(url.contains("youtube.com") ||
                                    url.contains("youtu.be") ||
                                    url.contains("ytimg.com") ||
                                    url.contains("googlevideo.com") ||
                                    url.contains("googleapis.com") ||
                                    url.startsWith("blob:") ||
                                    url.startsWith("data:"))
                        }
                    }
                    loadUrl(watchUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading overlay
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0A0A)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFB000),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Cargando stream...",
                        color = Color(0xFF90CAF9),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/** Extracts the 11-character video/stream ID from any YouTube URL format. */
fun extractYouTubeVideoId(url: String): String? {
    val pattern = "^.*(youtu\\.be/|v/|u/\\w/|embed/|watch\\?v=|\\&v=|live/)([^#\\&\\?]*).*".toRegex()
    val matchResult = pattern.find(url)
    val id = matchResult?.groupValues?.get(2)
    return if (id?.length == 11) id else null
}



@Composable
fun VideoPlayerDialog(
    stream: Stream,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
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
                .fillMaxHeight(0.65f)
                .padding(12.dp)
                .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = Color(0xFF121212),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                // Dialog header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF1A1A1A),
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stream.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (stream.youtubeMetadata?.isLive == true) {
                            Text(
                                text = "🔴 EN VIVO",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Open in YouTube app button
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(stream.url)).apply {
                                    setPackage("com.google.android.youtube")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(stream.url)))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                tint = Color(0xFFFF0000),
                                contentDescription = "Abrir en YouTube"
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, tint = Color.White, contentDescription = "Cerrar")
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFFFC107).copy(alpha = 0.2f))

                // YouTube player
                YoutubePlayer(
                    videoUrl = stream.url,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            }
        }
    }
}

private fun Stream.filenameHashCode(): Int {
    return name.hashCode()
}
