package app.tabit.tracker.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.tabit.tracker.feature.charts.ChartsScreen
import app.tabit.tracker.feature.habitform.HabitFormScreen
import app.tabit.tracker.feature.onboarding.OnboardingScreen
import app.tabit.tracker.feature.settings.SettingsScreen
import app.tabit.tracker.feature.table.TableScreen
import app.tabit.tracker.feature.today.TodayScreen

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Table : Screen("table")
    data object Today : Screen("today")
    data object Charts : Screen("charts")
    data object Settings : Screen("settings")
    data object HabitForm : Screen("habit_form")
    data object HabitEdit : Screen("habit_edit/{habitId}") {
        fun createRoute(habitId: Long) = "habit_edit/$habitId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Table.route,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300)) { it / 3 } },
        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { -it / 3 } }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = { navController.navigate(Screen.Table.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } } })
        }
        composable(Screen.Table.route) {
            TableScreen(onHabitClick = { habitId -> navController.navigate(Screen.HabitEdit.createRoute(habitId)) }, onAddHabit = { navController.navigate(Screen.HabitForm.route) })
        }
        composable(Screen.Today.route) {
            TodayScreen(onHabitClick = { habitId -> navController.navigate(Screen.HabitEdit.createRoute(habitId)) })
        }
        composable(Screen.Charts.route) { ChartsScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.HabitForm.route) {
            HabitFormScreen(onSaved = { navController.popBackStack() }, onCancelled = { navController.popBackStack() })
        }
        composable(Screen.HabitEdit.route, arguments = listOf(navArgument("habitId") { type = NavType.LongType })) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: 0L
            HabitFormScreen(habitId = habitId, onSaved = { navController.popBackStack() }, onCancelled = { navController.popBackStack() })
        }
    }
}
