package com.example.fitinmotion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExercisesScreen(
    onStart: (ExerciseType, Int) -> Unit
) {
    var type by remember { mutableStateOf(ExerciseType.PUSHUPS) }
    var target by remember { mutableStateOf(10) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Выбери режим", style = MaterialTheme.typography.titleMedium)
        SegmentedButtons(type) { type = it }

        Text("Цель по повторам", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5,10,15,20,30).forEach { n ->
                FilterChip(
                    selected = target == n,
                    onClick = { target = n },
                    label = { Text("$n") }
                )
            }
        }

        Spacer(Modifier.weight(1f))
        Button(onClick = { onStart(type, target) }, modifier = Modifier.fillMaxWidth()) {
            Text("Начать")
        }
    }
}

@Composable
private fun SegmentedButtons(selected: ExerciseType, onSelect: (ExerciseType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SegBtn("Отжимания", selected == ExerciseType.PUSHUPS) { onSelect(ExerciseType.PUSHUPS) }
        SegBtn("Приседания", selected == ExerciseType.SQUATS) { onSelect(ExerciseType.SQUATS) }
    }
}

@Composable
private fun SegBtn(text: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick) { Text(text) }
    }
}

@Composable
private fun FilterChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit) {
    if (selected) Button(onClick = onClick) { label() }
    else OutlinedButton(onClick = onClick) { label() }
}