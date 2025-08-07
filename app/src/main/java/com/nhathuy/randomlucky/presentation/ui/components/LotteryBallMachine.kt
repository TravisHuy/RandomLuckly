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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue =  if(isRolling) 360f else 0f,
        animationSpec =  if(isRolling) {
            infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        }
        else {
            spring(stiffness = Spring.StiffnessLow)
        }
    )

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
                drawLotteryMachine(isRolling, rollingProgress)
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
                        text = "?",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawLotteryMachine(isRolling: Boolean, progress: Float) {
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
        val ballCount = 10
        val ballRadius = 20.dp.toPx()

        repeat(ballCount) { index ->
            val angle = (index * 360f / ballCount) + (progress * 360f)
            val ballDistance = radius * 0.7f * (0.8f + 0.2f * sin(angle * 3))

            val ballX = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * ballDistance
            val ballY = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * ballDistance

            drawLotteryBall(
                center = Offset(ballX, ballY),
                radius = ballRadius,
                color = BallColors[index % BallColors.size],
                number = Random.nextInt(0, 100)
            )
        }
    }
}

private fun DrawScope.drawLotteryBall(
    center: Offset,
    radius: Float,
    color: Color,
    number: Int
) {
    // bóng của bóng
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = radius,
        center = center.copy(y = center.y + 2.dp.toPx())
    )

    // thân của bóng
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.9f),
                color
            ),
            center = center.copy(x = center.x - radius / 3, y = center.y - radius / 3),
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // bóng highlight
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.6f),
                Color.White.copy(alpha = 0.1f),
                Color.Transparent
            ),
            center = center.copy(x = center.x - radius / 3, y = center.y - radius / 3),
            radius = radius / 2
        ),
        radius = radius / 3,
        center = center.copy(x = center.x - radius / 3, y = center.y - radius / 3)
    )
}