package com.autonovations.railytics_up_mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autonovations.railytics_up_mobile.data.models.*
import com.autonovations.railytics_up_mobile.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RealTimeAnalysisScreen(
    viewModel: com.autonovations.railytics_up_mobile.ui.viewmodel.RailyticsViewModel,
    modifier: Modifier = Modifier
) {
    val activeSessions by viewModel.activeSessions.collectAsState()
    val performanceMetrics by viewModel.performanceMetrics.collectAsState()
    val liveAlerts by viewModel.liveAlerts.collectAsState()
    val hourlyActivity by viewModel.hourlyActivity.collectAsState()
    val allEvents by viewModel.events.collectAsState()
    val streams by viewModel.streams.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) }
    val tabs = listOf("Multi-Stream View", "Analytics", "Historical")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A05))
    ) {
        // Sub-Tab row
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color(0xFF121212),
            contentColor = Color(0xFFFFC107),
            divider = { Divider(color = Color.Gray.copy(alpha = 0.2f)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeSubTab == index,
                    onClick = { activeSubTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    selectedContentColor = Color(0xFFFFC107),
                    unselectedContentColor = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        when (activeSubTab) {
            0 -> MultiStreamSubTab(
                activeSessions = activeSessions,
                onStopAnalysis = { viewModel.stopStream(it) }
            )
            1 -> AnalyticsSubTab(
                activeSessions = activeSessions,
                performanceMetrics = performanceMetrics,
                liveAlerts = liveAlerts,
                hourlyActivity = hourlyActivity,
                allEvents = allEvents
            )
            2 -> HistoricalSubTab(
                allEvents = allEvents,
                streams = streams,
                selectedDate = viewModel.selectedCalDate.collectAsState().value,
                onDateSelected = { viewModel.setSelectedCalDate(it) },
                locationFilter = viewModel.calLocationFilter.collectAsState().value,
                onLocationFilterChanged = { viewModel.setCalLocationFilter(it) }
            )
        }
    }
}

// ════════════ SUB-TAB 0: MULTI-STREAM VIEW ════════════
@Composable
fun MultiStreamSubTab(
    activeSessions: List<AnalysisSession>,
    onStopAnalysis: (String) -> Unit
) {
    if (activeSessions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Active Analysis",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start an analysis from the Stream Selection tab to view real-time data.",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "📊 Active Stream Analysis (${activeSessions.size})",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(activeSessions) { session ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = session.streamName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Runtime: ${formatRuntime(session.runtimeSeconds)}",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { onStopAnalysis(session.streamId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Stop", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Frames Processed", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = session.framesProcessed.toString(),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB000),
                                fontSize = 16.sp
                            )
                        }

                        Column {
                            Text("Trains Detected", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = session.trainsDetected.toString(),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22C55E),
                                fontSize = 16.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Detection Rate", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = "${session.detectionRate}%",
                                fontWeight = FontWeight.Bold,
                                color = if (session.detectionRate > 15.0) Color(0xFF22C55E) else Color(0xFFFFB000),
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = (session.detectionRate / 100.0).toFloat().coerceIn(0f, 1f),
                        color = if (session.detectionRate > 15.0) Color(0xFF22C55E) else Color(0xFFFFB000),
                        trackColor = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

// ════════════ SUB-TAB 1: ANALYTICS ════════════
@Composable
fun AnalyticsSubTab(
    activeSessions: List<AnalysisSession>,
    performanceMetrics: List<PerformanceMetric>,
    liveAlerts: List<LiveAlert>,
    hourlyActivity: List<Int>,
    allEvents: List<RailwayEvent>
) {
    val totalProcessed = activeSessions.sumOf { it.framesProcessed }
    val totalDetected = activeSessions.sumOf { it.trainsDetected }
    val rate = if (totalProcessed > 0) String.format(Locale.US, "%.1f", (totalDetected.toDouble() / totalProcessed * 100)).toDouble() else 0.0
    val activeCount = activeSessions.size
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "📊 Real-Time Stream Analytics",
                color = Color(0xFFFFC107),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Live stats grid summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "📍 STREAMS",
                    value = activeCount.toString(),
                    subText = if (activeCount > 0) "↘ Monitoring" else "Inactive",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "🚂 DETECTIONS",
                    value = totalDetected.toString(),
                    subText = "Rate: $rate%",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 1. Live Detection Stream Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFFFB000), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Live Detection Stream", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Text(
                            text = "LIVE TRACKING",
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (activeSessions.isEmpty()) {
                        Text(
                            text = "No active detections - start a stream to track live",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Show top 3 recent events in a simple list
                        allEvents.take(3).forEach { event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(32.dp)
                                        .background(Color(0xFF22C55E))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "TRAIN detected at ${event.streamName.substringBefore(",")}",
                                        color = Color(0xFFFFB000),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Marks: ${event.reportingMarks.joinToString(", ")} • Units: ${event.unitsCount}",
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Speed Gauge and System Health
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚡ Processing Speed Gauge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    val currentFps = if (activeSessions.isNotEmpty()) (24.0 + Math.random() * 8.0) else 0.0
                    SpeedGauge(fps = currentFps)
                }
            }
        }

        // 3. Performance Metrics Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📈 Performance metrics over time", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    PerformanceLineChart(
                        metrics = performanceMetrics,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        // 4. Comparison Bar Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 Stream Comparison", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    val comparedSessions = activeSessions.take(4)
                    BarChartComparison(
                        labels = comparedSessions.map { it.streamName.substringBefore(",") },
                        processedValues = comparedSessions.map { it.framesProcessed },
                        detectedValues = comparedSessions.map { it.trainsDetected },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        // 5. Activity Heatmap
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔥 Hourly Activity Heatmap (Today)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    ActivityHeatmapGrid(activity = hourlyActivity, currentHour = currentHour)
                }
            }
        }

        // 6. Live Alerts Ticker
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔔 Live Alerts & Events Logs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (liveAlerts.isEmpty()) {
                        Text("No recent alerts triggered.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        liveAlerts.take(4).forEach { alert ->
                            val color = when (alert.type) {
                                "success" -> Color(0xFF22C55E)
                                "warning" -> Color(0xFFFF9800)
                                else -> Color(0xFF2196F3)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(color.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(alert.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(alert.desc, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    subText: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        modifier = modifier
            .height(110.dp)
            .border(2.dp, Color(0xFFFFB000), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(subText, color = Color(0xFF22C55E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ════════════ SUB-TAB 2: HISTORICAL ════════════
@Composable
fun HistoricalSubTab(
    allEvents: List<RailwayEvent>,
    streams: List<Stream>,
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    locationFilter: String,
    onLocationFilterChanged: (String) -> Unit
) {
    val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val selectedDateStr = dateSdf.format(selectedDate)

    // Calculate dates in month that have events to highlight them in calendar
    val eventDays = remember(allEvents) {
        allEvents.map { e ->
            try {
                val eDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(e.startTime)
                dateSdf.format(eDate)
            } catch (err: Exception) {
                ""
            }
        }.filter { it.isNotEmpty() }.toSet()
    }

    // Filter events by selected date and location
    val filteredEvents = remember(allEvents, selectedDateStr, locationFilter) {
        allEvents.filter { e ->
            val eDateStr = try {
                val eDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(e.startTime)
                dateSdf.format(eDate)
            } catch (err: Exception) {
                ""
            }
            val matchesDate = eDateStr == selectedDateStr
            val matchesLoc = locationFilter == "ALL" || e.streamName == locationFilter || e.streamId == locationFilter
            matchesDate && matchesLoc
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("📅 Event Calendar", color = Color(0xFFFFC107), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // Custom calendar component
        item {
            CalendarWidget(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                eventDays = eventDays
            )
        }

        // Location filter and events heading
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Events: ${filteredEvents.size}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                // Simple Dropdown location filter
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(locationFilter.substringBefore(","), color = Color(0xFFFFC107), fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF1E1E1E))
                    ) {
                        DropdownMenuItem(
                            text = { Text("ALL", color = Color.White) },
                            onClick = {
                                onLocationFilterChanged("ALL")
                                expanded = false
                            }
                        )
                        streams.map { it.name }.distinct().forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc.substringBefore(","), color = Color.White) },
                                onClick = {
                                    onLocationFilterChanged(loc)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // List of filtered events
        if (filteredEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No train detections for this date.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredEvents) { event ->
                HistoricEventRow(event = event)
            }
        }
    }
}

@Composable
fun CalendarWidget(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    eventDays: Set<String>
) {
    val calendar = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }

    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    val daysInMonth = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed (Sunday = 0, Monday = 1, etc.)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${monthNames[currentMonth]} $currentYear",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row {
                    IconButton(
                        onClick = {
                            if (currentMonth == 0) {
                                currentMonth = 11
                                currentYear -= 1
                            } else {
                                currentMonth -= 1
                            }
                        }
                    ) {
                        Text("<", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = {
                            if (currentMonth == 11) {
                                currentMonth = 0
                                currentYear += 1
                            } else {
                                currentMonth += 1
                            }
                        }
                    ) {
                        Text(">", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weekdays labels
            Row(modifier = Modifier.fillMaxWidth()) {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                days.forEach { day ->
                    Text(
                        text = day,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNum = cellIndex - firstDayOfWeek + 1

                        if (cellIndex < firstDayOfWeek || dayNum > daysInMonth) {
                            Box(modifier = Modifier.weight(1f))
                        } else {
                            val cellDate = remember(dayNum, currentMonth, currentYear) {
                                val cal = Calendar.getInstance()
                                cal.set(currentYear, currentMonth, dayNum)
                                cal.time
                            }
                            val dateKey = remember(dayNum, currentMonth, currentYear) {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                sdf.format(cellDate)
                            }

                            val isSelected = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate) == dateKey
                            val hasEvent = eventDays.contains(dateKey)
                            val isToday = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) == dateKey

                            val bg = when {
                                isSelected -> Color(0xFF22C55E) // Selected Green
                                isToday -> Color(0xFFFFC107)    // Today Gold
                                hasEvent -> Color(0xFF2196F3).copy(alpha = 0.5f) // Has Events Blue
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .clickable { onDateSelected(cellDate) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    color = if (isSelected || isToday) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoricEventRow(event: RailwayEvent) {
    val timeStr = try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(event.startTime)
        SimpleDateFormat("HH:mm:ss", Locale.US).format(date)
    } catch (e: Exception) {
        event.startTime
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB000),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.streamName.substringBefore(","),
                        color = Color(0xFF90CAF9),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Marks: ${event.reportingMarks.joinToString(", ")}",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFC107).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${event.unitsCount} Units",
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${event.durationSeconds ?: 0}s",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun formatRuntime(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "${h}h ${m}m ${s}s" else if (m > 0) "${m}m ${s}s" else "${s}s"
}
