package com.example.workertracking.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.workertracking.WorkerTrackingApplication
import com.example.workertracking.ui.screens.employers.AddEmployerScreen
import com.example.workertracking.ui.screens.employers.EditEmployerScreen
import com.example.workertracking.ui.screens.employers.EmployerDetailScreen
import com.example.workertracking.ui.screens.employers.EmployersScreen
import com.example.workertracking.ui.viewmodel.AddEmployerViewModel
import com.example.workertracking.ui.viewmodel.EditEmployerViewModel
import com.example.workertracking.ui.viewmodel.EmployerDetailViewModel
import com.example.workertracking.ui.viewmodel.EmployersViewModel

fun NavGraphBuilder.employersNavigation(
    navController: NavHostController,
    application: WorkerTrackingApplication
) {
    composable(Screen.Employers.route) {
        val viewModel: EmployersViewModel = viewModel {
            EmployersViewModel(application.container.employerRepository)
        }
        val employers by viewModel.employers.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        EmployersScreen(
            employers = employers,
            isLoading = isLoading,
            onAddEmployer = {
                navController.navigate(Screen.AddEmployer.route)
            },
            onEmployerClick = { employer ->
                navController.navigate(Screen.EmployerDetail.createRoute(employer.id))
            },
            onDeleteEmployer = { employer ->
                viewModel.deleteEmployer(employer)
            }
        )
    }

    composable(Screen.AddEmployer.route) {
        val viewModel: AddEmployerViewModel = viewModel {
            AddEmployerViewModel(application.container.employerRepository)
        }
        val saveSuccess by viewModel.saveSuccess.collectAsState()

        LaunchedEffect(saveSuccess) {
            if (saveSuccess) {
                viewModel.clearSaveSuccess()
                navController.popBackStack()
            }
        }

        AddEmployerScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onSaveEmployer = { name, phoneNumber ->
                viewModel.saveEmployer(name, phoneNumber)
            }
        )
    }

    composable(
        route = Screen.EditEmployer.route,
        arguments = listOf(navArgument("employerId") { type = NavType.LongType })
    ) { backStackEntry ->
        val employerId = backStackEntry.arguments?.getLong("employerId") ?: 0L
        val viewModel: EditEmployerViewModel = viewModel {
            EditEmployerViewModel(application.container.employerRepository)
        }
        val employer by viewModel.employer.collectAsState()
        val updateSuccess by viewModel.updateSuccess.collectAsState()

        LaunchedEffect(employerId) {
            viewModel.loadEmployer(employerId)
        }

        LaunchedEffect(updateSuccess) {
            if (updateSuccess) {
                viewModel.clearUpdateSuccess()
                navController.popBackStack()
            }
        }

        EditEmployerScreen(
            employer = employer,
            onNavigateBack = {
                navController.popBackStack()
            },
            onUpdateEmployer = { name, phoneNumber ->
                viewModel.updateEmployer(name, phoneNumber)
            }
        )
    }

    composable(
        route = Screen.EmployerDetail.route,
        arguments = listOf(navArgument("employerId") { type = NavType.LongType })
    ) { backStackEntry ->
        val employerId = backStackEntry.arguments?.getLong("employerId") ?: 0L
        val viewModel: EmployerDetailViewModel = viewModel {
            EmployerDetailViewModel(application.container.employerRepository)
        }
        val employer by viewModel.employer.collectAsState()
        val projects by viewModel.projects.collectAsState()
        val events by viewModel.events.collectAsState()
        val totalIncome by viewModel.totalIncome.collectAsState()
        val totalExpenses by viewModel.totalExpenses.collectAsState()
        val totalProfit by viewModel.totalProfit.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val deleteSuccess by viewModel.deleteSuccess.collectAsState()

        LaunchedEffect(employerId) {
            viewModel.loadEmployer(employerId)
        }

        LaunchedEffect(deleteSuccess) {
            if (deleteSuccess) {
                viewModel.clearDeleteSuccess()
                navController.popBackStack()
            }
        }

        EmployerDetailScreen(
            employer = employer,
            projects = projects,
            events = events,
            isLoading = isLoading,
            totalProfit = totalProfit,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            onNavigateBack = {
                navController.popBackStack()
            },
            onEditEmployer = {
                navController.navigate(Screen.EditEmployer.createRoute(employerId))
            },
            onDeleteEmployer = {
                viewModel.deleteEmployer()
            },
            onProjectClick = { project ->
                navController.navigate(Screen.ProjectDetail.createRoute(project.id))
            },
            onEventClick = { event ->
                navController.navigate(Screen.EventDetail.createRoute(event.id))
            }
        )
    }
}
