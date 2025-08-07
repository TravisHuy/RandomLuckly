package com.nhathuy.randomlucky.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun AnimatedLotteryBall(
    ballNumber: Int,
    color: Color,
    isAnimating: Boolean,
    modifier: Modifier = Modifier
){
    val infiniteTransition = rememberInfiniteTransition()

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(
        modifier = modifier.size(60.dp)
    ) {
        if (isAnimating) {
            drawAnimatedBall(
                center = center.copy(y = center.y - bounce),
                radius = size.minDimension / 2,
                color = color,
                number = ballNumber,
                rotation = rotation
            )
        }
    }
}

private fun DrawScope.drawAnimatedBall(
    center: Offset,
    radius: Float,
    color: Color,
    number: Int,
    rotation: Float
) {
    // Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = radius * 0.9f,
        center = center.copy(y = center.y + radius * 0.1f),
        style = Fill
    )

    // Main ball with gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.9f),
                color,
                color.copy(alpha = 0.7f)
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Shine effect
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                Color.White.copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = center.copy(
                x = center.x - radius * 0.3f,
                y = center.y - radius * 0.3f
            ),
            radius = radius * 0.5f
        ),
        radius = radius * 0.4f,
        center = center.copy(
            x = center.x - radius * 0.3f,
            y = center.y - radius * 0.3f
        )
    )

    // Number on ball
    rotate(rotation, center) {
        // White circle for number background
        drawCircle(
            color = Color.White,
            radius = radius * 0.4f,
            center = center
        )
    }
}