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
import com.example.workertracking.ui.screens.events.AddEventScreen
import com.example.workertracking.ui.screens.events.EditEventScreen
import com.example.workertracking.ui.screens.events.EventDetailScreen
import com.example.workertracking.ui.screens.events.EventsScreen
import com.example.workertracking.ui.viewmodel.AddEventViewModel
import com.example.workertracking.ui.viewmodel.EmployersViewModel
import com.example.workertracking.ui.viewmodel.EventDetailViewModel
import com.example.workertracking.ui.viewmodel.EventsViewModel

fun NavGraphBuilder.eventsNavigation(
    navController: NavHostController,
    application: WorkerTrackingApplication
) {
    composable(Screen.Events.route) {
        val viewModel: EventsViewModel = viewModel {
            EventsViewModel(application.container.eventRepository)
        }
        val events by viewModel.events.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        EventsScreen(
            events = events,
            isLoading = isLoading,
            onAddEvent = {
                navController.navigate(Screen.AddEvent.route)
            },
            onEventClick = { event ->
                navController.navigate(Screen.EventDetail.createRoute(event.id))
            },
            onDeleteEvent = { event ->
                viewModel.deleteEvent(event)
            }
        )
    }

    composable(Screen.AddEvent.route) {
        val viewModel: AddEventViewModel = viewModel {
            AddEventViewModel(application.container.eventRepository)
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

        AddEventScreen(
            availableEmployers = availableEmployers,
            onNavigateBack = {
                navController.popBackStack()
            },
            onSaveEvent = { name, date, startTime, endTime, hours, income, employerId ->
                viewModel.saveEvent(name, date, startTime, endTime, hours, income, employerId)
            }
        )
    }

    composable(
        route = Screen.EventDetail.route,
        arguments = listOf(navArgument("eventId") { type = NavType.LongType })
    ) { backStackEntry ->
        val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
        val viewModel: EventDetailViewModel = viewModel {
            EventDetailViewModel(
                application.container.eventRepository,
                application.container.workerRepository
            )
        }
        val event by viewModel.event.collectAsState()
        val eventWorkers by viewModel.eventWorkers.collectAsState()
        val allWorkers by viewModel.allWorkers.collectAsState()
        val totalCost by viewModel.totalCost.collectAsState()
        val deleteSuccess by viewModel.deleteSuccess.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        LaunchedEffect(eventId) {
            viewModel.loadEvent(eventId)
        }

        LaunchedEffect(deleteSuccess) {
            if (deleteSuccess) {
                viewModel.clearDeleteSuccess()
                navController.popBackStack()
            }
        }

        EventDetailScreen(
            event = event,
            eventWorkers = eventWorkers,
            allWorkers = allWorkers,
            totalCost = totalCost,
            isLoading = isLoading,
            onNavigateBack = {
                navController.popBackStack()
            },
            onEditEvent = {
                navController.navigate(Screen.EditEvent.createRoute(eventId))
            },
            onDeleteEvent = {
                viewModel.deleteEvent()
            },
            onAddWorkerToEvent = { eId, workerId, hours, isHourlyRate, payRate, referencePayRate, isRefHourly ->
                viewModel.addWorkerToEvent(eId, workerId, hours, isHourlyRate, payRate, referencePayRate, isRefHourly)
            },
            onRemoveWorker = { eventWorker ->
                viewModel.removeWorkerFromEvent(eventWorker)
            },
            onUpdatePayment = { eventWorkerId, isPaid, amountPaid, tipAmount ->
                viewModel.updateEventWorkerPayment(eventWorkerId, isPaid, amountPaid, tipAmount)
            },
            onUpdateReferencePayment = { eventWorkerId, isPaid, amountPaid, tipAmount ->
                viewModel.updateEventWorkerReferencePayment(eventWorkerId, isPaid, amountPaid, tipAmount)
            },
            onUpdateWorker = { eventWorker ->
                viewModel.updateWorkerInEvent(eventWorker)
            }
        )
    }

    composable(
        route = Screen.EditEvent.route,
        arguments = listOf(navArgument("eventId") { type = NavType.LongType })
    ) { backStackEntry ->
        val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
        val viewModel: EventDetailViewModel = viewModel {
            EventDetailViewModel(
                application.container.eventRepository,
                application.container.workerRepository
            )
        }
        val employersViewModel: EmployersViewModel = viewModel {
            EmployersViewModel(application.container.employerRepository)
        }
        val event by viewModel.event.collectAsState()
        val updateSuccess by viewModel.updateSuccess.collectAsState()
        val availableEmployers by employersViewModel.employers.collectAsState()

        LaunchedEffect(eventId) {
            viewModel.loadEvent(eventId)
        }

        LaunchedEffect(updateSuccess) {
            if (updateSuccess) {
                viewModel.clearUpdateSuccess()
                navController.popBackStack()
            }
        }

        EditEventScreen(
            event = event,
            availableEmployers = availableEmployers,
            onNavigateBack = {
                navController.popBackStack()
            },
            onUpdateEvent = { name, date, startTime, endTime, hours, income, employerId ->
                viewModel.updateEvent(name, date, startTime, endTime, hours, income, employerId)
            }
        )
    }
}
