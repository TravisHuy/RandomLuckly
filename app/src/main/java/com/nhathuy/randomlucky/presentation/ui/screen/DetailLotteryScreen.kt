package com.nhathuy.randomlucky.presentation.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nhathuy.randomlucky.presentation.viewmodel.HistoryViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhathuy.randomlucky.domain.model.LotteryResult
import com.nhathuy.randomlucky.domain.model.LotterySession
import com.nhathuy.randomlucky.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun DetailLotteryScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.sessions.find { it.id == sessionId }

    LaunchedEffect(sessionId) {
        if (session == null) {
            viewModel.loadHistory()
        }
    }

    when {
        session == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F0F23),
                                Color(0xFF1A1A2E),
                                Color(0xFF16213E),
                                Color(0xFF0F0F23)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = LotteryGold)
                    Text(
                        text = "Đang tải chi tiết...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        else -> {
            DetailLotteryContent(
                session = session,
                onNavigateBack = onNavigateBack,
                onDelete = {
                    viewModel.deleteSession(sessionId)
                    onNavigateBack()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailLotteryContent(
    session: LotterySession,
    onNavigateBack: () -> Unit,
    onDelete: () -> Unit
) {
    var showShareDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F23),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F0F23)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced Header với style giống Settings
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Chi tiết kết quả",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showShareDialog = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Chia sẻ",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )

            // Content với spacing đẹp hơn giống Settings
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Session info section
                item {
                    SettingsSection(
                        title = "🎲 THÔNG TIN PHIÊN QUAY",
                        gradient = Brush.horizontalGradient(
                            colors = listOf(
                                LotteryDarkBlue.copy(alpha = 0.3f),
                                LotteryPurple.copy(alpha = 0.2f)
                            )
                        )
                    ) {
                        SessionInfoContent(session = session)
                    }
                }

                // Results section
                val allPrizes = session.results.toList()
                    .sortedBy { (prizeId, _) ->
                        when (prizeId) {
                            "special" -> 0
                            "first" -> 1
                            "second" -> 2
                            "third" -> 3
                            "fourth" -> 4
                            "fifth" -> 5
                            "sixth" -> 6
                            "seventh" -> 7
                            "eighth" -> 8
                            else -> 9
                        }
                    }

                if (allPrizes.isNotEmpty()) {
                    item {
                        SettingsSection(
                            title = "🏆 KẾT QUẢ XỔ SỐ",
                            gradient = Brush.horizontalGradient(
                                colors = listOf(
                                    LotteryGold.copy(alpha = 0.3f),
                                    LotteryOrange.copy(alpha = 0.2f)
                                )
                            )
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                allPrizes.forEach { (prizeId, result) ->
                                    PrizeResultDisplayItem(
                                        prizeId = prizeId,
                                        result = result
                                    )

                                    if (prizeId != allPrizes.last().first) {
                                        Divider(
                                            color = Color.White.copy(alpha = 0.1f),
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Statistics section
                item {
                    SettingsSection(
                        title = "📊 THỐNG KÊ TỔNG KẾT",
                        gradient = Brush.horizontalGradient(
                            colors = listOf(
                                LotteryGreen.copy(alpha = 0.3f),
                                LotteryLightBlue.copy(alpha = 0.2f)
                            )
                        )
                    ) {
                        StatisticsContent(session = session)
                    }
                }

//                // Actions section
//                item {
//                    SettingsSection(
//                        title = "⚙️ THAO TÁC",
//                        gradient = Brush.horizontalGradient(
//                            colors = listOf(
//                                LotteryPurple.copy(alpha = 0.3f),
//                                LotteryRed.copy(alpha = 0.2f)
//                            )
//                        )
//                    ) {
//                        ActionItem(
//                            title = "Chia sẻ kết quả",
//                            description = "Chia sẻ dưới dạng text hoặc hình ảnh",
//                            icon = Icons.Default.Share,
//                            onClick = { showShareDialog = true },
//                            iconTint = LotteryGreen
//                        )
//
//                        Divider(
//                            color = Color.White.copy(alpha = 0.1f),
//                            thickness = 0.5.dp,
//                            modifier = Modifier.padding(vertical = 12.dp)
//                        )
//
//                        ActionItem(
//                            title = "Xóa phiên này",
//                            description = "Xóa vĩnh viễn dữ liệu phiên quay này",
//                            icon = Icons.Default.Delete,
//                            onClick = { showDeleteDialog = true },
//                            iconTint = LotteryRed
//                        )
//                    }
//                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    // Dialogs
    if (showShareDialog) {
        ShareDialog(
            session = session,
            onDismiss = { showShareDialog = false }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    gradient: Brush,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .background(Color(0xFF1E1E2E).copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LotteryGold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                content()
            }
        }
    }
}

@Composable
private fun SessionInfoContent(session: LotterySession) {
    // Date and time
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LotteryGold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = LotteryGold,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Thời gian thực hiện",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
                    .format(Date(session.startTime)),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }
    }

    Divider(
        color = Color.White.copy(alpha = 0.1f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(vertical = 12.dp)
    )

    // Session ID
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LotteryLightBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = LotteryLightBlue,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Mã phiên quay",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = session.id.take(8),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }

        if (session.isCompleted) {
            Badge(
                containerColor = LotteryGreen
            ) {
                Text(
                    text = "✓ Hoàn tất",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    Divider(
        color = Color.White.copy(alpha = 0.1f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(vertical = 12.dp)
    )

    // Quick stats row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickStatItem(
            title = "Tổng giải",
            value = session.results.size.toString(),
            icon = Icons.Default.EmojiEvents,
            color = LotteryGold
        )
        QuickStatItem(
            title = "Tổng số",
            value = session.results.values.sumOf { it.numbers.size }.toString(),
            icon = Icons.Default.Numbers,
            color = LotteryGreen
        )
        QuickStatItem(
            title = "Thời gian",
            value = if (session.endTime != null && session.endTime != 0L) {
                "${(session.endTime - session.startTime) / 1000}s"
            } else "N/A",
            icon = Icons.Default.Timer,
            color = LotteryPurple
        )
    }
}

@Composable
private fun QuickStatItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PrizeResultDisplayItem(
    prizeId: String,
    result: LotteryResult
) {
    val prizeName = when (prizeId) {
        "special" -> "🏆 GIẢI ĐẶC BIỆT"
        "first" -> "🥇 GIẢI NHẤT"
        "second" -> "🥈 GIẢI NHÌ"
        "third" -> "🥉 GIẢI BA"
        "fourth" -> "🏅 GIẢI TƯ"
        "fifth" -> "🏅 GIẢI NĂM"
        "sixth" -> "🏅 GIẢI SÁU"
        "seventh" -> "🏅 GIẢI BẢY"
        "eighth" -> "🏅 GIẢI TÁM"
        else -> "🎯 ${result.prize.name}"
    }

    val prizeColor = when (prizeId) {
        "special" -> LotteryGold
        "first" -> LotteryGold
        "second" -> Color(0xFFC0C0C0) // Silver
        "third" -> Color(0xFFCD7F32) // Bronze
        else -> LotteryLightBlue
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(prizeColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = prizeColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prizeName,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = result.numbers.joinToString(" - "),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = prizeColor,
                lineHeight = 20.sp
            )
        }

        // Count badge if multiple numbers
        if (result.numbers.size > 1) {
            Badge(
                containerColor = prizeColor.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "${result.numbers.size}",
                    fontSize = 12.sp,
                    color = prizeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatisticsContent(session: LotterySession) {
    // Prize breakdown
    val prizeCount = mapOf(
        "Giải đặc biệt" to session.results.count { it.key == "special" },
        "Giải nhất" to session.results.count { it.key == "first" },
        "Giải nhì" to session.results.count { it.key == "second" },
        "Giải ba" to session.results.count { it.key == "third" },
        "Các giải khác" to session.results.count {
            it.key !in listOf("special", "first", "second", "third")
        }
    ).filter { it.value > 0 }

    prizeCount.forEach { (prizeName, count) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = prizeName,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count giải",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = LotteryGold
            )
        }
    }

    // Number analysis
    val allNumbers = session.results.values.flatMap { it.numbers }
    if (allNumbers.isNotEmpty()) {
        Divider(
            color = Color.White.copy(alpha = 0.1f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Text(
            text = "🔢 PHÂN TÍCH SỐ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = LotteryGold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tổng số ra: ${allNumbers.size}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "Số duy nhất: ${allNumbers.toSet().size}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // Duration if available
        if (session.endTime != null && session.endTime != 0L) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Thời gian thực hiện:",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "${(session.endTime - session.startTime) / 1000} giây",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ActionItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    iconTint: Color = LotteryGold
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ShareDialog(
    session: LotterySession,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Chia sẻ kết quả")
            }
        },
        text = {
            Column {
                Text("Chọn cách chia sẻ kết quả xổ số:")

                Spacer(modifier = Modifier.height(16.dp))

                // Share options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShareOptionButton(
                        icon = Icons.Default.ContentCopy,
                        text = "Sao chép",
                        onClick = {
                            // Copy to clipboard logic
                            copyResultsToClipboard(context, session)
                            onDismiss()
                        }
                    )
                    ShareOptionButton(
                        icon = Icons.Default.Image,
                        text = "Hình ảnh",
                        onClick = {
                            // Share as image logic
                            saveResultsAsImage(context, session)
                            onDismiss()
                        }
                    )
                    ShareOptionButton(
                        icon = Icons.Default.Share,
                        text = "Chia sẻ",
                        onClick = {
                            // Share via other apps logic
                            shareResults(context, session)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun ShareOptionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = text,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}


@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LotteryRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = LotteryRed,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                "⚠️ Xác nhận xóa",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text(
                    "Bạn có chắc chắn muốn xóa phiên quay này không?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LotteryRed.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(
                        "Tất cả dữ liệu bao gồm kết quả xổ số và thông tin phiên sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = LotteryRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onConfirm,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = LotteryRed
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Xóa",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Hủy",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color(0xFF1E1E2E),
        shape = RoundedCornerShape(20.dp)
    )
}

// Utility functions remain the same
private fun shareResults(context: Context, session: LotterySession) {
    val resultsText = formatSessionResults(session)

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, resultsText)
        putExtra(Intent.EXTRA_SUBJECT, "Kết quả xổ số may mắn")
    }

    try {
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ kết quả"))
    } catch (e: Exception) {
        Toast.makeText(context, "Không thể chia sẻ", Toast.LENGTH_SHORT).show()
    }
}

private fun saveResultsAsImage(context: Context, session: LotterySession) {
    try {
        val bitmap = createResultsBitmap(context, session)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveBitmapToGallery(
                context,
                bitmap,
                "lottery_results_${System.currentTimeMillis()}.png"
            )
        } else {
            saveBitmapToGalleryLegacy(
                context,
                bitmap,
                "lottery_results_${System.currentTimeMillis()}.png"
            )
        }

        Toast.makeText(
            context,
            "Đã lưu kết quả dưới dạng hình ảnh vào thư viện",
            Toast.LENGTH_SHORT
        ).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Không thể lưu hình ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun createResultsBitmap(context: Context, session: LotterySession): Bitmap {
    val width = 800
    val height = 1200
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background
    canvas.drawColor(android.graphics.Color.WHITE)

    // Paint objects
    val titlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1976D2")
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    val headerPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#424242")
        textSize = 32f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.LEFT
        isAntiAlias = true
    }

    val textPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#616161")
        textSize = 24f
        textAlign = Paint.Align.LEFT
        isAntiAlias = true
    }

    val numberPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#D32F2F")
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.LEFT
        isAntiAlias = true
    }

    val linePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#E0E0E0")
        strokeWidth = 2f
    }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val dateTime = dateFormatter.format(Date(session.startTime))

    var y = 80f

    // Title
    canvas.drawText("🎲 KẾT QUẢ XỔ SỐ MAY MẮN 🎲", width / 2f, y, titlePaint)
    y += 60f

    // Divider line
    canvas.drawLine(50f, y, width - 50f, y, linePaint)
    y += 40f

    // Date and session info
    canvas.drawText("📅 Thời gian: $dateTime", 50f, y, textPaint)
    y += 40f
    canvas.drawText("🆔 Mã phiên: ${session.id.take(8)}", 50f, y, textPaint)
    y += 60f

    // Another divider
    canvas.drawLine(50f, y, width - 50f, y, linePaint)
    y += 40f

    // Sort prizes for display
    val sortedPrizes = session.results.toList().sortedBy { (prizeId, _) ->
        when (prizeId) {
            "special" -> 0
            "first" -> 1
            "second" -> 2
            "third" -> 3
            "fourth" -> 4
            "fifth" -> 5
            "sixth" -> 6
            "seventh" -> 7
            "eighth" -> 8
            else -> 9
        }
    }

    // Draw prizes
    sortedPrizes.forEach { (prizeId, result) ->
        val prizeName = when (prizeId) {
            "special" -> "🏆 GIẢI ĐẶC BIỆT"
            "first" -> "🥇 GIẢI NHẤT"
            "second" -> "🥈 GIẢI NHÌ"
            "third" -> "🥉 GIẢI BA"
            "fourth" -> "🏅 GIẢI TƯ"
            "fifth" -> "🏅 GIẢI NĂM"
            "sixth" -> "🏅 GIẢI SÁU"
            "seventh" -> "🏅 GIẢI BẢY"
            "eighth" -> "🏅 GIẢI TÁM"
            else -> "🎯 ${result.prize.name}"
        }

        // Prize name
        canvas.drawText(prizeName, 50f, y, headerPaint)
        y += 45f

        // Numbers
        val numbersText = result.numbers.joinToString(" - ")
        canvas.drawText(numbersText, 70f, y, numberPaint)
        y += 50f
    }

    return bitmap
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/LotteryResults"
        )
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
    }
}

private fun saveBitmapToGalleryLegacy(context: Context, bitmap: Bitmap, filename: String) {
    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val lotteryDir = File(picturesDir, "LotteryResults")

    if (!lotteryDir.exists()) {
        lotteryDir.mkdirs()
    }

    val file = File(lotteryDir, filename)
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }

    MediaScannerConnection.scanFile(
        context,
        arrayOf(file.absolutePath),
        arrayOf("image/png"),
        null
    )
}

private fun copyResultsToClipboard(context: Context, session: LotterySession) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val resultsText = formatSessionResults(session)
    val clipData = ClipData.newPlainText("Kết quả xổ số", resultsText)
    clipboardManager.setPrimaryClip(clipData)

    Toast.makeText(context, "Đã sao chép kết quả vào clipboard", Toast.LENGTH_SHORT).show()
}

private fun formatSessionResults(session: LotterySession): String {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val dateTime = dateFormatter.format(Date(session.startTime))

    val builder = StringBuilder()
    builder.append("🎲 KẾT QUẢ XỔ SỐ MAY MẮN 🎲\n")
    builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    builder.append("📅 Thời gian: $dateTime\n")
    builder.append("🆔 Mã phiên: ${session.id.take(8)}\n")
    builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

    val sortedPrizes = session.results.toList().sortedBy { (prizeId, _) ->
        when (prizeId) {
            "special" -> 0
            "first" -> 1
            "second" -> 2
            "third" -> 3
            "fourth" -> 4
            "fifth" -> 5
            "sixth" -> 6
            "seventh" -> 7
            "eighth" -> 8
            else -> 9
        }
    }

    sortedPrizes.forEach { (prizeId, result) ->
        val prizeName = when (prizeId) {
            "special" -> "🏆 GIẢI ĐẶC BIỆT"
            "first" -> "🥇 GIẢI NHẤT"
            "second" -> "🥈 GIẢI NHÌ"
            "third" -> "🥉 GIẢI BA"
            "fourth" -> "🏅 GIẢI TƯ"
            "fifth" -> "🏅 GIẢI NĂM"
            "sixth" -> "🏅 GIẢI SÁU"
            "seventh" -> "🏅 GIẢI BẢY"
            "eighth" -> "🏅 GIẢI TÁM"
            else -> "🎯 ${result.prize.name}"
        }

        builder.append("$prizeName\n")
        builder.append("${result.numbers.joinToString(" - ")}\n\n")
    }

    builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    builder.append("📊 THỐNG KÊ:\n")
    builder.append("• Tổng số giải: ${session.results.size}\n")
    builder.append("• Tổng số ra: ${session.results.values.sumOf { it.numbers.size }}\n")
    if (session.endTime != null && session.endTime != 0L) {
        builder.append("• Thời gian thực hiện: ${(session.endTime - session.startTime) / 1000}s\n")
    }
    builder.append("• Trạng thái: ${if (session.isCompleted) "Hoàn tất ✅" else "Chưa hoàn tất ⏳"}\n")
    builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    builder.append("🎮 Tạo bởi: TravisHuy\n")
    builder.append("📱 App: Random Lucky\n")
    builder.append("⏰ Chia sẻ lúc: ${dateFormatter.format(Date())}")

    return builder.toString()
}