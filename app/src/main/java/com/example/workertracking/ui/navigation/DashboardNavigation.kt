package com.example.workertracking.ui.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.workertracking.WorkerTrackingApplication
import com.example.workertracking.ui.screens.MoneyOwedScreen
import com.example.workertracking.ui.screens.RevenueDetailScreen
import com.example.workertracking.ui.screens.dashboard.DashboardScreen
import com.example.workertracking.ui.viewmodel.DashboardViewModel
import com.example.workertracking.ui.viewmodel.MoneyOwedViewModel
import com.example.workertracking.ui.viewmodel.RevenueDetailViewModel

fun NavGraphBuilder.dashboardNavigation(
    navController: NavHostController,
    application: WorkerTrackingApplication
) {
    composable(Screen.Dashboard.route) {
        val viewModel: DashboardViewModel = viewModel {
            DashboardViewModel(
                application.container.projectRepository,
                application.container.shiftRepository,
                application.container.eventRepository,
                application.container.workerRepository,
                application.container
            )
        }

        DashboardScreen(
            viewModel = viewModel,
            onProjectClick = { projectId ->
                navController.navigate(Screen.ProjectDetail.createRoute(projectId))
            },
            onEventClick = { eventId ->
                navController.navigate(Screen.EventDetail.createRoute(eventId))
            },
            onViewAllProjects = {
                navController.navigate(Screen.Projects.route)
            },
            onViewAllEvents = {
                navController.navigate(Screen.Events.route)
            },
            onMoneyOwedClick = {
                navController.navigate(Screen.MoneyOwed.route)
            },
            onRevenueClick = {
                navController.navigate(Screen.RevenueDetail.route)
            }
        )
    }

    composable(Screen.MoneyOwed.route) {
        val viewModel: MoneyOwedViewModel = viewModel {
            MoneyOwedViewModel.Factory(application.container.workerRepository).create(MoneyOwedViewModel::class.java)
        }

        MoneyOwedScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onWorkerClick = { workerId ->
                navController.navigate(Screen.WorkerDetail.createRoute(workerId))
            },
            viewModel = viewModel
        )
    }

    composable(Screen.RevenueDetail.route) {
        val viewModel: RevenueDetailViewModel = viewModel {
            RevenueDetailViewModel(
                application.container.projectRepository,
                application.container.shiftRepository,
                application.container.eventRepository
            )
        }

        RevenueDetailScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}
