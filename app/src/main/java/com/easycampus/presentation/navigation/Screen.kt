package com.easycampus.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Schedule : Screen("schedule")
    data object Settings : Screen("settings")
    data object Login : Screen("login")
    data object CheckIn : Screen("checkin")
    data object CourseDetail : Screen("course/{courseId}") {
        fun createRoute(courseId: String) = "course/$courseId"
    }
    data object AccountManagement : Screen("accounts")
}
