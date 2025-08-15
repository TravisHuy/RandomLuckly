package com.nhathuy.randomlucky.presentation.ui.screen

import android.util.Log
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nhathuy.randomlucky.domain.model.LotterySession
import com.nhathuy.randomlucky.presentation.state.LotteryUiState
import com.nhathuy.randomlucky.presentation.ui.components.*
import com.nhathuy.randomlucky.presentation.viewmodel.LotteryViewModel
import com.nhathuy.randomlucky.presentation.viewmodel.HistoryViewModel
import com.nhathuy.randomlucky.presentation.theme.*
import kotlinx.coroutines.delay

private const val LOTTERY_SCREEN_TAG = "LotteryScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotteryScreen(
    viewModel: LotteryViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    shouldReset: Boolean = false,
    onResetHandled: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // kiểm tra xem đã quay về màn hình nào chưa
    var isReturningFromNavigation by remember { mutableStateOf(false) }

    // reset khi xóa tất cả lịch sử back về lottery
    LaunchedEffect(shouldReset) {
        if (shouldReset) {
            viewModel.resetLottery()
            onResetHandled()
            Log.d(LOTTERY_SCREEN_TAG, "DEBUG: Lottery reset due to history being cleared")
        }
    }


    // reset floating dialog và scroll khi quay về màn hình
    LaunchedEffect(Unit) {
        // đây sẽ chạy mỗi khi chúng ta chuyển về màn hình này
        isReturningFromNavigation = true
        delay(100)
        isReturningFromNavigation = false
    }

    // State cho hiệu ứng dropping balls và floating results
    var droppingNumbers by remember { mutableStateOf<List<String>>(emptyList()) }
    var floatingResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentPrizeName by remember { mutableStateOf("") }
    var isShowingFloatingDialog by remember { mutableStateOf(false) }
    var droppingColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    var isSpecialPrize by remember { mutableStateOf(false) }
    var isViewingResults by remember { mutableStateOf(false) }


    // reset floating dialog trạng thái khi trờ về màn hình
    LaunchedEffect(isReturningFromNavigation) {
        if (isReturningFromNavigation) {
            // xóa floating dialog và hiển thị kết quả mới nhất.
            isShowingFloatingDialog = false
            floatingResults = emptyList()
            isViewingResults = false
            droppingNumbers = emptyList()
            droppingColors = emptyList()

            // reset scroll và hiển thị kết quả vừa quay
            if (uiState.results.isNotEmpty()) {
                delay(200)
                // Scroll xuống kết quả vừa quay
                scrollState.animateScrollTo(800)
            }

            Log.d(LOTTERY_SCREEN_TAG, "DEBUG: Reset floating dialog state on navigation return")
        }
    }

    // Theo dõi số lượng kết quả để tự động scroll
    val previousResultCount = remember { mutableStateOf(uiState.results.size) }
    val isFirstLaunch = remember { mutableStateOf(true) }

    val hasValidResults = uiState.results.isNotEmpty() &&
            uiState.results.values.any { it.numbers.isNotEmpty() }

    val hasValidSession = uiState.completedSession != null &&
            uiState.completedSession!!.results.isNotEmpty()

    // xóa tất cả các hiệu ứng khi giải đã reset
    LaunchedEffect(uiState.results.isEmpty() && !uiState.isRunning, shouldReset) {
        if (shouldReset && uiState.results.isEmpty() && !uiState.isRunning && uiState.currentPrize == null) {
            droppingNumbers = emptyList()
            floatingResults = emptyList()
            isShowingFloatingDialog = false
            isViewingResults = false
            droppingColors = emptyList()
            currentPrizeName = ""
            isSpecialPrize = false
            Log.d(LOTTERY_SCREEN_TAG, "DEBUG: All effects cleared due to explicit lottery reset")
        }
    }

    // Enhanced result handling với dialog logic
    LaunchedEffect(uiState.results.size, uiState.isRolling, isReturningFromNavigation, uiState.completedSession) {
        val latestResult = uiState.results.values.lastOrNull()

        // Không trigger animation nếu session đã hoàn thành hoặc đang quay về từ navigation
        if (latestResult != null &&
            !uiState.isRolling &&
            latestResult.numbers.isNotEmpty() &&
            !isReturningFromNavigation &&
            uiState.completedSession == null //Chỉ trigger khi session chưa hoàn thành
        ) {
            currentPrizeName = latestResult.prize.displayName
            isSpecialPrize = latestResult.prize.id == "special"

            // Xác định màu sắc theo giải
            val prizeColors = List(latestResult.numbers.size) {
                when {
                    latestResult.prize.id == "special" -> Color(0xFFFFD700) // Vàng
                    latestResult.prize.id == "first" -> Color(0xFFE91E63)   // Hồng
                    latestResult.prize.id == "second" -> Color(0xFF2196F3)  // Xanh dương
                    latestResult.prize.id == "third" -> Color(0xFF4CAF50)   // Xanh lá
                    else -> Color(0xFF4FC3F7) // Xanh nhạt
                }
            }

            //  Phase 1: Chuẩn bị và scroll đến vị trí dropping
            droppingNumbers = latestResult.numbers
            droppingColors = prizeColors
            isShowingFloatingDialog = false
            isViewingResults = false // Reset state

            //  Scroll để dropping balls hiển thị trong viewport
            delay(100)
            val droppingScrollTarget = when {
                latestResult.numbers.size <= 3 -> 450
                latestResult.numbers.size <= 6 -> 480
                latestResult.numbers.size <= 10 -> 500
                else -> 520
            }
            scrollState.animateScrollTo(droppingScrollTarget)

            //  Đợi dropping animation hoàn thành - giảm delay time để nhanh hơn
            val dropDuration = when {
                latestResult.numbers.size == 1 -> 1200L      // 1 số: 1200L
                latestResult.numbers.size <= 3 -> 1800L     // 2-3 số:  1800L
                latestResult.numbers.size <= 6 -> 2200L     // 4-6 số:  2200L
                latestResult.numbers.size <= 10 -> 2800L    // 7-10 số:  2800L
                latestResult.numbers.size <= 15 -> 3500L    // 11-15 số:  3500L
                else -> 4200L                               // Nhiều số:  4200L
            }
            delay(dropDuration)

            //  Kiểm tra nếu đang xem kết quả thì không chuyển sang floating dialog
            if (!isViewingResults) {
                //  Phase 2: Clear dropping, show floating dialog
                droppingNumbers = emptyList()
                floatingResults = latestResult.numbers
                isShowingFloatingDialog = true
            }
        }

        // Reset khi bắt đầu giải mới
        if (uiState.isRolling && uiState.currentPrize != null) {
            droppingNumbers = emptyList()
            floatingResults = emptyList()
            isShowingFloatingDialog = false
            isViewingResults = false

            // Scroll về lottery machine khi bắt đầu quay
            delay(200)
            scrollState.animateScrollTo(150)
        }
    }

    // Auto scroll logic cho các trường hợp khác
    LaunchedEffect(uiState.results.size, uiState.isRolling) {
        if (isFirstLaunch.value) {
            isFirstLaunch.value = false
            previousResultCount.value = uiState.results.size
            return@LaunchedEffect
        }

        // Khi reset hoàn toàn (làm mới)
        if (!uiState.isRolling && uiState.results.isEmpty() && uiState.currentPrize == null) {
            scrollState.animateScrollTo(0)
            previousResultCount.value = 0
        }
    }

    // reset khi session hoàn thành hoặc reset lottery
    LaunchedEffect(uiState.completedSession, uiState.results.isEmpty()) {
        if (uiState.completedSession != null || uiState.results.isEmpty()) {
            // Clear tất cả animation states khi session hoàn thành
            isShowingFloatingDialog = false
            floatingResults = emptyList()
            isViewingResults = false
            droppingNumbers = emptyList()
            droppingColors = emptyList()

            Log.d(LOTTERY_SCREEN_TAG, "DEBUG: Cleared all animation states - session completed: ${uiState.completedSession != null}, results empty: ${uiState.results.isEmpty()}")
        }
    }

    val handleNavigateToHistory = {
        // xóa floating dialog trước khi chuyển hướng
        isShowingFloatingDialog = false
        floatingResults = emptyList()
        isViewingResults = false
        droppingNumbers = emptyList()
        droppingColors = emptyList()

        onNavigateToHistory()
    }

    val handleNavigateToSettings = {
        // xóa floating dialog trước khi chuyển hướng
        isShowingFloatingDialog = false
        floatingResults = emptyList()
        isViewingResults = false
        droppingNumbers = emptyList()
        droppingColors = emptyList()

        onNavigateToSettings()
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
        // hiển thị giải đặc biệt
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
                onNavigateToHistory = handleNavigateToHistory,
                onNavigateToSettings = handleNavigateToSettings
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

            // Enhanced Lottery machine với số thật
            LotteryBallMachine(
                isRolling = uiState.isRolling && !uiState.isPaused,
                rollingProgress = uiState.rollingProgress,
                currentNumbers = uiState.currentPrize?.let { prize ->
                    // Tạo danh sách số có thể trúng cho giải này
                    when (prize.id) {
                        "special" -> (1..99).map { it.toString().padStart(2, '0') }.shuffled()
                            .take(12)

                        "first" -> (1..99).map { it.toString().padStart(2, '0') }.shuffled()
                            .take(10)

                        "second" -> (10..99).map { it.toString() }.shuffled().take(15)
                        "third" -> (100..999).map { it.toString() }.shuffled().take(20)
                        else -> (1..50).map { it.toString() }.shuffled().take(8)
                    }
                } ?: emptyList(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Control buttons
            ControlButtonsSection(
                uiState = uiState,
                onStart = { viewModel.startLottery() },
                onQuickStart = { viewModel.startLottery(withAnimation = false) },
                onPause = { viewModel.pauseLottery() },
                onResume = { viewModel.resumeLottery() },
                onReset = {
                    viewModel.resetLottery()
                    // Clear effects
                    droppingNumbers = emptyList()
                    floatingResults = emptyList()
                    isShowingFloatingDialog = false
                    previousResultCount.value = 0
                    isFirstLaunch.value = false
                }
            )


            Spacer(modifier = Modifier.height(32.dp))

            // Dropping balls animation với position tối ưu
            if (droppingNumbers.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    DroppingBallCompleted(
                        numbers = droppingNumbers,
                        colors = droppingColors,
                        isDropping = true,
                        onAllDropsComplete = {
                            // Sound effect hoặc haptic feedback có thể thêm ở đây
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Latest result section (khi không có floating)
            if (hasValidResults && !uiState.isRolling && !isShowingFloatingDialog) {
                LatestResultSection(
                    uiState = uiState,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Results board cho nhiều giải
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

            // Bottom padding
            Spacer(modifier = Modifier.height(120.dp))
        }

        // Floating Result Dialog - hiển thị như overlay
        if (isShowingFloatingDialog && floatingResults.isNotEmpty()) {
            Dialog(
                onDismissRequest = {
                    if (!isViewingResults) {
                        isShowingFloatingDialog = false
                        floatingResults = emptyList()
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = !isViewingResults,
                    dismissOnClickOutside = !isViewingResults,
                    usePlatformDefaultWidth = false
                )
            ) {
                FloatingResultDialog(
                    numbers = floatingResults,
                    prizeName = currentPrizeName,
                    isSpecialPrize = isSpecialPrize,
                    isViewingResults = isViewingResults,
                    isLastPrize = isSpecialPrize, // Giải đặc biệt là giải cuối cùng
                    onViewResults = {
                        isViewingResults = true
                        //  Thực sự pause lottery thông qua viewModel
                        viewModel.pauseForViewingResults()
                    },
                    onContinue = {
                        isShowingFloatingDialog = false
                        floatingResults = emptyList()
                        isViewingResults = false
                        // Resume lottery để tiếp tục quay giải tiếp theo
                        viewModel.resumeLottery()
                    },
                    onClose = {
                        // Đóng dialog khi là giải cuối cùng
                        isShowingFloatingDialog = false
                        floatingResults = emptyList()
                        isViewingResults = false
                    }
                )
            }
        }
    }
}

// Enhanced Floating Result Dialog Component
@Composable
private fun FloatingResultDialog(
    numbers: List<String>,
    prizeName: String,
    isSpecialPrize: Boolean,
    isViewingResults: Boolean,
    isLastPrize: Boolean,
    onViewResults: () -> Unit,
    onContinue: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .wrapContentHeight()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (isSpecialPrize) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2D1B69).copy(alpha = 0.8f),
                                Color(0xFF1A1A2E),
                                Color(0xFF0F0F23)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E3A8A).copy(alpha = 0.6f),
                                Color(0xFF1A1A2E),
                                Color(0xFF0F0F23)
                            )
                        )
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Enhanced header với animation
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Celebration icon
                    Text(
                        text = if (isSpecialPrize) "🏆" else "⭐",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Prize title
                    Text(
                        text = if (isSpecialPrize) "🎉 $prizeName 🎉" else "✨ $prizeName ✨",
                        fontSize = if (isSpecialPrize) 24.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpecialPrize) LotteryGold else LotteryLightBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle - thay đổi theo trạng thái
                    Text(
                        text = if (isViewingResults) {
                            if (isSpecialPrize) "Đang xem giải thưởng cao nhất" else "Đang xem kết quả"
                        } else {
                            if (isSpecialPrize) "Chúc mừng! Giải thưởng cao nhất!" else "Kết quả vừa được quay!"
                        },
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Enhanced FloatingResultGrid
                FloatingResultGrid(
                    numbers = numbers,
                    prizeName = prizeName,
                    isSpecialPrize = isSpecialPrize,
                    isVisible = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons row - thay đổi theo trạng thái và loại giải
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isLastPrize) {
                        // Nút "Đóng" cho giải đặc biệt (giải cuối cùng)
                        Button(
                            onClick = {
                                onClose()
                                println("DEBUG: Clicked Close button for special prize")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LotteryGold
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Đóng",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    } else {
                        // Logic cũ cho các giải khác
                        if (!isViewingResults) {
                            // View results button
                            Button(
                                onClick = {
                                    onViewResults()
                                    println("DEBUG: Clicked View Results button")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LotteryLightBlue
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Xem kết quả",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            // Continue button - hiển thị khi đang xem kết quả
                            Button(
                                onClick = {
                                    onContinue()
                                    println("DEBUG: Clicked Continue button")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LotteryGreen
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tiếp tục",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Hint text - thay đổi theo trạng thái
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isLastPrize) {
                                "🎉 Tất cả giải đã hoàn thành! Nhấn 'Đóng' để kết thúc."
                            } else if (isViewingResults) {
                                "💡 Nhấn 'Tiếp tục' để quay giải tiếp theo"
                            } else {
                                "👀 Nhấn 'Xem kết quả' để tạm dừng và xem chi tiết"
                            },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
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
        visible = latestResult != null && !uiState.isRolling,
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
                onDismissRequest = { showDropdownMenu = false },
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = LotteryGold,
                                modifier = Modifier.size(20.dp)
                            )

                            Text(
                                text = "Cài đặt",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }, onClick = {
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

// Giữ nguyên ControlButtonsSection từ version cũ
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

        // Enhanced secondary control buttons - hiển thị khi đang chạy HOẶC có kết quả và không hoàn thành
        AnimatedVisibility(
            visible = uiState.isRunning || (uiState.results.isNotEmpty() && uiState.completedSession == null),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pause button - hiển thị khi đang rolling HOẶC có kết quả và session chưa hoàn thành
                if (uiState.canPause || (!uiState.isRolling && uiState.results.isNotEmpty() && uiState.completedSession == null)) {
                    Button(
                        onClick = {
                            if (uiState.isRolling) {
                                onPause() // Tạm dừng khi đang quay
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

        // Result viewing hint - hiển thị khi có kết quả mới
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
                        text = "✨ Kết quả đã s��n sàng! Bạn có thể xem kết quả hoặc tiếp tục quay giải tiếp theo.",
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

