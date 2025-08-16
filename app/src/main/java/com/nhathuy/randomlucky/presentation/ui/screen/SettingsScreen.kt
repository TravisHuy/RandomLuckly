package com.nhathuy.randomlucky.presentation.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhathuy.randomlucky.R
import com.nhathuy.randomlucky.data.sound.SettingManager
import com.nhathuy.randomlucky.presentation.theme.*
import com.nhathuy.randomlucky.presentation.viewmodel.HistoryViewModel
import com.nhathuy.randomlucky.presentation.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit
import com.nhathuy.randomlucky.presentation.viewmodel.LotteryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    historyViewModel: HistoryViewModel = hiltViewModel(),
    lotteryViewModel: LotteryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    settingsManager: SettingManager = hiltViewModel<SettingsViewModel>().settingsManager
) {
    val context = LocalContext.current

    var showAboutDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val soundEnabled by settingsManager.isSoundEnabled.collectAsState()
    val darkModeEnabled by settingsManager.isDarkModeEnabled.collectAsState()
    val vibrationEnabled by settingsManager.isVibrationEnabled.collectAsState()

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
            // Enhanced Header
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Cài đặt",
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
                            .clip(RoundedCornerShape(10.dp))
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )

            // Content với spacing đẹp hơn
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Game Settings với gradient card
                item {
                    SettingsSection(
                        title = "🎮 Cài đặt trò chơi",
                        gradient = Brush.horizontalGradient(
                            colors = listOf(
                                LotteryDarkBlue.copy(alpha = 0.3f),
                                LotteryPurple.copy(alpha = 0.2f)
                            )
                        )
                    ) {
                        SettingsSwitchItem(
                            title = "Âm thanh",
                            description = "Bật/tắt hiệu ứng âm thanh trong game",
                            icon = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            checked = soundEnabled,
                            onCheckedChange = { newValue ->
                                // ✅ Update settings through SettingsManager
                                settingsManager.setSoundEnabled(newValue)

//                                Toast.makeText(
//                                    context,
//                                    if (newValue) "🔊 Âm thanh đã được bật" else "🔇 Âm thanh đã được tắt",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                            }
                        )

                        Divider(
                            color = Color.White.copy(alpha = 0.1f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        SettingsSwitchItem(
                            title = "Rung",
                            description = "Bật/tắt rung khi có kết quả",
                            icon = if (vibrationEnabled) Icons.Default.Vibration else Icons.Default.PhoneAndroid,
                            checked = vibrationEnabled,
                            onCheckedChange = { newValue ->
                                settingsManager.setVibrationEnabled(newValue)

//                                // Test vibration when enabling
//                                if (newValue) {
//                                    settingsViewModel.toggleVibration()
//                                }
                            }
                        )

//                        Divider(
//                            color = Color.White.copy(alpha = 0.1f),
//                            thickness = 0.5.dp,
//                            modifier = Modifier.padding(vertical = 12.dp)
//                        )
//
//                        SettingsSwitchItem(
//                            title = "Chế độ tối",
//                            description = "Sử dụng giao diện tối cho ứng dụng",
//                            icon = if (darkModeEnabled) Icons.Default.DarkMode else Icons.Default.LightMode,
//                            checked = darkModeEnabled,
//                            onCheckedChange = { newValue ->
//                                // ✅ Update settings through SettingsManager
//                                settingsManager.setDarkModeEnabled(newValue)
//
////                                Toast.makeText(
////                                    context,
////                                    if (newValue) "🌙 Chế độ tối đã được bật" else "☀️ Chế độ sáng đã được bật",
////                                    Toast.LENGTH_SHORT
////                                ).show()
//                            }
//                        )

                        Divider(
                            color = Color.White.copy(alpha = 0.1f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        SettingsClickableItem(
                            title = "Đặt lại ứng dụng",
                            description = "Xóa tất cả dữ liệu và cài đặt",
                            icon = Icons.Default.RestartAlt,
                            onClick = { showResetDialog = true },
                            iconTint = LotteryRed
                        )
                    }
                }

                // App Info & Support với gradient khác
                item {
                    SettingsSection(
                        title = "ℹ️ Thông tin & hỗ trợ",
                        gradient = Brush.horizontalGradient(
                            colors = listOf(
                                LotteryGreen.copy(alpha = 0.3f),
                                LotteryOrange.copy(alpha = 0.2f)
                            )
                        )
                    ) {
                        SettingsClickableItem(
                            title = "Về ứng dụng",
                            description = "Thông tin chi tiết về Random Lucky v1.0.0",
                            icon = Icons.Default.Info,
                            onClick = { showAboutDialog = true },
                            iconTint = LotteryLightBlue
                        )

                        Divider(
                            color = Color.White.copy(alpha = 0.1f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        SettingsClickableItem(
                            title = "Đánh giá ứng dụng",
                            description = "Đánh giá 5 sao trên Google Play Store",
                            icon = Icons.Default.Star,
                            onClick = { handleRateApp(context) },
                            iconTint = LotteryGold
                        )

                        Divider(
                            color = Color.White.copy(alpha = 0.1f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        SettingsClickableItem(
                            title = "Chia sẻ ứng dụng",
                            description = "Giới thiệu Random Lucky với bạn bè",
                            icon = Icons.Default.Share,
                            onClick = { handleShareApp(context) },
                            iconTint = LotteryGreen
                        )

                        Divider(
                            color = Color.White.copy(alpha = 0.1f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        SettingsClickableItem(
                            title = "Liên hệ hỗ trợ",
                            description = "Gửi phản hồi và báo lỗi cho nhà phát triển",
                            icon = Icons.Default.Email,
                            onClick = { handleContactSupport(context) },
                            iconTint = LotteryPurple
                        )
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    // Enhanced Dialogs
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showResetDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                handleResetApp(context, historyViewModel, lotteryViewModel, settingsManager)
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }
}

// xu ly danh gia ung dung
private fun handleRateApp(context: Context) {
    try {
        // Try to open Play Store app first
        val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=${context.packageName}")
            setPackage("com.android.vending")
        }

        if (playStoreIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(playStoreIntent)
        } else {
            // Fallback to web browser
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data =
                    Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            }
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Không thể mở Play Store", Toast.LENGTH_SHORT).show()
    }
}

// xử lý đánh giá ứng dụng
private fun handleShareApp(context: Context) {
    val shareText = """
        🎲 Random Lucky - Ứng dụng xổ số may mắn hàng đầu việt nam! 🎲
        
        🌟 Tính năng nổi bật:
        • Xổ số ngẫu nhiên với nhiều giải thưởng
        • Giao diện đẹp mắt và dễ sử dụng
        • Lưu lịch sử kết quả chi tiết
        • Hoàn toàn miễn phí
        
        📱 Tải ngay tại: https://play.google.com/store/apps/details?id=${context.packageName}
        
        #RandomLucky #XoSo #MayMan #TravisHuy
    """.trimIndent()

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "🎲 Random Lucky - Ứng dụng xổ số may mắn!")
    }
    try {
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                "Chia sẻ Random Lucky - Xổ Số May Mắn"
            )
        )
    } catch (e: Exception) {
        Toast.makeText(context, "Không thể chia sẻ", Toast.LENGTH_SHORT).show()
    }
}

private fun handleResetApp(
    context: Context,
    historyViewModel: HistoryViewModel,
    lotteryViewModel: LotteryViewModel,
    settingsManager: SettingManager
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            // 1. Reset lottery trước
            lotteryViewModel.resetForAppReset()

            // 2. Clear history
            historyViewModel.clearAllHistory()

            // 3. Reset settings về default values (không chỉ clear)
            settingsManager.resetToDefaults()

            // 4. Clear các shared preferences khác
            val sharedPrefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            sharedPrefs.edit {
                clear()
                apply()
            }

            // 5. Clear cache
            try {
                val cacheDir = context.cacheDir
                cacheDir.deleteRecursively()
            } catch (e: Exception) {
                // Ignore cache clear errors
            }

            // 6. Đợi để đảm bảo tất cả operations hoàn thành
            delay(500)

            Toast.makeText(
                context,
                "Đã đặt lại ứng dụng thành công!\nTất cả dữ liệu đã được xóa.\"",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Có lỗi xảy ra khi đặt lại: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

private fun handleContactSupport(context: Context) {
    val supportEmail = context.getString(R.string.email_dev)
    val subject = context.getString(R.string.email_subject)
    val body = """
        Xin chào TravisHuy,

        Tôi muốn gửi phản hồi về ứng dụng Random Lucky - Xổ Số May Mắn:

        📱 Thông tin thiết bị:
        • Phiên bản app: 1.0.0
        • Hệ điều hành:   Android ${android.os.Build.VERSION.RELEASE}
        • Thời gian: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}

         💬 Nội dung phản hồi:
        [Vui lòng viết phản hồi của bạn ở đây]

        Cảm ơn bạn đã phát triển ứng dụng tuyệt vời này!

        Trân trọng,
        Người dùng Random Lucky
    """.trimIndent()

    try {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822" // Chỉ định loại email
            putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        // Kiểm tra xem có ứng dụng email nào không
        if (emailIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(emailIntent, "Chọn ứng dụng email"))
            return
        }
    } catch (e: Exception) {
        // Log lỗi để debug
        android.util.Log.e("EmailError", "ACTION_SEND failed: ${e.message}")
    }
}
private fun copyToClipboard(context: Context, email: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Email", email))

    Toast.makeText(
        context,
        "📧 Đã copy email: $email",
        Toast.LENGTH_SHORT
    ).show()
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
private fun SettingsSwitchItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
                .background(
                    if (checked) LotteryGold.copy(alpha = 0.2f)
                    else Color.White.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) LotteryGold else Color.White.copy(alpha = 0.7f),
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

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = LotteryGold,
                uncheckedThumbColor = Color.White.copy(alpha = 0.8f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SettingsClickableItem(
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
private fun ResetConfirmationDialog(
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
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = LotteryRed,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                "⚠️ Đặt lại ứng dụng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text(
                    "Bạn có chắc chắn muốn đặt lại ứng dụng không?",
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
                        "Tất cả dữ liệu bao gồm lịch sử xổ số và cài đặt sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = LotteryRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LotteryRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Đặt lại",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
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
        containerColor = Color(0xFFE3E2E2),
        shape = RoundedCornerShape(20.dp)
    )
}
