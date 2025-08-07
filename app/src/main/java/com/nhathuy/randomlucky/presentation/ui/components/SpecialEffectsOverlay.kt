package com.nhathuy.randomlucky.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.*
import kotlin.random.Random

@Composable
fun SpecialEffectsOverlay(
    isActive: Boolean,
    modifier: Modifier = Modifier
){
    if(!isActive) return

    if (!isActive) return

    val particles = remember { List(50) { Particle() } }
    val time by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawParticle(particle, time)
        }
    }
}

private data class Particle(
    val x: Float = Random.nextFloat(),
    val y: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 3 + 1,
    val speed: Float = Random.nextFloat() * 0.5f + 0.5f,
    val color: Color = listOf(
        Color(0xFFFFD700),
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFF45B7D1)
    ).random()
)

private fun DrawScope.drawParticle(particle: Particle, time: Float) {
    val actualY = (particle.y + time * particle.speed) % 1f
    val actualX = particle.x + sin(actualY * PI * 2).toFloat() * 0.05f

    val center = Offset(
        x = actualX * size.width,
        y = actualY * size.height
    )

    // Draw star shape
    val path = Path().apply {
        val outerRadius = particle.size * 3
        val innerRadius = outerRadius * 0.5f

        for (i in 0..4) {
            val angle = (i * 72 - 90) * PI / 180
            val outerX = center.x + cos(angle).toFloat() * outerRadius
            val outerY = center.y + sin(angle).toFloat() * outerRadius

            if (i == 0) {
                moveTo(outerX, outerY)
            } else {
                lineTo(outerX, outerY)
            }

            val innerAngle = ((i * 72 + 36) - 90) * PI / 180
            val innerX = center.x + cos(innerAngle).toFloat() * innerRadius
            val innerY = center.y + sin(innerAngle).toFloat() * innerRadius
            lineTo(innerX, innerY)
        }
        close()
    }

    drawPath(
        path = path,
        color = particle.color.copy(alpha = 0.8f * (1f - actualY))
    )
}