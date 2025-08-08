package com.nhathuy.randomlucky.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhathuy.randomlucky.domain.model.LotteryResult
import com.nhathuy.randomlucky.presentation.theme.LotteryGold
import kotlinx.coroutines.delay

@Composable
fun PrizeResultCard(result: LotteryResult, isVisible: Boolean, modifier: Modifier = Modifier,isLatest: Boolean = false) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        modifier = modifier
    ) {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if(result.prize.id == "special"){
                    Color(0xFF1A1A2E)
                }
                else{
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ){
            Column(
                modifier = Modifier.padding(20.dp)
            ){
                // prize name header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = result.prize.displayName,
                        fontSize = if (result.prize.id == "special") 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.prize.id == "special") {
                            LotteryGold
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )

                    if (result.prize.id == "special") {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 18.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.3f),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 2f
                                    )
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // hien thi ket qua
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    items(result.numbers) {
                        number ->
                        var isRevealed by remember { mutableStateOf(false) }
                        LaunchedEffect(result) {
                            delay(result.numbers.indexOf(number) * 200L)
                            isRevealed = true
                        }

                        LotteryNumberDisplay(
                            number,isRevealed,
                            modifier = Modifier.size(
                                if(result.prize.id == "special") 90.dp else 70.dp
                            )
                        )
                    }
                }

                // thong tin gia
                if (result.prize.numberOfResults > 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Số lượng giải: ${result.prize.numberOfResults}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

    }
}