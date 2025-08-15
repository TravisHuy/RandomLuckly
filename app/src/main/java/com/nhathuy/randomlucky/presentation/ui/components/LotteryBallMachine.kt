package com.nhathuy.randomlucky.presentation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhathuy.randomlucky.presentation.theme.BallColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LotteryBallMachine(
    isRolling: Boolean,
    rollingProgress: Float,
    currentNumbers: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue =  if(isRolling) 360f * 10 else 0f,
        animationSpec =  if(isRolling) {
            infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        }
        else {
            spring(stiffness = Spring.StiffnessLow)
        }
    )

    val ballNumber = remember(isRolling) {
        if (isRolling && currentNumbers.isNotEmpty()) {
            // Trộn số thật với số random
            (currentNumbers + List(15) { Random.nextInt(1, 100).toString() }).shuffled()
        } else {
            List(20) { Random.nextInt(1, 100).toString() }
        }
    }

    Card(modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .height(320.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ){
            // Bình đựng quả cầu thủy tinh
            Canvas(
                modifier = Modifier.size(280.dp)
                    .rotate(rotation)
            ){
                drawLotteryMachine(isRolling, rollingProgress,ballNumber)
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color.White.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isRolling) {
                    Text(
                        text = "🎲",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                else {
                    Text(
                        text = "⭐",
                        fontSize = 36.sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawLotteryMachine(isRolling: Boolean, progress: Float,ballNumbers: List<String>) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 2

    // Vẽ quả cầu thủy tinh
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.1f),
                Color.White.copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Vẽ đường viền hình cầu
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = radius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
    // Vẽ những quả bóng quay
    if (isRolling) {
        val ballCount = minOf(ballNumbers.size, 15)
        val ballRadius = 18.dp.toPx()

        repeat(ballCount) { index ->
            val angle = (index * 360f / ballCount) + (progress * 720f)
            val ballDistance = radius * (0.5f + 0.3f * sin(angle * 2 + progress * 360f))

            val ballX = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * ballDistance
            val ballY = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * ballDistance

            drawNumberedLotteryBall(
                center = Offset(ballX, ballY),
                radius = ballRadius,
                color = BallColors[index % BallColors.size],
                number = ballNumbers[index]
            )
        }
    }
}

private fun DrawScope.drawNumberedLotteryBall(
    center: Offset,
    radius: Float,
    color: Color,
    number: String
) {
    // Bóng của bóng
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = radius * 0.9f,
        center = center.copy(y = center.y + 2.dp.toPx())
    )

    // Thân của bóng với gradient đẹp hơn
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.9f),
                color,
                color.copy(alpha = 0.8f)
            ),
            center = center.copy(x = center.x - radius / 3, y = center.y - radius / 3),
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Highlight bóng
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.7f),
                Color.White.copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = center.copy(x = center.x - radius / 3, y = center.y - radius / 3),
            radius = radius / 2
        ),
        radius = radius / 3,
        center = center.copy(x = center.x - radius / 3, y = center.y - radius / 3)
    )

    // Vòng tròn trắng để viết số
    drawCircle(
        color = Color.White,
        radius = radius * 0.5f,
        center = center
    )

    // Viết số lên bóng
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.BLACK
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = (radius * 0.6f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        drawText(
            number,
            center.x,
            center.y + paint.textSize / 3,
            paint
        )
    }
}