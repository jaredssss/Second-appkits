package com.globalcurrency.converter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.globalcurrency.converter.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Converter   : Screen("converter",   "Convert",    Icons.Default.CurrencyExchange)
    object Calculator  : Screen("calculator",  "Calculator", Icons.Default.Calculate)
    object Rates       : Screen("rates",       "Rates",      Icons.Default.TableChart)
    object Premium     : Screen("premium",     "Premium",    Icons.Default.Stars)
}

private val BOTTOM_NAV_ITEMS = listOf(
    Screen.Converter,
    Screen.Calculator,
    Screen.Rates,
    Screen.Premium
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor   = OnDarkBackground
            ) {
                BOTTOM_NAV_ITEMS.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                tint = if (selected) GoldPrimary else OnDarkDisabled
                            )
                        },
                        label = {
                            Text(
                                text  = screen.label,
                                color = if (selected) GoldPrimary else OnDarkDisabled,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = GoldPrimary,
                            unselectedIconColor = OnDarkDisabled,
                            indicatorColor      = DarkSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController     = navController,
            startDestination  = Screen.Converter.route,
            modifier          = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Converter.route)  { ConverterScreen(navController) }
            composable(Screen.Calculator.route) { CalculatorScreen(navController) }
            composable(Screen.Rates.route)      { RatesScreen(navController) }
            composable(Screen.Premium.route)    { PremiumScreen() }
        }
    }
}
