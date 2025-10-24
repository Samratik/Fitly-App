package com.example.fitinmotion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.fitinmotion.ui.theme.FitInMotionTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitInMotionTheme {
                val vm: AppStateViewModel = viewModel()
                val nav = rememberNavController()
                Scaffold(bottomBar = { BottomBar(nav) }) { pad ->
                    AppNavGraph(nav = nav, vm = vm, modifier = Modifier.padding(pad))
                }
            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController) {
    val items = listOf(Route.Home, Route.Exercises, Route.Profile)
    val current by nav.currentBackStackEntryAsState()
    val currentRoute = current?.destination?.route
    NavigationBar {
        items.forEach { r ->
            NavigationBarItem(
                selected = currentRoute?.startsWith(r.route) == true,
                onClick = {
                    nav.navigate(r.route) {
                        popUpTo(Route.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(r.icon, contentDescription = r.label) },
                label = { Text(r.label) }
            )
        }
    }
}
