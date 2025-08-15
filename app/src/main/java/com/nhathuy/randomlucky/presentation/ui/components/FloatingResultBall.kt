package com.nhathuy.randomlucky.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nhathuy.randomlucky.presentation.theme.LotteryGold
import kotlin.math.*

@Composable
fun FloatingResultBall(
    number: String,
    color: Color = LotteryGold,
    isVisible: Boolean,
    isSpecialPrize: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating_ball")

    val glow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_animation"
    )

    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_animation"
    )

    val sparkleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_rotation"
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    Canvas(
        modifier = modifier
    ) {
        if (isVisible && scaleAnimation > 0f) {
            val ballSize = (size.minDimension / 2) * scaleAnimation
            val centerY = size.height / 2 - bounce * scaleAnimation

            drawFloatingBall(
                center = Offset(size.width / 2, centerY),
                radius = ballSize,
                color = color,
                number = number,
                glow = glow,
                sparkleRotation = sparkleRotation,
                isSpecial = isSpecialPrize
            )
        }
    }
}

private fun DrawScope.drawFloatingBall(
    center: Offset,
    radius: Float,
    color: Color,
    number: String,
    glow: Float,
    sparkleRotation: Float,
    isSpecial: Boolean
) {
    // 1. Outer glow effect - multiple layers
    repeat(6) { i ->
        val glowRadius = radius * (1.8f + i * 0.3f) * glow
        val glowAlpha = (0.4f - i * 0.06f) * glow

        if (glowAlpha > 0f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = glowAlpha),
                        color.copy(alpha = glowAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = center
            )
        }
    }

    // 2. Special prize sparkles
    if (isSpecial) {
        repeat(8) { i ->
            val angle = sparkleRotation + i * 45f
            val sparkleDistance = radius * 2.2f
            val sparkleX = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * sparkleDistance
            val sparkleY = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * sparkleDistance

            // Large sparkles
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = glow * 0.9f),
                        Color.Yellow.copy(alpha = glow * 0.6f),
                        Color.Transparent
                    ),
                    radius = 12f
                ),
                radius = 8f * glow,
                center = Offset(sparkleX, sparkleY)
            )

            // Small sparkles
            val smallAngle = sparkleRotation * 1.5f + i * 45f + 22.5f
            val smallSparkleX = center.x + cos(Math.toRadians(smallAngle.toDouble())).toFloat() * (radius * 1.6f)
            val smallSparkleY = center.y + sin(Math.toRadians(smallAngle.toDouble())).toFloat() * (radius * 1.6f)

            drawCircle(
                color = Color.White.copy(alpha = glow * 0.7f),
                radius = 4f * glow,
                center = Offset(smallSparkleX, smallSparkleY)
            )
        }
    }

    // 3. Main ball shadow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.4f),
                Color.Black.copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = center.copy(x = center.x + 4f, y = center.y + 6f),
            radius = radius * 0.9f
        ),
        radius = radius * 0.9f,
        center = center.copy(x = center.x + 4f, y = center.y + 6f)
    )

    // 4. Main ball with enhanced gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = if (isSpecial) {
                listOf(
                    Color(0xFFFFD700).copy(alpha = 0.95f),
                    Color(0xFFFFB300).copy(alpha = 0.9f),
                    Color(0xFFFF8F00).copy(alpha = 0.8f),
                    Color(0xFFE65100).copy(alpha = 0.7f)
                )
            } else {
                listOf(
                    color.copy(alpha = 0.95f),
                    color.copy(alpha = 0.85f),
                    color.copy(alpha = 0.7f)
                )
            },
            center = center.copy(x = center.x - radius * 0.25f, y = center.y - radius * 0.25f),
            radius = radius * 1.2f
        ),
        radius = radius,
        center = center
    )

    // 5. Secondary depth highlight
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.6f),
                color.copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = center.copy(
                x = center.x + radius * 0.35f,
                y = center.y + radius * 0.35f
            ),
            radius = radius * 0.7f
        ),
        radius = radius * 0.5f,
        center = center.copy(
            x = center.x + radius * 0.35f,
            y = center.y + radius * 0.35f
        )
    )

    // 6. Multiple highlight layers for 3D effect
    repeat(3) { i ->
        val highlightSize = radius * (0.5f - i * 0.1f)
        val highlightAlpha = 0.9f - i * 0.25f
        val offsetMultiplier = 0.4f - i * 0.1f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = highlightAlpha),
                    Color.White.copy(alpha = highlightAlpha * 0.4f),
                    Color.Transparent
                ),
                center = center.copy(
                    x = center.x - radius * offsetMultiplier,
                    y = center.y - radius * offsetMultiplier
                ),
                radius = highlightSize
            ),
            radius = highlightSize,
            center = center.copy(
                x = center.x - radius * offsetMultiplier,
                y = center.y - radius * offsetMultiplier
            )
        )
    }

    // 7. Bright spot highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = radius * 0.15f,
        center = center.copy(
            x = center.x - radius * 0.45f,
            y = center.y - radius * 0.45f
        )
    )

    // 8. Number background with enhanced shadow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.9f)
            ),
            radius = radius * 0.65f
        ),
        radius = radius * 0.62f,
        center = center
    )

    // 9. Inner shadow for number background
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.08f),
                Color.Black.copy(alpha = 0.12f)
            ),
            radius = radius * 0.6f
        ),
        radius = radius * 0.58f,
        center = center
    )

    // 10. Draw number text with enhanced effects
    drawContext.canvas.nativeCanvas.apply {
        val textSize = when {
            number.length >= 4 -> radius * 0.65f
            number.length >= 3 -> radius * 0.75f
            number.length >= 2 -> radius * 0.85f
            else -> radius * 0.95f
        }

        // Multiple text shadows for depth
        repeat(3) { i ->
            val shadowOffset = (3 - i).toFloat()
            val shadowAlpha = 40 - i * 10

            val shadowPaint = android.graphics.Paint().apply {
                this.color = android.graphics.Color.BLACK
                this.alpha = shadowAlpha
                textAlign = android.graphics.Paint.Align.CENTER
                this.textSize = textSize
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }

            drawText(
                number,
                center.x + shadowOffset,
                center.y + shadowPaint.textSize / 3 + shadowOffset,
                shadowPaint
            )
        }

        // Main number text with stroke
        val textPaint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.BLACK
            textAlign = android.graphics.Paint.Align.CENTER
            this.textSize = textSize
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL_AND_STROKE
            strokeWidth = 2f
            strokeJoin = android.graphics.Paint.Join.ROUND
            strokeCap = android.graphics.Paint.Cap.ROUND
        }

        drawText(
            number,
            center.x,
            center.y + textPaint.textSize / 3,
            textPaint
        )
    }

    // 11. Outer rim glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                color.copy(alpha = 0.4f * glow),
                color.copy(alpha = 0.2f * glow),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.6f
        ),
        radius = radius * 1.3f,
        center = center,
        style = Stroke(width = 3f * glow)
    )

    // 12. Special prize additional effects
    if (isSpecial) {
        // Pulsing ring
        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = 0.6f * glow),
            radius = radius * (1.1f + glow * 0.1f),
            center = center,
            style = Stroke(width = 2f)
        )

        // Inner glow ring
        drawCircle(
            color = Color(0xFFFFFFFF).copy(alpha = 0.3f * glow),
            radius = radius * 0.7f,
            center = center,
            style = Stroke(width = 1f)
        )
    }
}