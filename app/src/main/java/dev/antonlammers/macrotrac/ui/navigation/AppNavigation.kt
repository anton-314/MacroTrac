package dev.antonlammers.macrotrac.ui.navigation

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.antonlammers.macrotrac.ui.addfood.AddFoodScreen
import dev.antonlammers.macrotrac.ui.addfood.BarcodeScannerScreen
import dev.antonlammers.macrotrac.ui.goals.GoalsScreen
import dev.antonlammers.macrotrac.ui.overview.OverviewScreen
import dev.antonlammers.macrotrac.ui.stats.StatsScreen
import java.time.LocalDate

sealed class Screen(val route: String) {
    object Overview : Screen("overview")
    object AddFood : Screen("add_food/{date}") {
        fun withDate(date: LocalDate) = "add_food/$date"
    }
    object Goals : Screen("goals")
    object BarcodeScanner : Screen("barcode_scanner")
    object Stats : Screen("stats")
}

private data class BottomNavItem(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Overview, "Übersicht", Icons.Rounded.Home),
    BottomNavItem(Screen.Goals, "Ziele", Icons.Rounded.Flag),
    BottomNavItem(Screen.Stats, "Statistik", Icons.Rounded.BarChart),
)

@ExperimentalGetImage
@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomNav = bottomNavItems.any { it.screen.route == currentRoute }

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Overview.route,
            modifier = Modifier.weight(1f),
        ) {
            composable(Screen.Overview.route) { OverviewScreen(navController) }
            composable(
                route = Screen.AddFood.route,
                arguments = listOf(navArgument("date") { type = NavType.StringType }),
            ) { AddFoodScreen(navController) }
            composable(Screen.Goals.route) { GoalsScreen(navController) }
            composable(Screen.BarcodeScanner.route) { BarcodeScannerScreen(navController) }
            composable(Screen.Stats.route) { StatsScreen(navController) }
        }
        if (showBottomNav) {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        }
    }
}
