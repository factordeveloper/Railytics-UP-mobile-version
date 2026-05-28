package com.autonovations.railytics_up_mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autonovations.railytics_up_mobile.data.models.RailwayEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun DashboardScreen(
    events: List<RailwayEvent>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "Railway Events",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            IconButton(
                onClick = onRefresh,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Icon(imageVector = Icons.Default.Refresh, tint = Color(0xFFFFC107), contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total chips counter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1B), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "🚂 Total Tracked Events: ${events.size}",
                color = Color(0xFFFFC107),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "There are no Railway Events available",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(events) { event ->
                    EventDashboardCard(event = event)
                }
            }
        }
    }
}

@Composable
fun EventDashboardCard(event: RailwayEvent) {
    val startStr = formatTimestamp(event.startTime)
    val endStr = if (event.endTime != null) formatTimestamp(event.endTime) else "Ongoing"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.streamName,
                    color = Color(0xFF90CAF9),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(Color(0xFF22C55E).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ID: ${event.id}",
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Time Row
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("START TIME", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(startStr, color = Color.White, fontSize = 13.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("END TIME", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(endStr, color = Color.White, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("DURATION", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${event.durationSeconds ?: 0}s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text("FRAMES", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(event.framesCount.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text("LOCOS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(event.locomotivesCount.toString(), color = Color(0xFFFFB000), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text("WAGONS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(event.wagonsCount.toString(), color = Color(0xFFFFB000), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text("UNITS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(event.unitsCount.toString(), color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reporting marks
            if (event.reportingMarks.isNotEmpty()) {
                Text("REPORTING MARKS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    event.reportingMarks.take(4).forEach { mark ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(mark, color = Color(0xFFFFC107), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (event.reportingMarks.size > 4) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("+${event.reportingMarks.size - 4}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(timestamp) ?: Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        timestamp
    }
}
