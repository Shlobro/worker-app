package com.example.workertracking.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.workertracking.R
import com.example.workertracking.WorkerTrackingApplication
import com.example.workertracking.ui.screens.projects.AddIncomeScreen
import com.example.workertracking.ui.screens.projects.AddProjectScreen
import com.example.workertracking.ui.screens.projects.EditIncomeScreen
import com.example.workertracking.ui.screens.projects.EditProjectScreen
import com.example.workertracking.ui.screens.projects.ProjectDetailScreen
import com.example.workertracking.ui.screens.projects.ProjectIncomeListScreen
import com.example.workertracking.ui.screens.projects.ProjectsScreen
import com.example.workertracking.ui.viewmodel.AddIncomeViewModel
import com.example.workertracking.ui.viewmodel.AddProjectViewModel
import com.example.workertracking.ui.viewmodel.EmployersViewModel
import com.example.workertracking.ui.viewmodel.ProjectDetailViewModel
import com.example.workertracking.ui.viewmodel.ProjectsViewModel

fun NavGraphBuilder.projectsNavigation(
    navController: NavHostController,
    application: WorkerTrackingApplication
) {
    composable(Screen.Projects.route) {
        val viewModel: ProjectsViewModel = viewModel {
            ProjectsViewModel(application.container.projectRepository)
        }
        val projects by viewModel.projects.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        ProjectsScreen(
            projects = projects,
            isLoading = isLoading,
            onAddProject = {
                navController.navigate(Screen.AddProject.route)
            },
            onProjectClick = { project ->
                navController.navigate(Screen.ProjectDetail.createRoute(project.id))
            },
            onDeleteProject = { project ->
                viewModel.deleteProject(project)
            }
        )
    }

    composable(Screen.AddProject.route) {
        val viewModel: AddProjectViewModel = viewModel {
            AddProjectViewModel(application.container.projectRepository)
        }
        val employersViewModel: EmployersViewModel = viewModel {
            EmployersViewModel(application.container.employerRepository)
        }
        val saveSuccess by viewModel.saveSuccess.collectAsState()
        val availableEmployers by employersViewModel.employers.collectAsState()

        LaunchedEffect(saveSuccess) {
            if (saveSuccess) {
                viewModel.clearSaveSuccess()
                navController.popBackStack()
            }
        }

        AddProjectScreen(
            availableEmployers = availableEmployers,
            onNavigateBack = {
                navController.popBackStack()
            },
            onSaveProject = { name, location, startDate, employerId ->
                viewModel.saveProject(name, location, startDate, employerId)
            }
        )
    }

    composable(
        route = Screen.EditProject.route,
        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
    ) { backStackEntry ->
        val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
        val viewModel: ProjectDetailViewModel = viewModel {
            ProjectDetailViewModel(
                application.container.projectRepository,
                application.container.shiftRepository
            )
        }
        val project by viewModel.project.collectAsState()
        val updateSuccess by viewModel.updateSuccess.collectAsState()

        LaunchedEffect(projectId) {
            viewModel.loadProject(projectId)
        }

        LaunchedEffect(updateSuccess) {
            if (updateSuccess) {
                viewModel.clearUpdateSuccess()
                navController.popBackStack()
            }
        }

        EditProjectScreen(
            project = project,
            onNavigateBack = {
                navController.popBackStack()
            },
            onUpdateProject = { name, location, startDate ->
                viewModel.updateProject(name, location, startDate)
            }
        )
    }

    composable(
        route = Screen.ProjectDetail.route,
        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
    ) { backStackEntry ->
        val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
        val viewModel: ProjectDetailViewModel = viewModel {
            ProjectDetailViewModel(
                application.container.projectRepository,
                application.container.shiftRepository
            )
        }
        val project by viewModel.project.collectAsState()
        val shifts by viewModel.shifts.collectAsState()
        val totalIncome by viewModel.totalIncome.collectAsState()
        val totalPayments by viewModel.totalPayments.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(projectId) {
            viewModel.loadProject(projectId)
        }

        LaunchedEffect(error) {
            error?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }

        ProjectDetailScreen(
            project = project,
            shifts = shifts,
            totalIncome = totalIncome,
            totalPayments = totalPayments,
            isLoading = isLoading,
            snackbarHostState = snackbarHostState,
            onNavigateBack = {
                navController.popBackStack()
            },
            onEditProject = {
                navController.navigate(Screen.EditProject.createRoute(projectId))
            },
            onDeleteProject = {
                viewModel.deleteProject()
                navController.popBackStack()
            },
            onAddShift = {
                navController.navigate(Screen.AddShift.createRoute(projectId))
            },
            onShiftClick = { shiftId ->
                navController.navigate(Screen.ShiftDetail.createRoute(shiftId))
            },
            onDeleteShift = { shift ->
                viewModel.deleteShift(shift)
            },
            onAddIncome = {
                navController.navigate(Screen.AddIncome.createRoute(projectId))
            },
            onIncomeHistoryClick = {
                navController.navigate(Screen.ProjectIncomeList.createRoute(projectId))
            },
            onCloseProject = {
                viewModel.closeProject()
            }
        )
    }

    composable(
        route = Screen.AddIncome.route,
        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
    ) { backStackEntry ->
        val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
        val viewModel: AddIncomeViewModel = viewModel {
            AddIncomeViewModel(application.container.projectRepository)
        }
        val saveSuccess by viewModel.saveSuccess.collectAsState()
        var isProjectLoading by remember { mutableStateOf(true) }
        var hasFetchedProject by remember { mutableStateOf(false) }
        var project by remember { mutableStateOf<com.example.workertracking.data.entity.Project?>(null) }

        LaunchedEffect(projectId) {
            isProjectLoading = true
            project = application.container.projectRepository.getProjectById(projectId)
            isProjectLoading = false
            hasFetchedProject = true
        }

        LaunchedEffect(saveSuccess) {
            if (saveSuccess) {
                viewModel.clearSaveSuccess()
                navController.popBackStack()
            }
        }

        LaunchedEffect(hasFetchedProject, project) {
            if (hasFetchedProject && project == null) {
                navController.popBackStack()
            }
        }

        if (isProjectLoading) {
            LoadingContent()
        } else {
            project?.let { currentProject ->
                AddIncomeScreen(
                    projectId = projectId,
                    projectName = currentProject.name,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveIncome = { pId, date, description, amount, units ->
                        viewModel.saveIncome(pId, date, description, amount, units)
                    }
                )
            }
        }
    }

    composable(
        route = Screen.ProjectIncomeList.route,
        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
    ) { backStackEntry ->
        val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
        val viewModel: ProjectDetailViewModel = viewModel {
            ProjectDetailViewModel(
                application.container.projectRepository,
                application.container.shiftRepository
            )
        }

        LaunchedEffect(projectId) {
            viewModel.loadProject(projectId)
        }

        val project by viewModel.project.collectAsState()
        val incomeEntries by viewModel.incomeEntries.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        ProjectIncomeListScreen(
            projectName = project?.name ?: "",
            incomeEntries = incomeEntries,
            isLoading = isLoading,
            onNavigateBack = {
                navController.popBackStack()
            },
            onAddIncome = {
                navController.navigate(Screen.AddIncome.createRoute(projectId))
            },
            onEditIncome = { income ->
                navController.navigate(Screen.EditIncome.createRoute(income.id))
            },
            onDeleteIncome = { income ->
                viewModel.deleteIncome(income)
            }
        )
    }

    composable(
        route = Screen.EditIncome.route,
        arguments = listOf(navArgument("incomeId") { type = NavType.LongType })
    ) { backStackEntry ->
        val incomeId = backStackEntry.arguments?.getLong("incomeId") ?: 0L
        val viewModel: ProjectDetailViewModel = viewModel {
            ProjectDetailViewModel(
                application.container.projectRepository,
                application.container.shiftRepository
            )
        }

        LaunchedEffect(incomeId) {
            viewModel.loadIncomeById(incomeId)
        }

        val income by viewModel.currentIncome.collectAsState()
        val project by viewModel.project.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        LaunchedEffect(isLoading, income) {
            if (!isLoading && income == null) {
                navController.popBackStack()
            }
        }

        if (isLoading) {
            LoadingContent()
        } else {
            income?.let { incomeEntry ->
                EditIncomeScreen(
                    income = incomeEntry,
                    projectName = project?.name ?: "",
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onUpdateIncome = { updatedIncome ->
                        viewModel.updateIncome(updatedIncome)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator()
            Text(text = stringResource(R.string.loading))
        }
    }
}
