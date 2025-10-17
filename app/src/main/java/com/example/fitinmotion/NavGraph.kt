package com.example.fitinmotion

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

enum class Route { Home, Workout }

@Composable
fun AppNavHost(nav: NavHostController) {
    NavHost(navController = nav, startDestination = Route.Home.name) {
        composable(Route.Home.name) { HomeScreen(onStartClick = { nav.navigate(Route.Workout.name) }) }
        composable(Route.Workout.name) { WorkoutScreen() }
    }
}
