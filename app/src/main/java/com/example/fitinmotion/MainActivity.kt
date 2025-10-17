package com.example.fitinmotion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.fitinmotion.ui.theme.FitInMotionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitInMotionTheme {
                val nav = rememberNavController()
                AppNavHost(nav = nav)
            }
        }
    }
}
