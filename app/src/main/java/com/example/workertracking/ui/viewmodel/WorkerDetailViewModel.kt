package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.Event
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.repository.WorkerRepository
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.EventRepository
import com.example.workertracking.repository.ShiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkerDetailViewModel(
    private val workerRepository: WorkerRepository,
    private val projectRepository: ProjectRepository,
    private val eventRepository: EventRepository,
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    private val _worker = MutableStateFlow<Worker?>(null)
    val worker: StateFlow<Worker?> = _worker.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadWorker(workerId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _worker.value = workerRepository.getWorkerById(workerId)
                loadWorkerProjectsAndEvents(workerId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadWorkerProjectsAndEvents(workerId: Long) {
        viewModelScope.launch {
            // Load projects for this worker through shifts
            shiftRepository.getShiftsByWorker(workerId).collect { shifts ->
                val projectIds = shifts.map { it.projectId }.distinct()
                val workerProjects = mutableListOf<Project>()
                projectIds.forEach { projectId ->
                    projectRepository.getProjectById(projectId)?.let { project ->
                        workerProjects.add(project)
                    }
                }
                _projects.value = workerProjects
            }
        }
        viewModelScope.launch {
            // Load events for this worker through EventWorker
            // For now, load all events - TODO: filter by worker
            eventRepository.getAllEvents().collect { events ->
                _events.value = events
            }
        }
    }
}