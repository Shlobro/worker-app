package com.example.workertracking

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.workertracking.ui.navigation.Screen
import com.example.workertracking.ui.navigation.bottomNavItems
import com.example.workertracking.ui.navigation.dashboardNavigation
import com.example.workertracking.ui.navigation.employersNavigation
import com.example.workertracking.ui.navigation.eventsNavigation
import com.example.workertracking.ui.navigation.projectsNavigation
import com.example.workertracking.ui.navigation.shiftsNavigation
import com.example.workertracking.ui.navigation.workersNavigation
import com.example.workertracking.ui.theme.WorkerTrackingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Debug logging
        Log.d("MainActivity", "Current locale: ${resources.configuration.locales[0]}")
        Log.d("MainActivity", "App name from resources: ${getString(R.string.app_name)}")
        Log.d("MainActivity", "Dashboard title: ${getString(R.string.nav_dashboard)}")

        enableEdgeToEdge()
        setContent {
            WorkerTrackingTheme {
                WorkerTrackingApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerTrackingApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as WorkerTrackingApplication

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute = currentDestination?.route

            // Only show bottom bar on main screens
            if (currentRoute in listOf(
                Screen.Dashboard.route,
                Screen.Projects.route,
                Screen.Workers.route,
                Screen.Events.route,
                Screen.Employers.route
            )) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination?.hierarchy?.any { it.route == item.screen.route } == true) {
                                        item.selectedIcon
                                    } else {
                                        item.icon
                                    },
                                    contentDescription = stringResource(item.screen.titleRes)
                                )
                            },
                            label = { Text(stringResource(item.screen.titleRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            dashboardNavigation(navController, application)
            projectsNavigation(navController, application)
            workersNavigation(navController, application)
            eventsNavigation(navController, application)
            shiftsNavigation(navController, application)
            employersNavigation(navController, application)
        }
    }
}
