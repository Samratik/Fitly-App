package com.example.fitinmotion

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person

sealed class Route(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Route("home", "Главная", Icons.Filled.Home)
    data object Exercises : Route("exercises", "Упражнения", Icons.Filled.FitnessCenter)
    data object Profile : Route("profile", "Профиль", Icons.Filled.Person)

    data object Workout : Route("workout/{type}/{target}", "Тренировка", Icons.Filled.FitnessCenter) {
        fun path(type: com.example.fitinmotion.ExerciseType, target: Int) = "workout/${type.name}/$target"
    }
}