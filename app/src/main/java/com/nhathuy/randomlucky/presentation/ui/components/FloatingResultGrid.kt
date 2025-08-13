package com.nhathuy.randomlucky.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun FloatingResultGrid(
    numbers: List<String>,
    prizeName: String,
    isSpecialPrize: Boolean = false,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    var visibleItems by remember { mutableStateOf<List<Boolean>>(emptyList()) }
    var showTitle by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible, numbers.size) {
        if (isVisible && numbers.isNotEmpty()) {
            visibleItems = List(numbers.size) { false }
            showTitle = false

            // Hiện title trước
            delay(200L)
            showTitle = true


            val itemDelay = when {
                numbers.size <= 3 -> 300L       // Ít số: delay lâu để tạo dramatic effect
                numbers.size <= 6 -> 200L       // Trung bình: delay vừa phải
                numbers.size <= 10 -> 150L      // Nhiều số: delay ngắn
                numbers.size <= 15 -> 100L      // Rất nhiều: delay rất ngắn
                else -> 80L                     // Cực nhiều: delay tối thiểu
            }

            // Sau đó hiện tuần tự với hiệu ứng staggered
            numbers.forEachIndexed { index, _ ->
                delay(itemDelay)
                visibleItems = visibleItems.toMutableList().apply {
                    this[index] = true
                }
            }

        } else {
            visibleItems = emptyList()
            showTitle = false
        }
    }

    if (isVisible && numbers.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Enhanced Prize Title với animation
            AnimatedVisibility(
                visible = showTitle,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                PrizeTitle(
                    title = prizeName,
                    isSpecial = isSpecialPrize,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Results grid - responsive layout với enhanced spacing
            LazyVerticalGrid(
                columns = GridCells.Fixed(
                    when {
                        numbers.size <= 3 -> numbers.size
                        numbers.size <= 6 -> 3
                        numbers.size <= 9 -> 3
                        numbers.size <= 12 -> 4
                        numbers.size <= 20 -> 5
                        else -> 6
                    }
                ),
                horizontalArrangement = Arrangement.spacedBy(
                    when {
                        numbers.size <= 6 -> 16.dp
                        numbers.size <= 12 -> 12.dp
                        else -> 8.dp
                    }
                ),
                verticalArrangement = Arrangement.spacedBy(
                    when {
                        numbers.size <= 6 -> 16.dp
                        numbers.size <= 12 -> 12.dp
                        else -> 8.dp
                    }
                ),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
                modifier = Modifier.heightIn(
                    max = when {
                        numbers.size <= 6 -> 200.dp
                        numbers.size <= 12 -> 300.dp
                        numbers.size <= 20 -> 400.dp
                        else -> 500.dp
                    }
                )
            ) {
                itemsIndexed(numbers) { index, number ->
                    FloatingResultBall(
                        number = number,
                        color = if (isSpecialPrize) Color(0xFFFFD700) else Color(0xFF4FC3F7),
                        isVisible = visibleItems.getOrElse(index) { false },
                        isSpecialPrize = isSpecialPrize,
                        modifier = Modifier.size(
                            when {
                                numbers.size <= 3 -> 90.dp
                                numbers.size <= 6 -> 75.dp
                                numbers.size <= 12 -> 65.dp
                                numbers.size <= 20 -> 55.dp
                                else -> 45.dp
                            }
                        )
                    )
                }
            }

            // Summary info cho nhiều số
            if (numbers.size > 6) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSpecialPrize) {
                            Color(0xFFFFD700).copy(alpha = 0.1f)
                        } else {
                            Color(0xFF4FC3F7).copy(alpha = 0.1f)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Tổng cộng:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${numbers.size} số trúng thưởng",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSpecialPrize) Color(0xFFFFD700) else Color(0xFF4FC3F7)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrizeTitle(
    title: String,
    isSpecial: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main title với gradient background
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = if (isSpecial) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.2f),
                                Color(0xFFFFB300).copy(alpha = 0.2f),
                                Color(0xFFFFD700).copy(alpha = 0.2f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4FC3F7).copy(alpha = 0.2f),
                                Color(0xFF29B6F6).copy(alpha = 0.2f),
                                Color(0xFF4FC3F7).copy(alpha = 0.2f)
                            )
                        )
                    }
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isSpecial) "🏆 $title 🏆" else "⭐ $title ⭐",
                fontSize = if (isSpecial) 22.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSpecial) Color(0xFFFFD700) else Color(0xFF4FC3F7),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = if (isSpecial) "Giải thưởng cao nhất" else "Kết quả vừa quay",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}