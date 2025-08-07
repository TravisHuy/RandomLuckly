package com.nhathuy.randomlucky.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nhathuy.randomlucky.domain.model.LotterySession
import com.nhathuy.randomlucky.presentation.state.LotteryUiState
import com.nhathuy.randomlucky.presentation.ui.components.*
import com.nhathuy.randomlucky.presentation.viewmodel.LotteryViewModel
import com.nhathuy.randomlucky.presentation.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotteryScreen(
    viewModel: LotteryViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Theo dõi số lượng kết quả để tự động scroll
    val previousResultCount = remember { mutableStateOf(uiState.results.size) }
    val isFirstLaunch = remember { mutableStateOf(true) }

    val hasValidResults = uiState.results.isNotEmpty() &&
            uiState.results.values.any { it.numbers.isNotEmpty() }

    val hasValidSession = uiState.completedSession != null &&
            uiState.completedSession!!.results.isNotEmpty()


    LaunchedEffect(uiState.results.size, uiState.isRolling, uiState.currentPrize) {
        if (isFirstLaunch.value) {
            isFirstLaunch.value = false
            previousResultCount.value = uiState.results.size
            return@LaunchedEffect
        }

        // Khi bắt đầu quay giải mới
        if (uiState.isRolling && uiState.results.isEmpty() && uiState.currentPrize != null) {
            // Scroll về đầu để user thấy được lottery machine và current prize
            scrollState.animateScrollTo(0)
            previousResultCount.value = 0
            return@LaunchedEffect
        }

        // Khi có kết quả mới được tạo ra
        if (uiState.results.size > previousResultCount.value && !uiState.isRolling) {
            // Đợi animation hiển thị kết quả hoàn thành
            delay(400)

            // Tính toán vị trí scroll để hiển thị "Latest Result Section"
            val targetScrollPosition = when {
                uiState.results.size == 1 -> 700    // Scroll vừa đủ để thấy kết quả đầu tiên
                uiState.results.size <= 3 -> 900    // Scroll cho 2-3 kết quả
                else -> 1100                        // Scroll nhiều hơn khi có nhiều kết quả
            }

            scrollState.animateScrollTo(targetScrollPosition)
            previousResultCount.value = uiState.results.size
        }

        // Khi reset hoàn toàn (làm mới)
        if (!uiState.isRolling && uiState.results.isEmpty() && uiState.currentPrize == null) {
            scrollState.animateScrollTo(0)
            previousResultCount.value = 0
        }
    }

    //  scroll khi bắt đầu quay giải tiếp theo
    LaunchedEffect(uiState.currentPrize?.id) {
        if (uiState.isRolling && uiState.currentPrize != null) {
            scrollState.animateScrollTo(200)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F23),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        // Special effects overlay
        SpecialEffectsOverlay(
            isActive = uiState.isRolling && uiState.currentPrize?.id == "special" && !uiState.isPaused,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            TopHeader(
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToSettings = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current prize display - compact version
            AnimatedVisibility(
                visible = uiState.currentPrize != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                uiState.currentPrize?.let { prize ->
                    CurrentPrizeCard(
                        prize = prize,
                        isRolling = uiState.isRolling,
                        isPaused = uiState.isPaused,
                        progress = uiState.rollingProgress
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lottery machine - more compact
            LotteryBallMachine(
                isRolling = uiState.isRolling && !uiState.isPaused,
                rollingProgress = uiState.rollingProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Control buttons - improved layout
            ControlButtonsSection(
                uiState = uiState,
                onStart = { viewModel.startLottery() },
                onQuickStart = { viewModel.startLottery(withAnimation = false) },
                onPause = { viewModel.pauseLottery() },
                onResume = { viewModel.resumeLottery() },
                onReset = {
                    viewModel.resetLottery()
                    previousResultCount.value = 0
                    isFirstLaunch.value = false
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (hasValidResults && !uiState.isRolling) {
                LatestResultSection(
                    uiState = uiState,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            //hiển thị khi có từ 2 kết quả trở lên
            if ((hasValidResults && uiState.results.size >= 2) || hasValidSession) {
                Spacer(modifier = Modifier.height(16.dp))
                LotteryResultsBoard(
                    results = uiState.results,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Session completion info
            if (hasValidSession) {
                uiState.completedSession?.let { session ->
                    SessionCompletionCard(session = session)
                }
            }

            // padding bottom để đảm bảo có thể scroll đầy đủ
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
private fun LatestResultSection(
    uiState: LotteryUiState,
    modifier: Modifier = Modifier
) {
    // hiển thị kết quả vừa quay
    val latestResult = uiState.results.values.lastOrNull()

    AnimatedVisibility(
        visible = latestResult != null && !uiState.isRolling ,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 300f
            )
        ) + fadeIn(
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 300f
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight }
        ) + fadeOut(),
        modifier = modifier
    ) {
        latestResult?.let { result ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Section header - flat design như settings
                Text(
                    text = "🏆 KẾT QUẢ VỪA QUAY",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LotteryGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                )

                // Main result item - flat design như settings item
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = 0.7f,
                                stiffness = 400f
                            )
                        ),
                    color = Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        // Prize header row - giống style settings item
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Prize icon box - giống settings icon box
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (result.prize.id == "special") {
                                                LotteryGold.copy(alpha = 0.2f)
                                            } else {
                                                LotteryLightBlue.copy(alpha = 0.2f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (result.prize.id == "special") {
                                            Icons.Default.EmojiEvents
                                        } else {
                                            Icons.Default.Star
                                        },
                                        contentDescription = null,
                                        tint = if (result.prize.id == "special") {
                                            LotteryGold
                                        } else {
                                            LotteryLightBlue
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Prize info - giống settings text layout
                                Column {
                                    Text(
                                        text = result.prize.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Vừa hoàn thành • ${result.numbers.size} số",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            // Badge - style settings
                            if (result.prize.id == "special") {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(LotteryGold, Color(0xFFFFE55C))
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ĐẶC BIỆT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = LotteryGreen,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "MỚI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Enhanced Numbers display với design đẹp hơn
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(result.numbers.size) { index ->
                                val number = result.numbers[index]

                                // Staggered animation cho latest result
                                var isNumberVisible by remember { mutableStateOf(false) }

                                LaunchedEffect(index) {
                                    delay(index * 150L) // Staggered delay
                                    isNumberVisible = true
                                }

                                AnimatedVisibility(
                                    visible = isNumberVisible,
                                    enter = fadeIn(
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    ) + slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )
                                ) {
                                    //  Enhanced lottery ball với size và spacing tốt hơn
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp) // Tăng size để tránh vỡ số
                                            .clip(CircleShape) // Dùng CircleShape cho hình tròn hoàn hảo
                                            .background(
                                                brush = if (result.prize.id == "special") {
                                                    // Gradient đặc biệt cho giải đặc biệt
                                                    Brush.radialGradient(
                                                        colors = listOf(
                                                            Color(0xFFFFD700), // Vàng sáng
                                                            Color(0xFFFFB300), // Vàng đậm
                                                            Color(0xFFFF8F00)  // Cam vàng
                                                        ),
                                                        radius = 60f
                                                    )
                                                } else {
                                                    // Gradient cho giải thường
                                                    Brush.radialGradient(
                                                        colors = listOf(
                                                            Color(0xFFFFD700), // Vàng sáng
                                                            Color(0xFFFFA500), // Cam vàng
                                                            Color(0xFFFF7043)  // Cam đậm
                                                        ),
                                                        radius = 50f
                                                    )
                                                }
                                            )
                                            // Thêm shadow effect
                                            .let { baseModifier ->
                                                if (result.prize.id == "special") {
                                                    baseModifier.background(
                                                        brush = Brush.radialGradient(
                                                            colors = listOf(
                                                                Color.Transparent,
                                                                LotteryGold.copy(alpha = 0.3f)
                                                            ),
                                                            radius = 70f
                                                        ),
                                                        shape = CircleShape
                                                    )
                                                } else baseModifier
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Text với font size phù hợp
                                        Text(
                                            text = number,
                                            fontSize = when {
                                                number.length >= 5 -> 14.sp // Số dài
                                                number.length >= 3 -> 16.sp // Số trung bình
                                                else -> 18.sp // Số ngắn
                                            },
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            //  Đảm bảo text không bị overflow
                                            modifier = Modifier.wrapContentSize()
                                        )

                                        //  Thêm highlight effect cho giải đặc biệt
                                        if (result.prize.id == "special") {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        brush = Brush.radialGradient(
                                                            colors = listOf(
                                                                Color.Transparent,
                                                                LotteryGold.copy(alpha = 0.2f)
                                                            )
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bottom info row - giống settings description
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 Kết quả:",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Quay thành công ${result.numbers.size} số",
                                fontSize = 14.sp,
                                color = LotteryGold,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Special prize celebration - flat design
                if (result.prize.id == "special") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2D1B69).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .animateContentSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Celebration icon
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LotteryGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🎊",
                                    fontSize = 24.sp
                                )
                            }

                            // Celebration message
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Chúc mừng!",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LotteryGold
                                )
                                Text(
                                    text = "Bạn đã trúng giải đặc biệt - giải thưởng cao nhất!",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    lineHeight = 18.sp
                                )
                            }

                            // Celebration badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(LotteryGold, Color(0xFFFFE55C))
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "XUẤT SẮC",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopHeader(
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "XỔ SỐ MAY MẮN",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = LotteryGold
        )

        Box {
            IconButton(
                onClick = { showDropdownMenu = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Cài đặt",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            //dropdown menu
            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { showDropdownMenu = false},
                modifier = Modifier.background(
                    color = Color(0xFF1A1A2E),
                    shape = RoundedCornerShape(12.dp)
                )
            ) {
                // history menu item
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = LotteryGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Lịch sử",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    onClick = {
                        showDropdownMenu = false
                        onNavigateToHistory()
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // divider
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                //Settings menu item
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = LotteryGold,
                                modifier = Modifier.size(20.dp)
                            )

                            Text(text = "Cài đặt",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                    , onClick = {
                        showDropdownMenu = false
                        onNavigateToSettings()
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Thêm các menu item khác nếu cần
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // about menu item
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = LotteryGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Về ứng dụng",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    onClick = {
                        showDropdownMenu = false
                        showAboutDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    LotteryGold.copy(alpha = 0.3f),
                                    LotteryGold.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = LotteryGold,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Random Lucky",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LotteryGold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Ứng dụng xổ số may mắn hàng đầu Việt Nam",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                InfoRow("Phiên bản", "1.0.0", LotteryLightBlue)
                InfoRow("Nhà phát triển", "TravisHuy", LotteryGreen)
                InfoRow("Ngày phát hành", "02/08/2025", LotteryPurple)
                InfoRow("Nền tảng", "Android", LotteryOrange)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LotteryGold.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🎉 Cảm ơn bạn đã tin tưởng và sử dụng Random Lucky!",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = LotteryGold,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = LotteryGold
                )
            ) {
                Text(
                    "Đóng",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = Color(0xFF1E1E2E),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String, color: Color = LotteryGold) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun CurrentPrizeCard(
    prize: com.nhathuy.randomlucky.domain.model.LotteryPrize,
    isRolling: Boolean,
    isPaused: Boolean,
    progress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (prize.id == "special") {
                Color(0xFF2D1B69)
            } else {
                Color(0xFF1E3A8A)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status text
                Text(
                    text = when {
                        isPaused -> "⏸️ TẠM DỪNG"
                        isRolling -> "🎲 ĐANG QUAY"
                        else -> "⭐ CHUẨN BỊ"
                    },
                    fontSize = 14.sp,
                    color = when {
                        isPaused -> LotteryOrange
                        else -> Color.White.copy(alpha = 0.8f)
                    },
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Prize name
                Text(
                    text = prize.displayName,
                    fontSize = if (prize.id == "special") 26.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (prize.id == "special") LotteryGold else Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                // Progress indicator
                if (isRolling && !isPaused) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .width(120.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = LotteryGold,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = LotteryGold,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlButtonsSection(
    uiState: LotteryUiState,
    onStart: () -> Unit,
    onQuickStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primary action button
            Button(
                onClick = {
                    when {
                        uiState.isPaused -> onResume()
                        uiState.isRunning -> onReset()
                        else -> onStart()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        uiState.isPaused -> LotteryGreen
                        uiState.isRunning -> LotteryRed
                        else -> LotteryGold
                    }
                ),
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = when {
                        uiState.isPaused -> Icons.Default.PlayArrow
                        uiState.isRunning -> Icons.Default.Refresh
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        uiState.isPaused -> "Tiếp tục"
                        uiState.isRunning -> "Làm mới"
                        else -> "Bắt đầu"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Quick draw button (only when not running)
            if (!uiState.isRunning) {
                OutlinedButton(
                    onClick = onQuickStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(LotteryGold, Color.White)
                        )
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nhanh",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ✅ Enhanced secondary control buttons - hiển thị khi đang chạy HOẶC có kết quả và không hoàn thành
        AnimatedVisibility(
            visible = uiState.isRunning || (uiState.results.isNotEmpty() && uiState.completedSession == null),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ✅ Pause button - hiển thị khi đang rolling HOẶC có kết quả và session chưa hoàn thành
                if (uiState.canPause || (!uiState.isRolling && uiState.results.isNotEmpty() && uiState.completedSession == null)) {
                    Button(
                        onClick = {
                            if (uiState.isRolling) {
                                onPause() // Tạm dừng khi đang rolling
                            } else {
                                // Tạm dừng để xem kết quả - logic này sẽ được handle trong ViewModel
                                onPause()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isRolling) LotteryOrange else LotteryLightBlue
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isRolling) Icons.Default.Pause else Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isRolling) "Tạm dừng" else "Xem kết quả",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                // ✅ Continue button - chỉ hiển thị khi có kết quả và session chưa hoàn thành và không đang rolling
//                if (!uiState.isRolling && uiState.results.isNotEmpty() && uiState.completedSession == null && !uiState.isPaused) {
//                    Button(
//                        onClick = onResume, // Tiếp tục quay giải tiếp theo
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(44.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = LotteryGreen
//                        ),
//                        shape = RoundedCornerShape(22.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.PlayArrow,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp)
//                        )
//                        Spacer(modifier = Modifier.width(6.dp))
//                        Text(
//                            text = "Tiếp tục",
//                            fontSize = 13.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = Color.White
//                        )
//                    }
//                }

                // Stop button - luôn hiển thị khi có activity
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LotteryRed
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dừng",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ✅ Result viewing hint - hiển thị khi có kết quả mới
        if (!uiState.isRolling && uiState.results.isNotEmpty() && uiState.completedSession == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LotteryGold.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = LotteryGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "✨ Kết quả đã sẵn sàng! Bạn có thể xem kết quả hoặc tiếp tục quay giải tiếp theo.",
                        fontSize = 12.sp,
                        color = LotteryGold,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionCompletionCard(
    session: LotterySession,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A237E).copy(alpha = 0.8f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // Thêm gradient background overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                LotteryGold.copy(alpha = 0.1f),
                                LotteryPurple.copy(alpha = 0.1f),
                                Color(0xFF1E88E5).copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon và title với hiệu ứng shimmer
                    Text(
                        text = "🎉 HOÀN TẤT! 🎉",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LotteryGold, // Giữ màu vàng nổi bật
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mô tả với màu sáng hơn
                    Text(
                        text = "Tất cả các giải đã được quay xong",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f), // Tăng độ sáng
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mã session với màu accent
                    Text(
                        text = "Mã: ${session.id.take(8)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = LotteryGold.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}