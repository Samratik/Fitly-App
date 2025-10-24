package com.example.fitinmotion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(coins: Int, streak: Int) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Профиль", style = MaterialTheme.typography.titleLarge)
        ElevatedCard { Column(Modifier.padding(16.dp)) {
            Text("Коины: $coins")
            Text("Стрик: $streak")
        } }
    }
}