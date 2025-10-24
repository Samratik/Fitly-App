package com.example.fitinmotion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate

enum class ExerciseType { PUSHUPS, SQUATS }

data class DailyTask(
    val type: ExerciseType,
    val targetReps: Int,
    val done: Boolean = false
)

class AppStateViewModel : ViewModel() {
    var coins by mutableStateOf(0)
        private set
    var streak by mutableStateOf(0)
        private set
    var lastDoneDate: LocalDate? = null
        private set

    var todayTask by mutableStateOf<DailyTask?>(null)
        private set

    fun setTask(type: ExerciseType, target: Int) {
        todayTask = DailyTask(type, target, done = false)
    }

    fun completeTask() {
        todayTask = todayTask?.copy(done = true)
        coins += 10
        val today = LocalDate.now()
        streak = when (lastDoneDate) {
            null -> 1
            today.minusDays(1) -> streak + 1
            today -> streak
            else -> 1
        }
        lastDoneDate = today
    }

    fun resetTask() { todayTask = null }
}