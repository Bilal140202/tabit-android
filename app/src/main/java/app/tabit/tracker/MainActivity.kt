package app.tabit.tracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.tabit.tracker.core.theme.TabitTheme
import app.tabit.tracker.navigation.AppNavigation
import app.tabit.tracker.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            Log.w("Tabit", "enableEdgeToEdge failed", e)
        }
        setContent {
            TabitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TabitApp()
                }
            }
        }
    }
}

@Composable
fun TabitApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.Table.route, Screen.Today.route, Screen.Charts.route, Screen.Settings.route
    )
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Table.route,
                        onClick = { navController.navigate(Screen.Table.route) { popUpTo(Screen.Table.route) { inclusive = true } } },
                        icon = { Icon(Icons.Default.Dashboard, "Table") },
                        label = { Text("Table") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Today.route,
                        onClick = { navController.navigate(Screen.Today.route) },
                        icon = { Icon(Icons.Default.CheckCircle, "Today") },
                        label = { Text("Today") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Charts.route,
                        onClick = { navController.navigate(Screen.Charts.route) },
                        icon = { Icon(Icons.Default.BarChart, "Charts") },
                        label = { Text("Charts") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = { navController.navigate(Screen.Settings.route) },
                        icon = { Icon(Icons.Default.Settings, "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { paddingValues ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
