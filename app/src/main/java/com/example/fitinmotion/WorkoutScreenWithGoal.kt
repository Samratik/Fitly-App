package com.example.fitinmotion

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import com.example.fitinmotion.PushUpCounterMP

@Composable
fun WorkoutScreenWithGoal(
    type: ExerciseType,
    target: Int,
    onCompleted: () -> Unit,
    onExit: () -> Unit
) {
    var reps by remember { mutableStateOf(0) }
    var showCongrats by remember { mutableStateOf(false) }

    val counter = remember(type) {
        when (type) {
            ExerciseType.PUSHUPS -> PushUpCounterMP()
            ExerciseType.SQUATS  -> SquatCounterMP()
        }
    }

    WorkoutScreenCore(
        exerciseType = type,
        currentReps = reps,
        onRepsChange = { newReps ->
            reps = newReps
            if (reps >= target && !showCongrats) showCongrats = true
        },
        counter = counter,
        onBack = onExit
    )

    if (showCongrats) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Готово!") },
            text = { Text("Ты выполнил цель: $target ${if (type==ExerciseType.PUSHUPS) "отжиманий" else "повторов"}") },
            confirmButton = {
                Button(onClick = {
                    showCongrats = false
                    onCompleted()
                }) { Text("На главную") }
            }
        )
    }
}