package com.example.workertracking.ui.navigation

import android.net.Uri
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
import com.example.workertracking.ui.screens.WorkerPhotoGalleryScreen
import com.example.workertracking.ui.screens.workers.AddWorkerScreen
import com.example.workertracking.ui.screens.workers.EditWorkerScreen
import com.example.workertracking.ui.screens.workers.WorkerDetailScreen
import com.example.workertracking.ui.screens.workers.WorkersScreen
import com.example.workertracking.ui.viewmodel.AddWorkerViewModel
import com.example.workertracking.ui.viewmodel.WorkerDetailViewModel
import com.example.workertracking.ui.viewmodel.WorkersViewModel

fun NavGraphBuilder.workersNavigation(
    navController: NavHostController,
    application: WorkerTrackingApplication
) {
    composable(Screen.Workers.route) {
        val viewModel: WorkersViewModel = viewModel {
            WorkersViewModel(application.container.workerRepository)
        }
        val workers by viewModel.workers.collectAsState()
        val workersWithDebt by viewModel.workersWithDebt.collectAsState()
        val workerEarnings by viewModel.workerEarnings.collectAsState()
        val referenceWorkerNames by viewModel.referenceWorkerNames.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        WorkersScreen(
            workers = workers,
            workersWithDebt = workersWithDebt,
            workerEarnings = workerEarnings,
            referenceWorkerNames = referenceWorkerNames,
            isLoading = isLoading,
            onAddWorker = {
                navController.navigate(Screen.AddWorker.route)
            },
            onWorkerClick = { worker ->
                navController.navigate(Screen.WorkerDetail.createRoute(worker.id))
            },
            onDeleteWorker = { worker ->
                viewModel.deleteWorker(worker)
            }
        )
    }

    composable(Screen.AddWorker.route) {
        val viewModel: AddWorkerViewModel = viewModel {
            AddWorkerViewModel(application.container.workerRepository)
        }
        val saveSuccess by viewModel.saveSuccess.collectAsState()
        val availableWorkers by viewModel.availableWorkers.collectAsState()
        val error by viewModel.error.collectAsState()

        LaunchedEffect(saveSuccess) {
            if (saveSuccess) {
                viewModel.clearSaveSuccess()
                navController.popBackStack()
            }
        }

        AddWorkerScreen(
            availableWorkers = availableWorkers,
            error = error,
            onNavigateBack = {
                navController.popBackStack()
            },
            onSaveWorker = { name, phoneNumber, referenceId ->
                viewModel.saveWorker(name, phoneNumber, referenceId)
            },
            onClearError = {
                viewModel.clearError()
            }
        )
    }

    composable(
        route = Screen.WorkerDetail.route,
        arguments = listOf(navArgument("workerId") { type = NavType.LongType })
    ) { backStackEntry ->
        val workerId = backStackEntry.arguments?.getLong("workerId") ?: 0L
        val viewModel: WorkerDetailViewModel = viewModel {
            WorkerDetailViewModel(
                application.container.workerRepository,
                application.container.projectRepository,
                application.container.eventRepository,
                application.container.shiftRepository
            )
        }
        val worker by viewModel.worker.collectAsState()
        val referenceWorker by viewModel.referenceWorker.collectAsState()
        val projects by viewModel.projects.collectAsState()
        val events by viewModel.events.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val unpaidShifts by viewModel.unpaidShifts.collectAsState()
        val unpaidEvents by viewModel.unpaidEvents.collectAsState()
        val allShifts by viewModel.allShifts.collectAsState()
        val allEvents by viewModel.allEvents.collectAsState()
        val totalOwed by viewModel.totalOwed.collectAsState()
        val unpaidReferenceShifts by viewModel.unpaidReferenceShifts.collectAsState()
        val unpaidReferenceEvents by viewModel.unpaidReferenceEvents.collectAsState()
        val totalReferenceOwed by viewModel.totalReferenceOwed.collectAsState()
        val paidShifts by viewModel.paidShifts.collectAsState()
        val paidEvents by viewModel.paidEvents.collectAsState()
        val showPaidItems by viewModel.showPaidItems.collectAsState()
        val dateFilter by viewModel.dateFilter.collectAsState()

        LaunchedEffect(workerId) {
            viewModel.loadWorker(workerId)
        }

        WorkerDetailScreen(
            worker = worker,
            referenceWorker = referenceWorker,
            projects = projects,
            events = events,
            isLoading = isLoading,
            unpaidShifts = unpaidShifts,
            unpaidEvents = unpaidEvents,
            allShifts = allShifts,
            allEvents = allEvents,
            totalOwed = totalOwed,
            unpaidReferenceShifts = unpaidReferenceShifts,
            unpaidReferenceEvents = unpaidReferenceEvents,
            totalReferenceOwed = totalReferenceOwed,
            paidShifts = paidShifts,
            paidEvents = paidEvents,
            showPaidItems = showPaidItems,
            dateFilter = dateFilter,
            onNavigateBack = {
                navController.popBackStack()
            },
            onEditWorker = {
                navController.navigate(Screen.EditWorker.createRoute(workerId))
            },
            onDeleteWorker = {
                viewModel.deleteWorker()
                navController.popBackStack()
            },
            onViewPhotos = {
                worker?.let { w ->
                    navController.navigate(Screen.WorkerPhotoGallery.createRoute(w.id, w.name))
                }
            },
            onMarkShiftAsPaid = { shiftWorkerId ->
                viewModel.markShiftAsPaid(shiftWorkerId)
            },
            onMarkEventAsPaid = { eventWorkerId ->
                viewModel.markEventAsPaid(eventWorkerId)
            },
            onUpdateEventPayment = { eventWorkerId, isPaid, amountPaid, tipAmount ->
                viewModel.updateEventWorkerPayment(eventWorkerId, isPaid, amountPaid, tipAmount)
            },
            onRevokeShiftPayment = { shiftWorkerId ->
                viewModel.revokeShiftPayment(shiftWorkerId)
            },
            onRevokeEventPayment = { eventWorkerId ->
                viewModel.revokeEventPayment(eventWorkerId)
            },
            onMarkAllAsPaid = {
                viewModel.markAllAsPaid()
            },
            onToggleShowPaidItems = {
                viewModel.toggleShowPaidItems()
            },
            onDateRangeSelected = { startDate, endDate ->
                viewModel.setDateFilter(startDate, endDate)
            },
            onClearDateFilter = {
                viewModel.clearDateFilter()
            }
        )
    }

    composable(
        route = Screen.EditWorker.route,
        arguments = listOf(navArgument("workerId") { type = NavType.LongType })
    ) { backStackEntry ->
        val workerId = backStackEntry.arguments?.getLong("workerId") ?: 0L
        val viewModel: WorkerDetailViewModel = viewModel {
            WorkerDetailViewModel(
                application.container.workerRepository,
                application.container.projectRepository,
                application.container.eventRepository,
                application.container.shiftRepository
            )
        }
        val worker by viewModel.worker.collectAsState()
        val allWorkers by viewModel.allWorkers.collectAsState()
        val updateSuccess by viewModel.updateSuccess.collectAsState()
        val error by viewModel.error.collectAsState()

        LaunchedEffect(workerId) {
            viewModel.loadWorker(workerId)
        }

        LaunchedEffect(updateSuccess) {
            if (updateSuccess) {
                viewModel.clearUpdateSuccess()
                navController.popBackStack()
            }
        }

        EditWorkerScreen(
            worker = worker,
            availableWorkers = allWorkers,
            error = error,
            onNavigateBack = {
                navController.popBackStack()
            },
            onUpdateWorker = { name, phoneNumber, referenceId ->
                viewModel.updateWorker(name, phoneNumber, referenceId)
            },
            onClearError = {
                viewModel.clearError()
            }
        )
    }

    composable(
        route = Screen.WorkerPhotoGallery.route,
        arguments = listOf(
            navArgument("workerId") { type = NavType.LongType },
            navArgument("workerName") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val workerId = backStackEntry.arguments?.getLong("workerId") ?: 0L
        val workerName = Uri.decode(backStackEntry.arguments?.getString("workerName") ?: "")

        WorkerPhotoGalleryScreen(
            workerId = workerId,
            workerName = workerName,
            onNavigateUp = {
                navController.popBackStack()
            }
        )
    }
}
