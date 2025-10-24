package com.example.fitinmotion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun HomeScreen(
    coins: Int,
    streak: Int,
    task: DailyTask?,
    onOpenExercises: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Коины", coins.toString(), Modifier.weight(1f))
            StatCard("Стрик", streak.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Text("Сегодняшняя задача", style = MaterialTheme.typography.titleMedium)
        if (task == null) {
            AssistChip(onClick = onOpenExercises, label = { Text("Выбрать упражнение") })
        } else {
            val title = when (task.type) { ExerciseType.PUSHUPS -> "Отжимания"; ExerciseType.SQUATS -> "Приседания" }
            val status = if (task.done) "✅ Выполнено" else "⏳ ${task.targetReps} раз"
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("$title — $status", modifier = Modifier.weight(1f))
                    if (!task.done) Button(onClick = onOpenExercises) { Text("Изменить") }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
