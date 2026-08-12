package app.tabit.tracker.navigation

import android.content.Context
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

// DataStore instance
private val Context.dataStore by preferencesDataStore(name = "tabit_settings")

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Fix C4: Check if onboarding was completed, with error fallback
    LaunchedEffect(Unit) {
        startDestination = try {
            val onboarded = context.dataStore.data.map { prefs ->
                prefs[booleanPreferencesKey("onboarding_completed")] ?: false
            }.first()
            if (onboarded) Screen.Table.route else Screen.Onboarding.route
        } catch (_: Exception) {
            Screen.Table.route // fallback to main screen on any DataStore error
        }
    }

    if (startDestination == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300)) { it / 3 } },
        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { -it / 3 } }
    ) {
        composable(Screen.Onboarding.route) {
            val onboardingScope = rememberCoroutineScope()
            OnboardingScreen(
                onFinish = {
                    onboardingScope.launch {
                        context.dataStore.edit { it[booleanPreferencesKey("onboarding_completed")] = true }
                    }
                    navController.navigate(Screen.Table.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Table.route) {
            TableScreen(
                onHabitClick = { habitId -> navController.navigate(Screen.HabitEdit.createRoute(habitId)) },
                onAddHabit = { navController.navigate(Screen.HabitForm.route) }
            )
        }
        composable(Screen.Today.route) {
            TodayScreen(onHabitClick = { habitId -> navController.navigate(Screen.HabitEdit.createRoute(habitId)) })
        }
        composable(Screen.Charts.route) { ChartsScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.HabitForm.route) {
            HabitFormScreen(onSaved = { navController.popBackStack() }, onCancelled = { navController.popBackStack() })
        }
        composable(
            Screen.HabitEdit.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: 0L
            HabitFormScreen(habitId = habitId, onSaved = { navController.popBackStack() }, onCancelled = { navController.popBackStack() })
        }
    }
}

private fun booleanPreferencesKey(name: String) = androidx.datastore.preferences.core.booleanPreferencesKey(name)
