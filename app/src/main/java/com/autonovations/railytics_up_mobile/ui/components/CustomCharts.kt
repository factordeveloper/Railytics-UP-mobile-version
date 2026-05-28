package com.autonovations.railytics_up_mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autonovations.railytics_up_mobile.data.models.PerformanceMetric
import java.util.Locale

@Composable
fun SpeedGauge(
    fps: Double,
    modifier: Modifier = Modifier
) {
    val speedValue = fps.coerceIn(0.0, 100.0)
    val color = when {
        speedValue > 80 -> Color(0xFF22C55E)
        speedValue > 50 -> Color(0xFFFFB000)
        else -> Color(0xFFEF4444)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h - 20f)
            val radius = minOf(w / 2, h - 20f) - 10f

            // Draw Background Arc
            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 30f, cap = StrokeCap.Round)
            )

            // Draw Active Arc
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = (speedValue / 100.0 * 180.0).toFloat(),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 30f, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 20.dp)
        ) {
            Text(
                text = String.format(Locale.US, "%.1f", fps),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "FPS",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PerformanceLineChart(
    metrics: List<PerformanceMetric>,
    modifier: Modifier = Modifier
) {
    if (metrics.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No metrics data available", color = Color.Gray)
        }
        return
    }

    Canvas(modifier = modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
        val w = size.width
        val h = size.height

        val maxProcessed = metrics.maxOfOrNull { it.processed } ?: 1
        val maxDetected = metrics.maxOfOrNull { it.detected } ?: 1
        val maxDiscarded = metrics.maxOfOrNull { it.discarded } ?: 1
        val maxValue = maxOf(maxProcessed, maxDetected, maxDiscarded, 10).toFloat() * 1.1f

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = h - (h / gridLines) * i
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 2f
            )
        }

        val stepX = w / (metrics.size - 1).coerceAtLeast(1)

        val processedPath = Path()
        val detectedPath = Path()
        val discardedPath = Path()

        metrics.forEachIndexed { index, m ->
            val x = index * stepX
            val yProc = h - (m.processed / maxValue) * h
            val yDet = h - (m.detected / maxValue) * h
            val yDisc = h - (m.discarded / maxValue) * h

            if (index == 0) {
                processedPath.moveTo(x, yProc)
                detectedPath.moveTo(x, yDet)
                discardedPath.moveTo(x, yDisc)
            } else {
                processedPath.lineTo(x, yProc)
                detectedPath.lineTo(x, yDet)
                discardedPath.lineTo(x, yDisc)
            }
        }

        // Draw paths
        drawPath(
            path = processedPath,
            color = Color(0xFFFFB000), // Gold
            style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = detectedPath,
            color = Color(0xFF22C55E), // Green
            style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = discardedPath,
            color = Color(0xFFEF4444), // Red
            style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw dots at the last point
        if (metrics.isNotEmpty()) {
            val lastIdx = metrics.size - 1
            val lastX = lastIdx * stepX
            val m = metrics.last()

            drawCircle(
                color = Color(0xFFFFB000),
                radius = 12f,
                center = Offset(lastX, h - (m.processed / maxValue) * h)
            )
            drawCircle(
                color = Color(0xFF22C55E),
                radius = 12f,
                center = Offset(lastX, h - (m.detected / maxValue) * h)
            )
            drawCircle(
                color = Color(0xFFEF4444),
                radius = 12f,
                center = Offset(lastX, h - (m.discarded / maxValue) * h)
            )
        }
    }
}

@Composable
fun BarChartComparison(
    labels: List<String>,
    processedValues: List<Int>,
    detectedValues: List<Int>,
    modifier: Modifier = Modifier
) {
    if (labels.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No stream data for comparison", color = Color.Gray)
        }
        return
    }

    Canvas(modifier = modifier.padding(16.dp)) {
        val w = size.width
        val h = size.height

        val maxVal = maxOf(
            processedValues.maxOrNull() ?: 10,
            detectedValues.maxOrNull() ?: 10
        ).toFloat() * 1.1f

        val groupCount = labels.size
        val groupWidth = w / groupCount
        val barWidth = (groupWidth * 0.35f)

        for (i in 0 until groupCount) {
            val groupStartX = i * groupWidth

            // Processed Bar
            val procHeight = (processedValues.getOrElse(i) { 0 } / maxVal) * (h - 20f)
            drawRect(
                color = Color(0xFFFFB000).copy(alpha = 0.8f),
                topLeft = Offset(groupStartX + groupWidth * 0.12f, h - 20f - procHeight),
                size = Size(barWidth, procHeight)
            )

            // Detected Bar
            val detHeight = (detectedValues.getOrElse(i) { 0 } / maxVal) * (h - 20f)
            drawRect(
                color = Color(0xFF22C55E).copy(alpha = 0.8f),
                topLeft = Offset(groupStartX + groupWidth * 0.12f + barWidth + groupWidth * 0.06f, h - 20f - detHeight),
                size = Size(barWidth, detHeight)
            )
        }

        // Base line
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(0f, h - 20f),
            end = Offset(w, h - 20f),
            strokeWidth = 3f
        )
    }
}

@Composable
fun DoughnutChart(
    values: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No active session metrics", color = Color.Gray)
        }
        return
    }

    val sum = values.sum()

    Canvas(modifier = modifier.padding(16.dp)) {
        val w = size.width
        val h = size.height
        val sizeMin = minOf(w, h)
        val rectSize = Size(sizeMin * 0.8f, sizeMin * 0.8f)
        val topLeft = Offset((w - rectSize.width) / 2, (h - rectSize.height) / 2)

        var startAngle = 0f
        values.forEachIndexed { i, value ->
            val sweepAngle = if (sum > 0) (value / sum) * 360f else 0f
            drawArc(
                color = colors.getOrElse(i) { Color.Gray },
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = rectSize,
                style = Stroke(width = 45f)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun ActivityHeatmapGrid(
    activity: List<Int>,
    currentHour: Int,
    modifier: Modifier = Modifier
) {
    val maxVal = activity.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0h", color = Color.Gray, fontSize = 10.sp)
            Text("6h", color = Color.Gray, fontSize = 10.sp)
            Text("12h", color = Color.Gray, fontSize = 10.sp)
            Text("18h", color = Color.Gray, fontSize = 10.sp)
            Text("23h", color = Color.Gray, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            activity.forEachIndexed { hour, count ->
                val intensity = count.toFloat() / maxVal
                val color = when {
                    count == 0 -> Color(0xFF1E1E1E)
                    intensity > 0.7f -> Color(0xFF22C55E)
                    intensity > 0.4f -> Color(0xFFFFB000)
                    else -> Color(0xFFFF9800)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(
                            color = color,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .border(
                            width = if (hour == currentHour) 1.dp else 0.dp,
                            color = if (hour == currentHour) Color(0xFFFFC107) else Color.Transparent,
                            shape = RoundedCornerShape(2.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (count > 0) {
                        Text(
                            text = count.toString(),
                            color = if (count > 0 && intensity < 0.1f) Color.White else Color.Black,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
