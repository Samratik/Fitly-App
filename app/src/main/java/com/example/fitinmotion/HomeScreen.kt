package com.example.fitinmotion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onStartClick: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Fit in Motion") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Добро пожаловать! Нажми Старт чтобы открыть камеру.")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onStartClick) { Text("Старт") }
        }
    }
}
