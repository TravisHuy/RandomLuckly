package com.nhathuy.randomlucky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nhathuy.randomlucky.presentation.theme.RandomLuckyTheme
import com.nhathuy.randomlucky.presentation.ui.screen.DetailLotteryScreen
import com.nhathuy.randomlucky.presentation.ui.screen.HistoryScreen
import com.nhathuy.randomlucky.presentation.ui.screen.LotteryScreen
import com.nhathuy.randomlucky.presentation.ui.screen.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            RandomLuckyTheme {
                RandomLuckyApp()
            }
        }
    }
}

@Composable
fun RandomLuckyApp(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "lottery"
    ){
        composable("lottery") {
            LotteryScreen(
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("history") {
            HistoryScreen(onNavigateBack = {navController.popBackStack()},
                onNavigateToDetail = {
                        sessionId ->
                    navController.navigate("detail/$sessionId")
                })
        }
        composable("settings") {
            SettingsScreen(onNavigateBack = {navController.popBackStack()})
        }

        composable("detail/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            DetailLotteryScreen(
                sessionId = sessionId,
                onNavigateBack = {navController.popBackStack()}
            )
        }
    }
}




@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RandomLuckyTheme {
        RandomLuckyApp()
    }
}