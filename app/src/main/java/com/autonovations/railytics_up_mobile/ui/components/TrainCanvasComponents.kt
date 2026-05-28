package com.autonovations.railytics_up_mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.autonovations.railytics_up_mobile.data.models.Frame
import kotlin.math.abs

@Composable
fun TrainFrameCanvas(
    frame: Frame,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    ) {
        val w = size.width
        val h = size.height

        // 1. Draw Sky Gradient
        val hash = abs(frame.filename.hashCode())
        val colors = listOf(
            Color(0xFF1A472A),
            Color(0xFF2D5016),
            Color(0xFF1A3A5C),
            Color(0xFF4A2D1A),
            Color(0xFF3D1A4A),
            Color(0xFF1A4A4A)
        )
        val bgColor = colors[hash % colors.size]
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF87CEEB).copy(alpha = 0.3f),
                    bgColor,
                    Color(0xFF1A1A1A)
                )
            ),
            size = Size(w, h)
        )

        // 2. Draw Ground and Tracks
        val groundHeight = h * 0.25f
        val groundY = h - groundHeight
        drawRect(
            color = Color(0xFF3A3A3A),
            topLeft = Offset(0f, groundY),
            size = Size(w, groundHeight)
        )
        // Rails
        val rail1Y = groundY + groundHeight * 0.3f
        val rail2Y = groundY + groundHeight * 0.7f
        drawLine(
            color = Color(0xFF8A8A8A),
            start = Offset(0f, rail1Y),
            end = Offset(w, rail1Y),
            strokeWidth = 6f
        )
        drawLine(
            color = Color(0xFF8A8A8A),
            start = Offset(0f, rail2Y),
            end = Offset(w, rail2Y),
            strokeWidth = 6f
        )

        // 3. Draw Train and bounding boxes based on detections
        frame.detections.forEachIndexed { index, detection ->
            // Let's map coordinate boxes from Catalyst's 1280x720 coordinates system
            val rawBox = detection.bbox
            if (rawBox.size == 4) {
                val scaleX = w / 1280f
                val scaleY = h / 720f

                val bLeft = rawBox[0] * scaleX
                val bTop = rawBox[1] * scaleY
                val bWidth = (rawBox[2] - rawBox[0]) * scaleX
                val bHeight = (rawBox[3] - rawBox[1]) * scaleY

                // Draw Train Compartment
                val trainColors = listOf(Color(0xFFFFB000), Color(0xFFE09900), Color(0xFFCC8400), Color(0xFFFFCC33))
                val tc = trainColors[(hash + index) % trainColors.size]

                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(tc, Color(0xFF8B4513).copy(alpha = 0.7f))
                    ),
                    topLeft = Offset(bLeft, bTop),
                    size = Size(bWidth, bHeight),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                // Wheels
                val wheelRadius = minOf(bWidth * 0.08f, 25f)
                val wheelY = bTop + bHeight - 10f
                drawCircle(
                    color = Color(0xFF333333),
                    radius = wheelRadius,
                    center = Offset(bLeft + bWidth * 0.15f, wheelY),
                    style = Stroke(width = 6f)
                )
                drawCircle(
                    color = Color(0xFF333333),
                    radius = wheelRadius,
                    center = Offset(bLeft + bWidth * 0.85f, wheelY),
                    style = Stroke(width = 6f)
                )

                // Dashed bounding box
                drawRoundRect(
                    color = Color(0xFF00FF00),
                    topLeft = Offset(bLeft - 10f, bTop - 10f),
                    size = Size(bWidth + 20f, bHeight + 20f),
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                )

                // Label box
                val labelHeight = 35f
                drawRoundRect(
                    color = Color(0xFF00FF00),
                    topLeft = Offset(bLeft - 10f, bTop - 10f - labelHeight),
                    size = Size(bWidth.coerceAtMost(220f), labelHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Label text
                val labelText = "${detection.className} ${(detection.confidence * 100).toInt()}%"
                val textLayoutResult = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(bLeft, bTop - 10f - labelHeight + 6f)
                )
            }
        }

        // 4. Draw Overlay HUD (Top branding and timestamps)
        // Top Left Timestamp HUD
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.7f),
            topLeft = Offset(15f, 15f),
            size = Size(w * 0.45f, 45f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        val locationText = frame.filename.substringAfter("frame_").substringBefore("_").replaceFirstChar { it.uppercase() }
        val hudText = "RAILYTICS | $locationText | ${frame.filename}"
        val hudLayout = textMeasurer.measure(
            text = hudText,
            style = TextStyle(
                color = Color(0xFF00FF00),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        )
        drawText(hudLayout, topLeft = Offset(25f, 25f))

        // Top Right Brand HUD
        drawRoundRect(
            color = Color(0xFFFFB000).copy(alpha = 0.9f),
            topLeft = Offset(w - 215f, 15f),
            size = Size(200f, 45f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        val brandLayout = textMeasurer.measure(
            text = "RAILYTICS UP",
            style = TextStyle(
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        )
        drawText(brandLayout, topLeft = Offset(w - 180f, 25f))

        // Bottom Left Model HUD
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.7f),
            topLeft = Offset(15f, h - 55f),
            size = Size(280f, 40f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        val modelLayout = textMeasurer.measure(
            text = "YOLO v8 | Conf: 0.92 | Mock",
            style = TextStyle(
                color = Color(0xFFFFC107),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        )
        drawText(modelLayout, topLeft = Offset(25f, h - 45f))
    }
}

@Composable
fun TrainCropCanvas(
    serialText: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Background
        drawRect(color = Color(0xFF1F1F1F), size = Size(w, h))

        // Border
        drawRoundRect(
            color = Color(0xFFFFC107),
            topLeft = Offset(10f, 10f),
            size = Size(w - 20f, h - 20f),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 4f)
        )

        // Serial mark text
        val textLayout = textMeasurer.measure(
            text = serialText,
            style = TextStyle(
                color = Color(0xFFFFC107),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        )
        drawText(
            textLayout,
            topLeft = Offset((w - textLayout.size.width) / 2f, (h - textLayout.size.height) / 2f - 10f)
        )

        // Confidence text
        val confLayout = textMeasurer.measure(
            text = "OCR Confidence: 0.87",
            style = TextStyle(
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        )
        drawText(
            confLayout,
            topLeft = Offset((w - confLayout.size.width) / 2f, h - 35f)
        )
    }
}
