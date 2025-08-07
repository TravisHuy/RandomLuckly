package com.nhathuy.randomlucky.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhathuy.randomlucky.presentation.theme.LotteryGold

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LotteryNumberDisplay(
    number :String,
    isRevealed : Boolean,
    modifier: Modifier = Modifier
){

    val scale by animateFloatAsState(
        targetValue =  if(isRevealed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val rotation by animateFloatAsState(
        targetValue =  if(isRevealed) 0f else 180f,
        animationSpec = tween(durationMillis = 600)
    )

    Box(
        modifier = modifier
            .size(80.dp)
            .scale(scale)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ){
        Card(modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                LotteryGold,
                                LotteryGold.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.2f)
                            ),
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ){
                AnimatedContent(
                    targetState = if(isRevealed) number else "?",
                    transitionSpec = {
                        fadeIn() with fadeOut()
                    }
                ) {
                    displayNumber ->
                    Text(
                        text = displayNumber,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center)
                }
            }
        }
    }
}