package com.example.workertracking.di

import android.content.Context
import com.example.workertracking.data.WorkerTrackingDatabase
import com.example.workertracking.repository.EventRepository
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.ShiftRepository
import com.example.workertracking.repository.WorkerRepository

class AppContainer(context: Context) {
    private val database = WorkerTrackingDatabase.getDatabase(context)
    
    val projectRepository = ProjectRepository(database.projectDao())
    val workerRepository = WorkerRepository(database.workerDao(), database.paymentDao())
    val shiftRepository = ShiftRepository(database.shiftDao())
    val eventRepository = EventRepository(database.eventDao(), database.eventWorkerDao())
}