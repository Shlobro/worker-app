package com.example.workertracking.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.workertracking.ui.screens.shifts.AddShiftScreen
import com.example.workertracking.ui.screens.shifts.EditShiftScreen
import com.example.workertracking.ui.screens.shifts.ShiftDetailScreen
import com.example.workertracking.ui.viewmodel.AddShiftViewModel
import com.example.workertracking.ui.viewmodel.ShiftDetailViewModel

fun NavGraphBuilder.shiftsNavigation(
    navController: NavHostController,
    application: WorkerTrackingApplication
) {
    composable(
        route = Screen.AddShift.route,
        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
    ) { backStackEntry ->
        val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
        val viewModel: AddShiftViewModel = viewModel {
            AddShiftViewModel(
                application.container.shiftRepository
            )
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
                AddShiftScreen(
                    projectId = projectId,
                    projectName = currentProject.name,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveShift = { pId, name, date, startTime, endTime, hours ->
                        viewModel.saveShift(pId, name, date, startTime, endTime, hours)
                    }
                )
            }
        }
    }

    composable(
        route = Screen.EditShift.route,
        arguments = listOf(navArgument("shiftId") { type = NavType.LongType })
    ) { backStackEntry ->
        val shiftId = backStackEntry.arguments?.getLong("shiftId") ?: 0L
        val viewModel: ShiftDetailViewModel = viewModel {
            ShiftDetailViewModel(
                application.container.shiftRepository,
                application.container.workerRepository
            )
        }
        val shift by viewModel.shift.collectAsState()
        val updateSuccess by viewModel.updateSuccess.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        LaunchedEffect(shiftId) {
            viewModel.loadShiftDetails(shiftId)
        }

        LaunchedEffect(updateSuccess) {
            if (updateSuccess) {
                viewModel.clearUpdateSuccess()
                navController.popBackStack()
            }
        }

        LaunchedEffect(isLoading, shift) {
            if (!isLoading && shift == null) {
                navController.popBackStack()
            }
        }

        if (isLoading) {
            LoadingContent()
        } else {
            shift?.let { currentShift ->
                var isProjectLoading by remember { mutableStateOf(true) }
                var hasFetchedProject by remember { mutableStateOf(false) }
                var project by remember { mutableStateOf<com.example.workertracking.data.entity.Project?>(null) }

                LaunchedEffect(currentShift.projectId) {
                    isProjectLoading = true
                    project = application.container.projectRepository.getProjectById(currentShift.projectId)
                    isProjectLoading = false
                    hasFetchedProject = true
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
                        EditShiftScreen(
                            shift = currentShift,
                            projectName = currentProject.name,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onUpdateShift = { name, date, startTime, endTime, hours ->
                                viewModel.updateShift(name, date, startTime, endTime, hours)
                            }
                        )
                    }
                }
            }
        }
    }

    composable(
        route = Screen.ShiftDetail.route,
        arguments = listOf(navArgument("shiftId") { type = NavType.LongType })
    ) { backStackEntry ->
        val shiftId = backStackEntry.arguments?.getLong("shiftId") ?: 0L
        val viewModel: ShiftDetailViewModel = viewModel {
            ShiftDetailViewModel(
                application.container.shiftRepository,
                application.container.workerRepository
            )
        }
        val shift by viewModel.shift.collectAsState()
        val shiftWorkers by viewModel.shiftWorkers.collectAsState()
        val allWorkers by viewModel.allWorkers.collectAsState()

        LaunchedEffect(shiftId) {
            viewModel.loadShiftDetails(shiftId)
        }

        shift?.let { currentShift ->
            ShiftDetailScreen(
                shift = currentShift,
                shiftWorkers = shiftWorkers,
                allWorkers = allWorkers,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditShift = {
                    navController.navigate(Screen.EditShift.createRoute(shiftId))
                },
                onDeleteShift = {
                    viewModel.deleteShift(currentShift)
                    navController.popBackStack()
                },
                onAddWorkerToShift = { sId, wId, isHourly, payRate, referencePayRate, isRefHourly ->
                    viewModel.addWorkerToShift(sId, wId, isHourly, payRate, referencePayRate, isRefHourly)
                },
                onRemoveWorkerFromShift = { sId, wId ->
                    viewModel.removeWorkerFromShift(sId, wId)
                },
                onUpdateWorkerPayment = { shiftWorker ->
                    viewModel.updateWorkerPayment(shiftWorker)
                },
                onUpdatePayment = { shiftWorkerId, isPaid, amount, tip ->
                    viewModel.updateShiftWorkerPayment(shiftWorkerId, isPaid, amount, tip)
                },
                onUpdateReferencePayment = { shiftWorkerId, isPaid, amount, tip ->
                    viewModel.updateShiftWorkerReferencePayment(shiftWorkerId, isPaid, amount, tip)
                }
            )
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
