package com.nhathuy.randomlucky.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DroppingBallCompleted(
    numbers: List<String>,
    colors: List<Color>,
    isDropping: Boolean,
    onAllDropsComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentlyDropping by remember { mutableStateOf<List<Boolean>>(emptyList()) }

    LaunchedEffect(isDropping, numbers.size) {
        if (isDropping && numbers.isNotEmpty()) {
            currentlyDropping = List(numbers.size) { false }

            // Tối ưu delay để dropping nhanh và mượt hơn
            numbers.forEachIndexed { index, _ ->
                val delayTime = when {
                    numbers.size == 1 -> 0L                    // 1 số: không delay
                    numbers.size <= 3 -> 200L * index          // 2-3 số: 200ms giữa mỗi quả
                    numbers.size <= 6 -> 150L * index          // 4-6 số: 150ms
                    numbers.size <= 10 -> 120L * index         // 7-10 số: 120ms
                    numbers.size <= 15 -> 100L * index         // 11-15 số: 100ms
                    else -> 80L * index                   // Nhiều số: 150ms
                }

                delay(delayTime)
                currentlyDropping = currentlyDropping.toMutableList().apply {
                    this[index] = true
                }
            }

            // Giảm final delay để chuyển đổi nhanh hơn
            val finalDelay = when {
                numbers.size == 1 -> 500L      // 1 số: 1.2s
                numbers.size <= 3 -> 600L      // 2-3 số: 1.4s
                numbers.size <= 6 -> 700L      // 4-6 số: 1.6s
                else -> 800L                   // Nhiều số: 1.8s
            }
            delay(finalDelay)
            onAllDropsComplete()
        }
    }

    if (isDropping) {
        // Container với layout tối ưu
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp), // Fixed height để animation ổn định
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    when {
                        numbers.size <= 3 -> 10.dp
                        numbers.size <= 6 -> 8.dp
                        numbers.size <= 10 -> 6.dp
                        numbers.size <= 15 -> 4.dp
                        else -> 3.dp
                    }
                ),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 6.dp) // Padding để tránh cắt
            ) {
                numbers.forEachIndexed { index, number ->
                    DroppingBall(
                        number = number,
                        color = colors.getOrElse(index) { colors.first() },
                        isDropping = currentlyDropping.getOrElse(index) { false },
                        dropDelay = index * 20L, // Giảm stagger delay
                        modifier = Modifier.size(
                            when {
                                numbers.size <= 3 -> 52.dp
                                numbers.size <= 6 -> 46.dp
                                numbers.size <= 10 -> 42.dp
                                numbers.size <= 15 -> 48.dp
                                else -> 34.dp
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DroppingBall(
    number: String,
    color: Color,
    isDropping: Boolean,
    dropDelay: Long = 0L,
    onDropComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var startDrop by remember { mutableStateOf(false) }

    LaunchedEffect(isDropping) {
        if (isDropping) {
            delay(dropDelay)
            startDrop = true
        } else {
            startDrop = false
        }
    }

    //Tối ưu animation cho mobile
    val dropY by animateFloatAsState(
        targetValue = if (startDrop) 30f else 0f, // Giảm khoảng cách rơi cho phù hợp mobile
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh, // Giảm stiffness cho smooth hơn
        ),
        finishedListener = {
            if (startDrop) onDropComplete()
        },
        label = "dropY"
    )

    val rotation by animateFloatAsState(
        targetValue = if (startDrop) 360f else 0f, // Giảm rotation
        animationSpec = tween(
            durationMillis = 400, // Giảm duration
            easing = FastOutSlowInEasing
        ),
        label = "rotation"
    )

    val scale by animateFloatAsState(
        targetValue = if (startDrop) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    // Bounce effect khi landing
    val bounceScale by animateFloatAsState(
        targetValue = if (startDrop && dropY >= 25f) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bounceScale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isDropping) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = dropY.dp)
                    .rotate(rotation)
            ) {
                val ballRadius = (size.minDimension / 2) * scale * bounceScale
                drawDroppingBall(
                    center = Offset(size.width / 2, size.height / 2),
                    radius = ballRadius,
                    color = color,
                    number = number,
                    alpha = 1f,
                    isLanding = dropY >= 25f
                )
            }
        }
    }
}

// Hoàn chỉnh drawDroppingBall với hiệu ứng đẹp
private fun DrawScope.drawDroppingBall(
    center: Offset,
    radius: Float,
    color: Color,
    number: String,
    alpha: Float,
    isLanding: Boolean = false
) {
    // 1. Motion blur effect khi đang rơi
    if (!isLanding) {
        repeat(3) { i ->
            val blurOffset = i * 3f
            val blurAlpha = alpha * (0.2f - i * 0.05f)

            if (blurAlpha > 0f) {
                drawCircle(
                    color = color.copy(alpha = blurAlpha),
                    radius = radius * 0.95f,
                    center = center.copy(y = center.y - blurOffset)
                )
            }
        }
    }

    // 2. Shadow với độ mờ phù hợp
    drawCircle(
        color = Color.Black.copy(alpha = if (isLanding) 0.3f else 0.15f),
        radius = radius * (if (isLanding) 0.95f else 0.9f),
        center = center.copy(
            x = center.x + (if (isLanding) 3f else 1.5f),
            y = center.y + (if (isLanding) 4f else 2f)
        )
    )

    // 3. Main ball với gradient đẹp
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0.85f),
                color.copy(alpha = 0.7f)
            ),
            center = center.copy(x = center.x - radius * 0.3f, y = center.y - radius * 0.3f),
            radius = radius * 1.2f
        ),
        radius = radius,
        center = center
    )

    // 4. Outer glow cho special effects
    if (color == Color(0xFFFFD700)) { // Special prize gold
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius * 1.15f,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }

    // 5. Inner highlight
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.7f),
                Color.White.copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = center.copy(x = center.x - radius * 0.4f, y = center.y - radius * 0.4f),
            radius = radius * 0.4f
        ),
        radius = radius * 0.25f,
        center = center.copy(x = center.x - radius * 0.3f, y = center.y - radius * 0.3f)
    )

    // 6. Number background circle
    drawCircle(
        color = Color.White.copy(alpha = 0.95f),
        radius = radius * 0.65f,
        center = center
    )

    // 7. Inner border cho number background
    drawCircle(
        color = Color.White,
        radius = radius * 0.63f,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    // 8. Number text với shadow
    drawContext.canvas.nativeCanvas.apply {
        val textSize = when {
            number.length >= 5 -> radius * 0.5f
            number.length >= 4 -> radius * 0.6f
            number.length >= 3 -> radius * 0.7f
            number.length >= 2 -> radius * 0.8f
            else -> radius * 0.9f
        }

        // Text shadow
        val shadowPaint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.argb(50, 0, 0, 0)
            textAlign = android.graphics.Paint.Align.CENTER
            this.textSize = textSize
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        drawText(
            number,
            center.x + 1f,
            center.y + shadowPaint.textSize / 3 + 1f,
            shadowPaint
        )

        // Main text
        val textPaint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.BLACK
            textAlign = android.graphics.Paint.Align.CENTER
            this.textSize = textSize
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        drawText(
            number,
            center.x,
            center.y + textPaint.textSize / 3,
            textPaint
        )
    }

    // 9. ✅ Landing effect - sparkle khi chạm đất
//    if (isLanding) {
//        repeat(8) { i ->
//            val angle = (360f / 8) * i
//            val sparkleRadius = radius * 1.3f
//            val sparkleX = center.x + kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * sparkleRadius
//            val sparkleY = center.y + kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * sparkleRadius
//
//            drawCircle(
//                color = color.copy(alpha = 0.6f),
//                radius = 2.dp.toPx(),
//                center = Offset(sparkleX, sparkleY)
//            )
//        }
//    }

    // 10. Rim light effect
    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
}