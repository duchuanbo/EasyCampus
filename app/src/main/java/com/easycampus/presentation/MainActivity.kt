package com.easycampus.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.easycampus.presentation.navigation.Screen
import com.easycampus.presentation.screens.home.HomeScreen
import com.easycampus.presentation.screens.login.LoginScreen
import com.easycampus.presentation.screens.schedule.ScheduleScreen
import com.easycampus.presentation.screens.settings.SettingsScreen
import com.easycampus.presentation.theme.EasyCampusTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        setContent {
            EasyCampusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EasyCampusApp()
                }
            }
        }
    }
}

@Composable
fun EasyCampusApp() {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSchedule = { navController.navigate(Screen.Schedule.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLoginSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}
