package com.example.workertracking.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Dashboard,
        icon = Icons.Default.Home,
        selectedIcon = Icons.Default.Home
    ),
    BottomNavItem(
        screen = Screen.Projects,
        icon = Icons.Default.Build,
        selectedIcon = Icons.Default.Build
    ),
    BottomNavItem(
        screen = Screen.Workers,
        icon = Icons.Default.Person,
        selectedIcon = Icons.Default.Person
    ),
    BottomNavItem(
        screen = Screen.Events,
        icon = Icons.Default.DateRange,
        selectedIcon = Icons.Default.DateRange
    ),
    BottomNavItem(
        screen = Screen.Employers,
        icon = Icons.Default.AccountCircle,
        selectedIcon = Icons.Default.AccountCircle
    )
)