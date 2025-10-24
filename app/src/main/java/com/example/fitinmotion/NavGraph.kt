package com.example.fitinmotion

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.navArgument

@Composable
fun AppNavGraph(
    nav: NavHostController,
    vm: AppStateViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(navController = nav, startDestination = Route.Home.route, modifier = modifier) {

        composable(Route.Home.route) {
            HomeScreen(
                coins = vm.coins,
                streak = vm.streak,
                task = vm.todayTask,
                onOpenExercises = { nav.navigate(Route.Exercises.route) }
            )
        }

        composable(Route.Exercises.route) {
            ExercisesScreen(
                onStart = { type, target ->
                    vm.setTask(type, target)
                    nav.navigate(Route.Workout.path(type, target))
                }
            )
        }

        composable(
            Route.Workout.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("target") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val type = ExerciseType.valueOf(backStackEntry.arguments!!.getString("type")!!)
            val target = backStackEntry.arguments!!.getInt("target")
            WorkoutScreenWithGoal(
                type = type,
                target = target,
                onCompleted = {
                    vm.completeTask()
                    nav.popBackStack(Route.Home.route, inclusive = false)
                },
                onExit = { nav.popBackStack() }
            )
        }

        composable(Route.Profile.route) {
            ProfileScreen(coins = vm.coins, streak = vm.streak)
        }
    }
}
