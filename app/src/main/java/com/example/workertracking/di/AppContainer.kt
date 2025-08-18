package com.example.workertracking.di

import android.content.Context
import com.example.workertracking.data.WorkerTrackingDatabase
import com.example.workertracking.repository.EmployerRepository
import com.example.workertracking.repository.EventRepository
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.ShiftRepository
import com.example.workertracking.repository.WorkerRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppContainer(context: Context) {
    private val database = WorkerTrackingDatabase.getDatabase(context)
    
    val projectRepository = ProjectRepository(database.projectDao(), database.projectIncomeDao())
    val workerRepository = WorkerRepository(database.workerDao(), database.paymentDao(), database.shiftWorkerDao(), database.eventWorkerDao())
    val shiftRepository = ShiftRepository(database.shiftDao(), database.shiftWorkerDao())
    val eventRepository = EventRepository(database.eventDao(), database.eventWorkerDao())
    val employerRepository = EmployerRepository(database.employerDao(), database.projectDao(), database.eventDao())
    
    // Shared refresh trigger for dashboard
    private val _dashboardRefreshTrigger = MutableSharedFlow<Unit>()
    val dashboardRefreshTrigger: SharedFlow<Unit> = _dashboardRefreshTrigger.asSharedFlow()
    
    fun triggerDashboardRefresh() {
        _dashboardRefreshTrigger.tryEmit(Unit)
    }
}