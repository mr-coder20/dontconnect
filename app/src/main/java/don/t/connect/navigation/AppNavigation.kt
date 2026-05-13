// navigation/AppNavigation.kt
package don.t.connect.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import don.t.connect.screens.FakeVpnScreen
import don.t.connect.screens.InflationScreen
import don.t.connect.screens.SettingsScreen
import don.t.connect.viewmodel.FakeVpnViewModel
import don.t.connect.viewmodel.InflationViewModel
import don.t.connect.viewmodel.SettingsViewModel

// تعریف مقصدهای ناوبری (عنوان حذف شد)
sealed class Screen(val route: String, val icon: ImageVector) {
    object Inflation : Screen("inflation", Icons.Default.TrendingUp)
    object FakeVpn : Screen("fakevpn", Icons.Default.VpnKey)
    object Settings : Screen("settings", Icons.Default.Settings)
}

@Composable
fun AppNavigation(
    inflationViewModel: InflationViewModel,
    fakeVpnViewModel: FakeVpnViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Inflation, Screen.FakeVpn, Screen.Settings)
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val configuration = LocalConfiguration.current
    val isEnglish = configuration.locales[0]?.language == "en"

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                items.forEach { screen ->
                    // عنوان پویا بر اساس زبان جاری
                    val title = when (screen) {
                        Screen.Inflation -> if (isEnglish) "Inflation" else "تورم"
                        Screen.FakeVpn -> if (isEnglish) "Fake VPN" else "فیلترنشکن"
                        Screen.Settings -> if (isEnglish) "Settings" else "تنظیمات"
                    }
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Inflation.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Inflation.route) {
                InflationScreen(viewModel = inflationViewModel)
            }
            composable(Screen.FakeVpn.route) {
                FakeVpnScreen(
                    viewModel = fakeVpnViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(settingsViewModel = settingsViewModel)
            }
        }
    }
}