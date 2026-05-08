package com.jaymin.smartconnect.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jaymin.smartconnect.ui.screens.devices.DevicesScreen
import com.jaymin.smartconnect.ui.screens.history.HistoryScreen
import com.jaymin.smartconnect.ui.screens.nfc.NfcScreen
import com.jaymin.smartconnect.ui.screens.scanner.ScannerScreen

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Scanner : Screen("scanner", "Scan", Icons.Filled.Bluetooth, Icons.Outlined.Bluetooth)
    data object Devices : Screen("devices", "Paired", Icons.Filled.Devices, Icons.Outlined.Devices)
    data object Nfc : Screen("nfc", "NFC", Icons.Filled.Nfc, Icons.Outlined.Nfc)
    data object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
}

val bottomItems = listOf(Screen.Scanner, Screen.Devices, Screen.Nfc, Screen.History)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Scanner.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Scanner.route) { ScannerScreen() }
            composable(Screen.Devices.route) { DevicesScreen() }
            composable(Screen.Nfc.route) { NfcScreen() }
            composable(Screen.History.route) { HistoryScreen() }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination

    NavigationBar {
        bottomItems.forEach { screen ->
            val selected = current?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
